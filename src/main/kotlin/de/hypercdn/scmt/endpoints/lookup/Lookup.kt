package de.hypercdn.scmt.endpoints.lookup

import de.hypercdn.scmt.entities.json.MarketItemJson
import de.hypercdn.scmt.entities.json.MarketSnapshotJson
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketSnapshotRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class Lookup @Autowired constructor(
    var marketItemRepository: MarketItemRepository,
    var marketSnapshotRepository: MarketSnapshotRepository
){

    @GetMapping("/lookup")
    fun getPricesFor(
        @RequestParam(value = "gameid", required = true) gameId: Int,
        @RequestParam(value = "market_hash_name", required = true) marketHashName: String
    ): MarketItemJson {
        val item = marketItemRepository.findByIdOrNull(MarketItem.Key(gameId, marketHashName)) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return MarketItemJson(item).apply {
            history = marketSnapshotRepository.getAllByAppIdAndName(item).map { MarketSnapshotJson(it) }
        }
    }

}