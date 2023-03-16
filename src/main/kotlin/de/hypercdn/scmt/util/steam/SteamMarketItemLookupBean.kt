package de.hypercdn.scmt.util.steam

import de.hypercdn.scmt.config.EntityLookupConfig
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketSnapshotRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SteamMarketItemLookupBean @Autowired constructor(
    var appRepository: AppRepository,
    var marketItemRepository: MarketItemRepository,
    var marketSnapshotRepository: MarketSnapshotRepository,
    var steamFetchService: SteamFetchService,
    var entityLookupConfig: EntityLookupConfig
) {

    var log: Logger = LoggerFactory.getLogger(SteamMarketItemLookupBean::class.java)
    var running: AtomicBoolean = AtomicBoolean(false)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        if (!entityLookupConfig.marketItem.onStartup) {
            log.info("Startup update of community market items has been disabled")
            return
        }
        updateListOfItemsAndSweepFetchSnapshots()
    }

    @Async
    @Scheduled(cron = "\${steam-community-market-tracker.entity-lookup.market-item.cron}")
    fun onCron() {
        if (!entityLookupConfig.marketItem.onCron) {
            log.info("Automatic update of community market items has been disabled")
            return
        }
        updateListOfItemsAndSweepFetchSnapshots()
    }

    fun updateListOfItemsAndSweepFetchSnapshots() {
        if (!running.compareAndSet(false, true)){
            return
        }
        try {
            log.info("Updating list of community market items...")
            val trackedApps = appRepository.getAllTrackedApps()
            for (trackedApp in trackedApps) {
                log.info("Updating list of community market items for app {}...", trackedApp.id)
                val dbMarketItems = marketItemRepository.getAllMarketItemsByApp(trackedApp).associateBy { it.name }
                var start = 0
                val fetchedMarketItemsTotal = HashMap<String, MarketItem>()
                while (true) {
                    val retrieved = steamFetchService.retrieveMarketItemsFromSteam(trackedApp.id, start)
                    start += retrieved.size
                    if (retrieved.isEmpty()) {
                        break
                    }
                    val fetchedMarketItems = retrieved.map { it.first }.associateBy { it.name }
                    fetchedMarketItemsTotal.putAll(fetchedMarketItems)
                    val addToDbSet = HashSet(fetchedMarketItems.keys).apply { removeAll(dbMarketItems.keys) }
                    marketItemRepository.saveAll(fetchedMarketItems.filter { addToDbSet.contains(it.key) }.values)

                    val fetchedMarketSnapshots = retrieved.map { it.second }
                    val fetchedMarketSnapshotsToSave = ArrayList(fetchedMarketSnapshots).filter { dbMarketItems.contains(it.name) && dbMarketItems.get(it.name)?.tracked == true }
                    marketSnapshotRepository.saveAll(fetchedMarketSnapshotsToSave)

                    log.info("Updated list of community market items for app {}: +{} [{}]", trackedApp.id, addToDbSet.size, marketItemRepository.getCountyByApp(trackedApp))
                }
                if (start == 0 && entityLookupConfig.marketItem.disableTrackingOnEmptyResponse) {
                    log.info("No longer tracking items for app {} - no items found", trackedApp.id)
                    appRepository.save(trackedApp.apply { tracked = false })
                    continue
                }
                // delete if there are some
                val removeFromDbSet = HashSet(dbMarketItems.keys).apply { removeAll(fetchedMarketItemsTotal.keys) }
                if (entityLookupConfig.marketItem.deleteNotFoundEntities) {
                    marketItemRepository.deleteAllById(removeFromDbSet.map { MarketItem.Key(trackedApp.id, it) })
                    log.info("Updated list of community market items for app {}: -{} [{}]", trackedApp.id, removeFromDbSet.size, marketItemRepository.getCountyByApp(trackedApp))
                }
                log.info("Updated list of community market items for app {}: {}", trackedApp.id, marketItemRepository.getCountyByApp(trackedApp))
            }
            log.info("Updated list of community market items [{}]", marketItemRepository.count())
        } catch (e: Exception) {
            log.error("An exception occurred while updating the list of known community market items", e)
        }finally {
            running.set(false)
        }
    }

}