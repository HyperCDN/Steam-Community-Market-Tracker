package de.hypercdn.scmt.endpoints.lookup

import com.fasterxml.jackson.databind.ObjectMapper
import de.hypercdn.scmt.entities.json.MarketItemJson
import de.hypercdn.scmt.entities.json.MarketSnapshotJson
import de.hypercdn.scmt.entities.json.StatisticsJson
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketSnapshotRepository
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestController
class Lookup @Autowired constructor(
    var marketItemRepository: MarketItemRepository,
    var marketSnapshotRepository: MarketSnapshotRepository,
    var objectMapper: ObjectMapper
) {

    @GetMapping("/lookup", params = ["type=history"])
    fun getPriceHistoryFor(
        @RequestParam(value = "gameid", required = true) gameId: Int,
        @RequestParam(value = "market_hash_name", required = true) marketHashName: String,
        @RequestParam(value = "includeFullHistory", required = false, defaultValue = "false") includeFullHistory: Boolean
    ): MarketItemJson {
        val item = marketItemRepository.findByIdOrNull(MarketItem.Key(gameId, marketHashName)) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return MarketItemJson(item)
            .apply {
                history = if (includeFullHistory)
                    marketSnapshotRepository.getAllFor(item).map { MarketSnapshotJson(it) }
                else
                    listOf(MarketSnapshotJson(marketSnapshotRepository.getLatestFor(item)))
            }
    }

    @Validated
    @GetMapping("/lookup", params = ["type=stats"])
    fun getPricesStatisticFor(
        @RequestParam(value = "gameid", required = true) gameId: Int,
        @RequestParam(value = "market_hash_name", required = true) marketHashName: String,
        @RequestParam(value = "range", required = false, defaultValue = "1") @Min(1) @Max(4) range: Int
    ): MarketItemJson {
        val item = marketItemRepository.findByIdOrNull(MarketItem.Key(gameId, marketHashName)) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val node = HashMap<String, StatisticsJson>().apply {
            val now = LocalDateTime.now()
            put("day", StatisticsJson.from(marketSnapshotRepository.getStatisticsBetween(item, now, now.minusDays(1))))
            if (range >= 2)
                put("week", StatisticsJson.from(marketSnapshotRepository.getStatisticsBetween(item, now, now.minusWeeks(1))))
            if (range >= 3)
                put("month", StatisticsJson.from(marketSnapshotRepository.getStatisticsBetween(item, now, now.minusMonths(1))))
            if (range >= 4)
                put("year", StatisticsJson.from(marketSnapshotRepository.getStatisticsBetween(item, now, now.minusYears(1))))
        }
        return MarketItemJson(item)
            .apply {
                statistics = objectMapper.valueToTree(node)
            }
    }


}