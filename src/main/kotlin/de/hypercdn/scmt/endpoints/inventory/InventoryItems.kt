package de.hypercdn.scmt.endpoints.inventory

import de.hypercdn.scmt.entities.json.out.InventoryItemJson
import de.hypercdn.scmt.entities.json.out.MarketItemJson
import de.hypercdn.scmt.entities.json.out.MarketSnapshotJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemSnapshotRepository
import de.hypercdn.scmt.entities.sql.repositories.UserInventoryItemSnapshotRepository
import de.hypercdn.scmt.entities.sql.repositories.UserInventoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class InventoryItems @Autowired constructor(
    val appRepository: AppRepository,
    val inventoryRepository: UserInventoryRepository,
    val userInventoryItemSnapshotRepository: UserInventoryItemSnapshotRepository,
    val snapshotRepository: MarketItemSnapshotRepository
) {

    @GetMapping("/inventory/{appId}/{userId}/items")
    fun getInventoryItemsWithPrices(
        @PathVariable("appId") appId: Int,
        @PathVariable("userId") userId: Long,
    ): ResponseEntity<List<InventoryItemJson>> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventory = inventoryRepository.findUserInventoryByAppAndUserId(app, userId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val items = userInventoryItemSnapshotRepository.getItemsCurrentlyInUserInventory(inventory)
        val snapshots = snapshotRepository.getLatestFor(items.map { it.marketItem }).associateBy { it.marketItemUUID }
        val itemJsons = items.map {
            InventoryItemJson(it)
                .includeItem {
                    MarketItemJson(it)
                        .includeName()
                        .includeProperties()
                }
                .includeIdentity()
                .includeProperties()
                .includeSnapshot {
                    snapshots.get(it)?.let {
                        MarketSnapshotJson(it)
                            .includeAvailability()
                            .includePrice()
                            .includeProperties()
                    }
                }
        }.toList()
        return ResponseEntity(itemJsons, HttpStatus.OK)
    }

}