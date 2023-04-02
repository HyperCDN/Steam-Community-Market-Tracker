package de.hypercdn.scmt.endpoints.app

import de.hypercdn.scmt.entities.json.AppJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class AppInfo @Autowired constructor(
    val appRepository: AppRepository
) {

    @GetMapping("/app/{appId}")
    fun getAppInfo(
        @PathVariable("appId") appId: Int
    ): ResponseEntity<AppJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val appJson = AppJson(app)
            .includeId()
            .includeName()
            .includeProperties()
        return ResponseEntity(appJson, HttpStatus.OK)
    }

}