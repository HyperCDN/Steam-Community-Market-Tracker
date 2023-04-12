package de.hypercdn.scmt.endpoints.item

import de.hypercdn.scmt.entities.json.out.MarketItemJson
import de.hypercdn.scmt.entities.json.out.MarketSnapshotJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemSnapshotRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class ItemInfo @Autowired constructor(
    val appRepository: AppRepository,
    val marketItemRepository: MarketItemRepository,
    val snapshotRepository: MarketItemSnapshotRepository
) {

    @GetMapping("/items/{appId}")
    fun getItemList(
        @PathVariable("appId") appId: Int
    ): ResponseEntity<List<MarketItemJson>> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val items = marketItemRepository.getAllMarketItemsByApp(app)
        val itemJsons = items.map {
            MarketItemJson(it)
                .includeName()
                .includeProperties()
        }
        return ResponseEntity(itemJsons, HttpStatus.OK)
    }

    @GetMapping("/item/{appId}/{marketHashName}")
    fun getItemInfo(
        @PathVariable("appId") appId: Int,
        @PathVariable("marketHashName") marketHashName: String
    ): ResponseEntity<MarketItemJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val item = marketItemRepository.findMarketItemByAppAndName(app, marketHashName) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val lastSnapshot = snapshotRepository.getLatestFor(item)
        val itemJson = MarketItemJson(item)
            .includeName()
            .includeProperties()
            .includeSnapshot(lastSnapshot == null) {
                MarketSnapshotJson(lastSnapshot)
                    .includeAvailability()
                    .includePrice()
                    .includeProperties()
            }
        return ResponseEntity(itemJson, HttpStatus.OK)
    }

}