package de.hypercdn.scmt.endpoints.inventory

import de.hypercdn.scmt.entities.json.`in`.UserInventoryCreateJson
import de.hypercdn.scmt.entities.json.`in`.UserInventoryUpdateJson
import de.hypercdn.scmt.entities.json.out.AppJson
import de.hypercdn.scmt.entities.json.out.UserInventoryJson
import de.hypercdn.scmt.entities.sql.entities.UserInventory
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.UserInventoryRepository
import jakarta.validation.constraints.Min
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Validated
@RestController
class InventoryManagement @Autowired constructor(
    val appRepository: AppRepository,
    val inventoryRepository: UserInventoryRepository
) {

    @PostMapping("/inventory/{appId}")
    fun addInventory(
        @RequestBody requestBody: UserInventoryCreateJson,
        @PathVariable("appId") @Min(0) appId: Int
    ): ResponseEntity<UserInventoryJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        inventoryRepository.findUserInventoryByAppAndUserId(app, requestBody.userId)?.let {
            throw ResponseStatusException(HttpStatus.CONFLICT)
        }
        val inventory = UserInventory().apply {
            userId = requestBody.userId
            requestBody.properties?.let {
                it.tracked?.let { v -> tracked = v }
            }
        }
        val savedInventory = inventoryRepository.save(inventory)
        val inventoryJson = UserInventoryJson(savedInventory)
            .includeApp {
                AppJson(it)
                    .includeId()
                    .includeName()
                    .includeProperties()
            }
            .includeUserId()
            .includeProperties()
        return ResponseEntity(inventoryJson, HttpStatus.CREATED)
    }

    @PatchMapping("/inventory/{appId}/{userId}")
    fun updateInventorySettings(
        @RequestBody requestBody: UserInventoryUpdateJson,
        @PathVariable("appId") @Min(0) appId: Int,
        @PathVariable("userId") @Min(0) userId: Long,
    ): ResponseEntity<UserInventoryJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventory = inventoryRepository.findUserInventoryByAppAndUserId(app, userId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        requestBody.properties?.let {
            it.tracked?.let { v -> inventory.tracked = v }
        }
        val savedInventory = inventoryRepository.save(inventory)
        val inventoryJson = UserInventoryJson(savedInventory)
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

    @DeleteMapping("/inventory/{appId}/{userId}")
    fun deleteInventor(
        @PathVariable("appId") @Min(0) appId: Int,
        @PathVariable("userId") @Min(0) userId: Long,
    ): ResponseEntity<Void> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val inventory = inventoryRepository.findUserInventoryByAppAndUserId(app, userId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        inventoryRepository.delete(inventory)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}