package de.hypercdn.scmt.endpoints.inventory

import de.hypercdn.scmt.entities.json.AppJson
import de.hypercdn.scmt.entities.json.UserInventoryJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.UserInventoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class InventoryInfo @Autowired constructor(
    val appRepository: AppRepository,
    val inventoryRepository: UserInventoryRepository
) {

    @GetMapping("/inventory/{appId}/{userId}")
    fun getInfoForInventory(
        @PathVariable("userId") appId: Int,
        @PathVariable("userId") userId: Long,
    ): ResponseEntity<UserInventoryJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventory = inventoryRepository.findUserInventoryByAppAndUserId(app, userId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventoryJson = UserInventoryJson(inventory)
            .includeApp {
                AppJson(it)
                    .includeId()
                    .includeName()
                    .includeProperties()
            }
            .includeUserId()
            .includeProperties()
        return ResponseEntity(inventoryJson, HttpStatus.OK)
    }

}