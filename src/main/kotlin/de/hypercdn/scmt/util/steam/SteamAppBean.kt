package de.hypercdn.scmt.util.steam

import de.hypercdn.scmt.config.AppConfig
import de.hypercdn.scmt.entities.sql.entities.App
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.util.steam.api.SteamFetchService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
class SteamAppBean @Autowired constructor(
    var steamFetchService: SteamFetchService,
    var appConfig: AppConfig,
    var appRepository: AppRepository
) {

    var log: Logger = LoggerFactory.getLogger(SteamAppBean::class.java)
    var running: AtomicBoolean = AtomicBoolean(false)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        if (!appConfig.updateOnStartup) return
        updateListOfSteamApps()
    }

    @Async
    @Scheduled(cron = "\${steam-community-market-tracker.app-search.cron}")
    fun onCron() {
        if (!appConfig.updateOnCron) return
        updateListOfSteamApps()
    }

    fun updateListOfSteamApps() {
        if (!running.compareAndSet(false, true)){
            log.warn("Update already in progress - Skipping execution")
            return
        }
        try {
            log.info("Starting update...")
            val appsFromGithub = steamFetchService.retrieveAppListFromGithub()
            val githubAppMap = appsFromGithub.associateBy { it.get("app-id").asInt() }

            val appsFromDb = appRepository.findAll().toList()
            val dbAppMap = appsFromDb.associateBy { it.id }

            val removeFromDbSet = HashSet(dbAppMap.keys).apply { removeAll(githubAppMap.keys) }
            val addToDbSet = HashSet(githubAppMap.keys).apply { removeAll(dbAppMap.keys) }
            val updateDbSet = HashSet(dbAppMap.keys).apply { retainAll(githubAppMap.keys) }.filter { githubAppMap.get(it)?.get("name")?.asText() != dbAppMap.get(it)?.name }.toHashSet()
            // update existing
            updateDbSet.forEach {
                githubAppMap.get(it)?.get("name")?.asText()?.let { name ->
                    appsFromDb.get(it).name = name
                }
            }
            val updated = appRepository.saveAll(appsFromDb.filter { updateDbSet.contains(it.id) && it.name != githubAppMap.get(it.id)?.get("name")?.asText() })
            log.info("Updated {} apps", updated.count())
            // add new
            val added = appRepository.saveAll(appsFromGithub.filter { addToDbSet.contains(it.get("app-id").asInt()) }.map {
                App().apply {
                    id = it.get("app-id").asInt()
                    name = it.get("name").asText()
                    tracked = appConfig.trackNewByDefault
                }
            })
            log.info("Added {} apps", added.count())
            // delete removed
            if (appConfig.deleteNotFoundApp) {
                val deleted = appRepository.deleteAppByAppIds(removeFromDbSet)
                log.info("Deleted {} apps", deleted)
            }
            log.info("Update finished")
        }catch (e: Exception) {
            log.error("An exception occurred performing update", e)
        }finally {
            running.set(false)
        }
    }

}