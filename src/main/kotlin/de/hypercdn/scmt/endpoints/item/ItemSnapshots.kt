package de.hypercdn.scmt.endpoints.item

import de.hypercdn.scmt.entities.json.MarketSnapshotJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketSnapshotRepository
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.Range
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class ItemSnapshots @Autowired constructor(
    val appRepository: AppRepository,
    val marketItemRepository: MarketItemRepository,
    val snapshotRepository: MarketSnapshotRepository
) {

    @GetMapping("/item/{appId}/{marketHashName}/snapshots")
    fun getItemSnapshots(
        @PathVariable("appId") appId: Int,
        @PathVariable("marketHashName") marketHashName: String,
        @RequestParam("page", required = false, defaultValue = "0") @Min(0) page: Int,
        @RequestParam("chunkSize", required = false, defaultValue = "10") @Range(min = 1, max = 1000) chunkSize: Int
    ): ResponseEntity<List<MarketSnapshotJson>> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val item = marketItemRepository.findMarketItemByAppAndName(app, marketHashName) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val snapshots = snapshotRepository.getAllFor(item, PageRequest.of(page, chunkSize, Sort.by(Sort.Direction.DESC, "createdAt")))
        val snapshotJsons = snapshots.map {
            MarketSnapshotJson(it)
                .includeAvailability()
                .includePrice()
                .includeProperties()
        }
        return ResponseEntity(snapshotJsons, HttpStatus.OK)
    }

}