package de.hypercdn.scmt.endpoints.app

import de.hypercdn.scmt.entities.json.out.AppJson
import de.hypercdn.scmt.entities.json.out.PagedJson
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Validated
@RestController
class AppInfo @Autowired constructor(
    val appRepository: AppRepository
) {

    @GetMapping("/apps")
    fun getAppList(
        @RequestParam("tracked", required = false) tracked: Boolean?,
        @RequestParam("page", required = false, defaultValue = "0") @Min(0) page: Int,
        @RequestParam("count", required = false, defaultValue = "100") @Min(1) @Max(250) count: Int
    ): ResponseEntity<PagedJson<AppJson>> {
        val pageRequest = PageRequest.of(page, count)
        val apps = appRepository.findAll(
            tracked, pageRequest
        )
        val appJsons = apps.map {
            AppJson(it)
                .includeId()
                .includeName()
                .includeProperties()
        }
        val paged = PagedJson(pageRequest, appJsons)
        return ResponseEntity(paged, HttpStatus.OK)
    }

    @GetMapping("/app/{appId}")
    fun getAppInfo(
        @PathVariable("appId") @Min(0) appId: Int
    ): ResponseEntity<AppJson> {
        val app = appRepository.findAppByAppId(appId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val appJson = AppJson(app)
            .includeId()
            .includeName()
            .includeProperties()
        return ResponseEntity(appJson, HttpStatus.OK)
    }

}