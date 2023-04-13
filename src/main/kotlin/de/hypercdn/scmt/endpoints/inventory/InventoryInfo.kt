package de.hypercdn.scmt.endpoints.inventory

import de.hypercdn.scmt.entities.json.out.*
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemSnapshotRepository
import de.hypercdn.scmt.entities.sql.repositories.UserInventoryItemSnapshotRepository
import de.hypercdn.scmt.entities.sql.repositories.UserInventoryRepository
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Validated
@RestController
class InventoryInfo @Autowired constructor(
    val appRepository: AppRepository,
    val inventoryRepository: UserInventoryRepository,
    val userInventoryItemSnapshotRepository: UserInventoryItemSnapshotRepository,
    val snapshotRepository: MarketItemSnapshotRepository
) {

    @GetMapping("/inventory/{appId}/{userId}")
    fun getInventoryInfo(
        @PathVariable("appId") @Min(0) appId: Int,
        @PathVariable("userId") @Min(0) userId: Long,
        @RequestParam("state-of", required = false) stateOf: OffsetDateTime?,
        @RequestParam("include-value-evaluation", required = false, defaultValue = "false") includeValueEvaluation: Boolean,
    ): ResponseEntity<UserInventoryJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventory = inventoryRepository.findUserInventoryByAppAndUserId(app, userId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventoryJson = UserInventoryJson(inventory)
            .includeUserId()
            .includeProperties()
            .includeInventoryValue(!includeValueEvaluation) {
                val items = userInventoryItemSnapshotRepository.getItemsFor(inventory, stateOf)
                val snapshots = snapshotRepository.getLatestFor(items.map { it.marketItem }).associateBy { it.marketItemUUID }
                UserInventoryJson.InventoryValueJson().apply {
                    totalItems = items.count()
                    val itemsWithSnapshot = items.map { it to snapshots.get(it.marketItemUUID) }.filter { it.second != null }
                    evaluatedItems = itemsWithSnapshot.count()
                    minBasedEvaluation = itemsWithSnapshot.mapNotNull { it.second!!.price.lowestPrice }.sum()
                    medBasedEvaluation = itemsWithSnapshot.mapNotNull { it.second!!.price.medianPrice }.sum()
                }
            }
        return ResponseEntity(inventoryJson, HttpStatus.OK)
    }

    @GetMapping("/inventory/{appId}/{userId}/items")
    fun getInventoryItemsWithPrices(
        @PathVariable("appId") @Min(0) appId: Int,
        @PathVariable("userId") @Min(0) userId: Long,
        @RequestParam("state-of", required = false) stateOf: OffsetDateTime?,
        @RequestParam("include-latest-snapshot", required = false, defaultValue = "true") includeLatestSnapshot: Boolean,
        @RequestParam("page", required = false, defaultValue = "0") @Min(0) page: Int,
        @RequestParam("count", required = false, defaultValue = "100") @Min(1) @Max(250) count: Int
    ): ResponseEntity<List<UserInventoryItemSnapshotJson>> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventory = inventoryRepository.findUserInventoryByAppAndUserId(app, userId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val items = userInventoryItemSnapshotRepository.getItemsFor(
            inventory,
            stateOf,
            PageRequest.of(page, count)
        )
        val snapshots = if (includeLatestSnapshot) snapshotRepository.getLatestFor(items.map { it.marketItem }).associateBy { it.marketItemUUID } else emptyMap()
        val itemJsons = items.map {
            UserInventoryItemSnapshotJson(it)
                .includeItem {
                    MarketItemJson(it)
                        .includeName()
                        .includeProperties()
                }
                .includeIdentity()
                .includeProperties()
                .includeSnapshot(snapshots.isEmpty()) {
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