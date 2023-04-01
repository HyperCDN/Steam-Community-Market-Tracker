package de.hypercdn.scmt.endpoints.lookup

import de.hypercdn.scmt.entities.json.MarketItemJson
import de.hypercdn.scmt.entities.json.MarketSnapshotJson
import de.hypercdn.scmt.entities.json.StatisticsJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketSnapshotRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestController
class ItemLookup @Autowired constructor(
    var appRepository: AppRepository,
    var marketItemRepository: MarketItemRepository,
    var marketSnapshotRepository: MarketSnapshotRepository
) {

    @GetMapping("/lookup/item")
    fun getInfoForItem(
        @RequestParam(value = "gameid", required = true) gameId: Int,
        @RequestParam(value = "market_hash_name", required = true) marketHashName: String,
        @RequestParam(value = "includeFullHistory", required = false, defaultValue = "false") includeFullHistory: Boolean
    ): MarketItemJson {
        val app = appRepository.findAppByAppId(gameId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val item = marketItemRepository.findMarketItemByAppAndName(app, marketHashName) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val statistics = LinkedHashMap<String, StatisticsJson>().apply {
            val now = LocalDateTime.now()
            put("day", StatisticsJson.from(marketSnapshotRepository.getStatisticsBetween(item, now, now.minusDays(1))))
            put("week", StatisticsJson.from(marketSnapshotRepository.getStatisticsBetween(item, now, now.minusWeeks(1))))
            put("month", StatisticsJson.from(marketSnapshotRepository.getStatisticsBetween(item, now, now.minusMonths(1))))
            put("year", StatisticsJson.from(marketSnapshotRepository.getStatisticsBetween(item, now, now.minusYears(1))))
        }
        return MarketItemJson(item).apply {
            this.statistics = statistics
            if (includeFullHistory)
                this.history = marketSnapshotRepository.getAllFor(item).map { MarketSnapshotJson(it) }
        }
    }

}