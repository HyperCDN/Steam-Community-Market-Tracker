package de.hypercdn.scmt.endpoints.item

import com.fasterxml.jackson.databind.ObjectMapper
import de.hypercdn.scmt.entities.json.StatisticsJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketSnapshotRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@RestController
class ItemStatistics @Autowired constructor(
    val appRepository: AppRepository,
    val marketItemRepository: MarketItemRepository,
    val snapshotRepository: MarketSnapshotRepository,
    val objectMapper: ObjectMapper
) {

    @GetMapping("/item/{appId}/{marketHashName}/price", params = ["base=low"])
    fun getItemPriceStatisticsLow(
        @PathVariable("appId") appId: Int,
        @PathVariable("marketHashName") marketHashName: String
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
        @PathVariable("appId") appId: Int,
        @PathVariable("marketHashName") marketHashName: String,
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
        @PathVariable("appId") appId: Int,
        @PathVariable("marketHashName") marketHashName: String,
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