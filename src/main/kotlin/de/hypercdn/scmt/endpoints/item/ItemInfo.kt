package de.hypercdn.scmt.endpoints.item

import de.hypercdn.scmt.entities.json.out.MarketItemJson
import de.hypercdn.scmt.entities.json.out.MarketItemSnapshotJson
import de.hypercdn.scmt.entities.json.out.PagedJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemSnapshotRepository
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
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

@Validated
@RestController
class ItemInfo @Autowired constructor(
    val appRepository: AppRepository,
    val marketItemRepository: MarketItemRepository,
    val snapshotRepository: MarketItemSnapshotRepository
) {

    @GetMapping("/items/{appId}")
    fun getItemList(
        @PathVariable("appId") @Min(0) appId: Int,
        @RequestParam("tracked", required = false) tracked: Boolean?,
        @RequestParam("page", required = false, defaultValue = "0") @Min(0) page: Int,
        @RequestParam("count", required = false, defaultValue = "100") @Min(1) @Max(250) count: Int
    ): ResponseEntity<PagedJson<MarketItemJson>> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val pageRequest = PageRequest.of(page, count)
        val items = marketItemRepository.getAllMarketItemsByApp(
            app, tracked, pageRequest
        )
        val itemJsons = items.map {
            MarketItemJson(it)
                .includeName()
                .includeProperties()
        }
        val paged = PagedJson(pageRequest, itemJsons)
        return ResponseEntity(paged, HttpStatus.OK)
    }

    @GetMapping("/item/{appId}/{marketHashName}")
    fun getItemInfo(
        @PathVariable("appId") @Min(0) appId: Int,
        @PathVariable("marketHashName") @NotBlank marketHashName: String,
        @RequestParam("include-latest-snapshot", required = false, defaultValue = "false") includeLatestSnapshot: Boolean
    ): ResponseEntity<MarketItemJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val item = marketItemRepository.findMarketItemByAppAndName(app, marketHashName) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val lastSnapshot = if (includeLatestSnapshot) snapshotRepository.getLatestFor(item) else null
        val itemJson = MarketItemJson(item)
            .includeName()
            .includeProperties()
            .includeSnapshot(lastSnapshot == null) {
                MarketItemSnapshotJson(lastSnapshot)
                    .includeAvailability()
                    .includePrice()
                    .includeProperties()
            }
        return ResponseEntity(itemJson, HttpStatus.OK)
    }

}