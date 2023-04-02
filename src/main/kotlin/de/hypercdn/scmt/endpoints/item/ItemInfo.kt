package de.hypercdn.scmt.endpoints.item

import de.hypercdn.scmt.entities.json.AppJson
import de.hypercdn.scmt.entities.json.MarketItemJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
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
    val marketItemRepository: MarketItemRepository
) {

    @GetMapping("/item/{appId}/{marketHashName}")
    fun getItemInfo(
        @PathVariable("appId") appId: Int,
        @PathVariable("marketHashName") marketHashName: String
    ): ResponseEntity<MarketItemJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val item = marketItemRepository.findMarketItemByAppAndName(app, marketHashName) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val itemJson = MarketItemJson(item)
            .includeApp {
                AppJson(it)
                    .includeId()
                    .includeName()
            }
            .includeIds()
            .includeProperties()
        return ResponseEntity(itemJson, HttpStatus.OK)
    }

}