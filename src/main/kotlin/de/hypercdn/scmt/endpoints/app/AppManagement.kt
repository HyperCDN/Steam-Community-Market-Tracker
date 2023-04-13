package de.hypercdn.scmt.endpoints.app

import de.hypercdn.scmt.entities.json.`in`.AppUpdateJson
import de.hypercdn.scmt.entities.json.out.AppJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import jakarta.validation.constraints.Min
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Validated
@RestController
class AppManagement @Autowired constructor(
    val appRepository: AppRepository
) {

    @PatchMapping("/app/{appId}")
    fun updateAppSettings(
        @RequestBody requestBody: AppUpdateJson,
        @PathVariable("appId") @Min(0) appId: Int
    ): ResponseEntity<AppJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        requestBody.properties?.let {
            it.tracked?.let { v -> app.tracked = v }
        }
        val savedApp = appRepository.save(app)
        val appJson = AppJson(savedApp)
            .includeId()
            .includeName()
            .includeProperties()
        return ResponseEntity(appJson, HttpStatus.OK)
    }

    @DeleteMapping("/app/{appId}")
    fun deleteApp(
        @PathVariable("appId") @Min(0) appId: Int
    ): ResponseEntity<Void> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        appRepository.delete(app)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}