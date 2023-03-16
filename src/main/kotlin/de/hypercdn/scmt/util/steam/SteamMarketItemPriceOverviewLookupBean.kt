package de.hypercdn.scmt.util.steam

import de.hypercdn.scmt.config.EntityLookupConfig
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
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SteamMarketItemPriceOverviewLookupBean @Autowired constructor(
    var appRepository: AppRepository,
    var marketItemRepository: MarketItemRepository,
    var marketSnapshotRepository: MarketSnapshotRepository,
    var steamFetchService: SteamFetchService,
    var entityLookupConfig: EntityLookupConfig
) {

    var log: Logger = LoggerFactory.getLogger(SteamMarketItemPriceOverviewLookupBean::class.java)
    var running: AtomicBoolean = AtomicBoolean(false)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        if (!entityLookupConfig.marketItemPriceOverview.onStartup) {
            log.info("Startup update of tracked community market items price overview has been disabled")
            return
        }
        updateTrackedItemSnapshots()
    }

    @Async
    @Scheduled(cron = "\${steam-community-market-tracker.entity-lookup.market-item-price-overview.cron}")
    fun onCron() {
        if (!entityLookupConfig.marketItemPriceOverview.onCron) {
            log.info("Automatic update of tracked community market item price overview has been disabled")
            return
        }
        updateTrackedItemSnapshots()
    }

    fun updateTrackedItemSnapshots() {
        if (!running.compareAndSet(false, true)){
            return
        }
        try {
            log.info("Updating the price overview of tracked community market items...")
            val trackedApps = appRepository.getAllTrackedApps()
            for (trackedApp in trackedApps) {
                marketItemRepository.getAllTrackedMarketItemsByApp(trackedApp).forEach {
                    steamFetchService.retrievePriceOverviewFromSteam(trackedApp.id, it.name)?.let { it1 -> marketSnapshotRepository.save(it1) }
                }
            }
            log.info("Updated the price overview of community market items")
        } catch (e: Exception) {
            log.error("An exception occurred while updating the price overview of tracked community market items", e)
        }finally {
            running.set(false)
        }
    }

}