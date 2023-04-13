package de.hypercdn.scmt.endpoints.inventory

import de.hypercdn.scmt.entities.json.out.*
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
class InventoryInfo @Autowired constructor(
    val appRepository: AppRepository,
    val inventoryRepository: UserInventoryRepository,
    val userInventoryItemSnapshotRepository: UserInventoryItemSnapshotRepository,
    val snapshotRepository: MarketItemSnapshotRepository
) {

    @GetMapping("/inventory/{appId}/{userId}")
    fun getInventoryInfo(
        @PathVariable("appId") appId: Int,
        @PathVariable("userId") userId: Long,
    ): ResponseEntity<UserInventoryJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventory = inventoryRepository.findUserInventoryByAppAndUserId(app, userId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventoryJson = UserInventoryJson(inventory)
            .includeApp {
                AppJson(it)
                    .includeId()
                    .includeName()
                    .includeProperties()
            }
            .includeUserId()
            .includeProperties()
        return ResponseEntity(inventoryJson, HttpStatus.OK)
    }

    @GetMapping("/inventory/{appId}/{userId}/items")
    fun getInventoryItemsWithPrices(
        @PathVariable("appId") appId: Int,
        @PathVariable("userId") userId: Long,
    ): ResponseEntity<List<UserInventoryItemSnapshotJson>> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventory = inventoryRepository.findUserInventoryByAppAndUserId(app, userId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val items = userInventoryItemSnapshotRepository.getItemsCurrentlyInUserInventory(inventory)
        val snapshots = snapshotRepository.getLatestFor(items.map { it.marketItem }).associateBy { it.marketItemUUID }
        val itemJsons = items.map {
            UserInventoryItemSnapshotJson(it)
                .includeItem {
                    MarketItemJson(it)
                        .includeName()
                        .includeProperties()
                }
                .includeIdentity()
                .includeProperties()
                .includeSnapshot {
                    snapshots.get(it)?.let {
                        MarketItemSnapshotJson(it)
                            .includeAvailability()
                            .includePrice()
                            .includeProperties()
                    }
                }
        }.toList()
        return ResponseEntity(itemJsons, HttpStatus.OK)
    }

}