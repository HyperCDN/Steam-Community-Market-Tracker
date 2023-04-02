package de.hypercdn.scmt.util.steam

import com.fasterxml.jackson.databind.JsonNode
import de.hypercdn.scmt.config.AppConfig
import de.hypercdn.scmt.config.ItemSearchConfig
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.util.steam.api.SteamFetchService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SteamMarketItemBean @Autowired constructor(
    var appRepository: AppRepository,
    var marketItemRepository: MarketItemRepository,
    var steamFetchService: SteamFetchService,
    var appSearchConfig: AppConfig,
    var itemSearchConfig: ItemSearchConfig
) {

    var log: Logger = LoggerFactory.getLogger(SteamMarketItemBean::class.java)
    var running: AtomicBoolean = AtomicBoolean(false)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        if (!itemSearchConfig.updateOnStartup) return
        updateListOfItemsAndSweepFetchSnapshots()
    }

    @Async
    @Scheduled(cron = "\${steam-community-market-tracker.item-search.cron}")
    fun onCron() {
        if (!itemSearchConfig.updateOnCron) return
        updateListOfItemsAndSweepFetchSnapshots()
    }

    fun updateListOfItemsAndSweepFetchSnapshots() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Update already in progress - Skipping execution")
            return
        }
        try {
            log.info("Starting update...")
            LocalDateTime.now() + itemSearchConfig.noUpdateBefore
            appRepository.getAllAppsDueToItemScan(itemSearchConfig.noUpdateBefore).forEach { app ->
                log.info("Fetching items for app {}", app.id)
                val knownMarketItems = marketItemRepository.getAllMarketItemsByApp(app).associateBy { it.name }
                var start = 0
                val fetchedItems = HashMap<String, JsonNode>()
                while (true) {
                    val retrieved = steamFetchService.retrieveMarketItemsFromSteam(app.id, start)
                    start += retrieved.size
                    if (retrieved.isEmpty()) {
                        break
                    }
                    val currentlyFetchedMarketItems = retrieved.associateBy { it.get("name").asText() }
                    fetchedItems.putAll(currentlyFetchedMarketItems)
                    // to add
                    val addToDbSet = HashSet(currentlyFetchedMarketItems.keys).apply { removeAll(knownMarketItems.keys) }
                    marketItemRepository.saveAll(currentlyFetchedMarketItems.filter { addToDbSet.contains(it.key) }.map {
                        MarketItem().apply {
                            appUUID = app.__uuid
                            name = it.value.get("name").asText()
                            tracked = itemSearchConfig.trackNewByDefault
                        }
                    })
                    log.info("Added {} (run {}) items to app {}", addToDbSet.size, start, app.id)
                }
                if (start == 0 && appSearchConfig.untrackOnNoFoundItems) {
                    appRepository.save(app.apply { tracked = false })
                    log.info("No longer tracking app {}: no items found", app.id)
                    return@forEach
                }
                if (itemSearchConfig.deleteNotFoundEntities) {
                    val removeFromDbSet = HashSet(knownMarketItems.keys).apply { removeAll(fetchedItems.keys) }.map { knownMarketItems.get(it)?.__uuid }
                    marketItemRepository.deleteAllById(removeFromDbSet)
                    log.debug("Removed {} items from app {}", removeFromDbSet.size, app.id)
                }
                log.info("Finished fetching items for app {}", app.id)
                appRepository.save(app.apply { lastItemScan = OffsetDateTime.now() })
            }
            log.info("Update finished")
        } catch (e: Exception) {
            log.error("An exception occurred performing update", e)
        } finally {
            running.set(false)
        }
    }

}