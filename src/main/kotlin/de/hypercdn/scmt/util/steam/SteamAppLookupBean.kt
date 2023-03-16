package de.hypercdn.scmt.util.steam

import de.hypercdn.scmt.config.EntityLookupConfig
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
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
class SteamAppLookupBean @Autowired constructor(
    var steamFetchService: SteamFetchService,
    var entityLookupConfig: EntityLookupConfig,
    var appRepository: AppRepository
) {

    var log: Logger = LoggerFactory.getLogger(SteamAppLookupBean::class.java)
    var running: AtomicBoolean = AtomicBoolean(false)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        if (!entityLookupConfig.app.onStartup) {
            log.info("Startup steam app update has been disabled")
            return
        }
        updateListOfSteamApps()
    }

    @Async
    @Scheduled(cron = "\${steam-community-market-tracker.entity-lookup.app.cron}")
    fun onCron() {
        if (!entityLookupConfig.app.onCron) {
            log.info("Automatic steam app update has been disabled")
            return
        }
        updateListOfSteamApps()
    }

    fun updateListOfSteamApps() {
        if (!running.compareAndSet(false, true)){
            return
        }
        try {
            log.info("Updating list of known steam apps...")
            val appsFromGithub = steamFetchService.retrieveAppListFromGithub()
            val githubAppMap = appsFromGithub.associateBy { it.id }

            val appsFromDb = appRepository.findAll().toList()
            val dbAppMap = appsFromDb.associateBy { it.id }

            val removeFromDbSet = HashSet(dbAppMap.keys).apply { removeAll(githubAppMap.keys) }
            val addToDbSet = HashSet(githubAppMap.keys).apply { removeAll(dbAppMap.keys) }
            val updateDbSet = HashSet(dbAppMap.keys).apply { retainAll(githubAppMap.keys) }.filter { githubAppMap.get(it)?.name != dbAppMap.get(it)?.name }.toHashSet()
            updateDbSet.forEach{
                githubAppMap.get(it)?.name?.let { name ->
                    appsFromDb.get(it).name = name
                }
            }

            if (entityLookupConfig.app.deleteNotFoundEntities) {
                appRepository.deleteAllById(removeFromDbSet)
            }

            appRepository.saveAll(appsFromDb.filter { updateDbSet.contains(it.id) && it.name != githubAppMap.get(it.id)?.name })
            appRepository.saveAll(appsFromGithub.filter { addToDbSet.contains(it.id) })

            log.info("Updated  list of known steam apps: +{} ~{} -{} [{}]", addToDbSet.count(), updateDbSet.count(), removeFromDbSet.count(),  appRepository.count())
        }catch (e: Exception) {
            log.error("An exception occurred while updating the list of known stream apps", e)
        }finally {
            running.set(false)
        }
    }

}