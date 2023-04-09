package de.hypercdn.scmt.endpoints.item

import de.hypercdn.scmt.entities.json.`in`.MarketItemUpdateJson
import de.hypercdn.scmt.entities.json.out.MarketItemJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class ItemManagement @Autowired constructor(
    val appRepository: AppRepository,
    val marketItemRepository: MarketItemRepository
) {

    @PatchMapping("/item/{appId}/{marketHashName}")
    fun updateItemSettings(
        @RequestBody requestBody: MarketItemUpdateJson,
        @PathVariable("appId") appId: Int,
        @PathVariable("marketHashName") marketHashName: String
    ): ResponseEntity<MarketItemJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val item = marketItemRepository.findMarketItemByAppAndName(app, marketHashName) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        requestBody.properties?.let {
            it.tracked?.let { v -> item.tracked = v }
        }
        val itemSaved = marketItemRepository.save(item)
        val itemJson = MarketItemJson(itemSaved)
            .includeName()
            .includeProperties()
        return ResponseEntity(itemJson, HttpStatus.OK)
    }

}