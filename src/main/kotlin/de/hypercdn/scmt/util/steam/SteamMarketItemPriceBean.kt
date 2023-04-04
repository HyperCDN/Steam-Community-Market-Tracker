package de.hypercdn.scmt.util.steam

import de.hypercdn.scmt.config.ItemPriceSearchConfig
import de.hypercdn.scmt.config.ItemSearchConfig
import de.hypercdn.scmt.entities.sql.entities.MarketSnapshot
import de.hypercdn.scmt.entities.sql.repositories.AppRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketSnapshotRepository
import de.hypercdn.scmt.util.delay.Delay
import de.hypercdn.scmt.util.steam.api.SteamFetchService
import lombok.Synchronized
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SteamMarketItemPriceBean @Autowired constructor(
    var appRepository: AppRepository,
    var marketItemRepository: MarketItemRepository,
    var marketSnapshotRepository: MarketSnapshotRepository,
    var steamFetchService: SteamFetchService,
    var itemSearchConfig: ItemSearchConfig,
    var itemPriceSearchConfig: ItemPriceSearchConfig
) {

    var log: Logger = LoggerFactory.getLogger(SteamMarketItemPriceBean::class.java)
    var running: AtomicBoolean = AtomicBoolean(false)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    @Delay(amountPropertyValue = "steam-community-market-tracker.item-price-search.update-on-startup-delay")
    fun onStartup() {
        if (!itemPriceSearchConfig.updateOnStartup) return
        updateTrackedItemSnapshots()
    }

    @Async
    @Scheduled(cron = "\${steam-community-market-tracker.item-price-search.cron}")
    fun onCron() {
        if (!itemPriceSearchConfig.updateOnCron) return
        updateTrackedItemSnapshots()
    }

    @Synchronized
    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 60_000, multiplier = 3.0, maxDelay = 240_000))
    fun updateTrackedItemSnapshots() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Update already in progress - Skipping execution")
            return
        }
        try {
            log.info("Starting update...")
            appRepository.getAllTrackedApps()
                .forEach { app ->
                    log.info("Fetching item prices for app {}", app.id)
                    marketItemRepository.getMarketItemsDueToItemScan(app, itemPriceSearchConfig.noUpdateBefore)
                        .forEach inner@{ marketItem ->
                            log.info("Fetching item price for item {} ({}) from app {}", marketItem.__uuid, marketItem.name, app.id)
                            val priceOverview = try {
                                steamFetchService.retrievePriceOverviewFromSteam(app.id, marketItem.name)
                            } catch (e: SteamFetchService.HttpFetchException) {
                                if (e.code == 404 && itemSearchConfig.disableNotFoundEntities) {
                                    marketItemRepository.save(marketItem.apply { tracked = false })
                                    return@inner
                                }
                                throw e
                            }
                            marketSnapshotRepository.save(
                                MarketSnapshot().apply {
                                    marketItemUUID = marketItem.__uuid
                                    stats = MarketSnapshot.Stats().apply {
                                        volume = priceOverview.get("volume")?.asInt()
                                    }
                                    price = MarketSnapshot.Price().apply {
                                        lowestPrice = priceOverview.get("lowest-price")?.asDouble()
                                        medianPrice = priceOverview.get("median-price")?.asDouble()
                                        currency = priceOverview.get("currency")?.asText()
                                    }
                                }
                            )
                            // update timestamp
                            marketItem.lastItemScan = OffsetDateTime.now()
                            marketItemRepository.save(marketItem)
                        }
                    log.info("Finished fetching item prices for app {}", app.id)
                }
            log.info("Update finished")
        } catch (e: Exception) {
            log.error("An exception occurred performing update", e)
        } finally {
            running.set(false)
        }
    }

}