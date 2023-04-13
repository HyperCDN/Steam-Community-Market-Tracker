package de.hypercdn.scmt.endpoints.item

import de.hypercdn.scmt.entities.json.out.MarketItemSnapshotJson
import de.hypercdn.scmt.entities.json.out.StatisticsJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemSnapshotRepository
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
class ItemSnapshotInfo @Autowired constructor(
    val appRepository: AppRepository,
    val marketItemRepository: MarketItemRepository,
    val snapshotRepository: MarketItemSnapshotRepository
) {

    @GetMapping("/item/{appId}/{marketHashName}/snapshots")
    fun getItemSnapshots(
        @PathVariable("appId") @Min(0) appId: Int,
        @PathVariable("marketHashName") @NotBlank marketHashName: String,
        @RequestParam("page", required = false, defaultValue = "0") @Min(0) page: Int,
        @RequestParam("chunkSize", required = false, defaultValue = "10") @Min(1) @Max(250) chunkSize: Int
    ): ResponseEntity<List<MarketItemSnapshotJson>> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val item = marketItemRepository.findMarketItemByAppAndName(app, marketHashName) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val snapshots = snapshotRepository.getAllFor(item, PageRequest.of(page, chunkSize, Sort.by(Sort.Direction.DESC, "createdAt")))
        val snapshotJsons = snapshots.map {
            MarketItemSnapshotJson(it)
                .includeAvailability()
                .includePrice()
                .includeProperties()
        }
        return ResponseEntity(snapshotJsons, HttpStatus.OK)
    }

    @GetMapping("/item/{appId}/{marketHashName}/price", params = ["base=low"])
    fun getItemPriceStatisticsLow(
        @PathVariable("appId") @Min(0) appId: Int,
        @PathVariable("marketHashName") @NotBlank marketHashName: String
    ): ResponseEntity<HashMap<String, StatisticsJson<Double>>> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val item = marketItemRepository.findMarketItemByAppAndName(app, marketHashName) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val statisticsMap = LinkedHashMap<String, StatisticsJson<Double>>().apply {
            put("1d", StatisticsJson.from(snapshotRepository.getPriceStatisticsForLowestBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusDays(1))))
            put("1w", StatisticsJson.from(snapshotRepository.getPriceStatisticsForLowestBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusWeeks(1))))
            put("1m", StatisticsJson.from(snapshotRepository.getPriceStatisticsForLowestBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusMonths(1))))
            put("1y", StatisticsJson.from(snapshotRepository.getPriceStatisticsForLowestBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusYears(1))))
        }
        return ResponseEntity(statisticsMap, HttpStatus.OK)
    }

    @GetMapping("/item/{appId}/{marketHashName}/price", params = ["base=med"])
    fun getItemPriceStatisticsMed(
        @PathVariable("appId") @Min(0) appId: Int,
        @PathVariable("marketHashName") @NotBlank marketHashName: String,
    ): ResponseEntity<HashMap<String, StatisticsJson<Double>>> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val item = marketItemRepository.findMarketItemByAppAndName(app, marketHashName) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val statisticsMap = LinkedHashMap<String, StatisticsJson<Double>>().apply {
            put("1d", StatisticsJson.from(snapshotRepository.getPriceStatisticsForMedianBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusDays(1))))
            put("1w", StatisticsJson.from(snapshotRepository.getPriceStatisticsForMedianBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusWeeks(1))))
            put("1m", StatisticsJson.from(snapshotRepository.getPriceStatisticsForMedianBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusMonths(1))))
            put("1y", StatisticsJson.from(snapshotRepository.getPriceStatisticsForMedianBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusYears(1))))
        }
        return ResponseEntity(statisticsMap, HttpStatus.OK)
    }

    @GetMapping("/item/{appId}/{marketHashName}/volume")
    fun getItemVolumeStatistics(
        @PathVariable("appId") @Min(0) appId: Int,
        @PathVariable("marketHashName") @NotBlank marketHashName: String
    ): ResponseEntity<HashMap<String, StatisticsJson<Int>>> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val item = marketItemRepository.findMarketItemByAppAndName(app, marketHashName) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val statisticsMap = LinkedHashMap<String, StatisticsJson<Int>>().apply {
            put("1d", StatisticsJson.from(snapshotRepository.getVolumeStatisticsBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusDays(1))))
            put("1w", StatisticsJson.from(snapshotRepository.getVolumeStatisticsBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusWeeks(1))))
            put("1m", StatisticsJson.from(snapshotRepository.getVolumeStatisticsBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusMonths(1))))
            put("1y", StatisticsJson.from(snapshotRepository.getVolumeStatisticsBetween(item, OffsetDateTime.now(), OffsetDateTime.now().minusYears(1))))
        }
        return ResponseEntity(statisticsMap, HttpStatus.OK)
    }

}