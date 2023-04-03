package de.hypercdn.scmt.util.steam

import de.hypercdn.scmt.config.InventorySearchConfig
import de.hypercdn.scmt.entities.sql.entities.InventoryItem
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.repositories.InventoryItemRepository
import de.hypercdn.scmt.entities.sql.repositories.MarketItemRepository
import de.hypercdn.scmt.entities.sql.repositories.UserInventoryRepository
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
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@Component
class SteamInventoryBean @Autowired constructor(
    var steamFetchService: SteamFetchService,
    val inventorySearchConfig: InventorySearchConfig,
    val inventoryRepository: UserInventoryRepository,
    val inventoryItemRepository: InventoryItemRepository,
    val marketItemRepository: MarketItemRepository
) {

    var log: Logger = LoggerFactory.getLogger(SteamAppBean::class.java)
    var running: AtomicBoolean = AtomicBoolean(false)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        if (!inventorySearchConfig.updateOnStartup) return
        updateInventories()
    }

    @Async
    @Scheduled(cron = "\${steam-community-market-tracker.app-search.cron}")
    fun onCron() {
        if (!inventorySearchConfig.updateOnCron) return
        updateInventories()
    }

    @Synchronized
    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 60_000, multiplier = 3.0, maxDelay = 240_000))
    fun updateInventories() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Update already in progress - Skipping execution")
            return
        }
        try {
            log.info("Starting update...")
            inventoryRepository.getInventoriesDueToItemScan(inventorySearchConfig.noUpdateBefore).forEach { inv ->
                val inventoryItems = steamFetchService.retrieveInventory(inv.app.id, inv.userId)
                    .map {
                        InventoryItem().apply {
                            userInventoryUUID = inv.__uuid
                            val item = marketItemRepository.findMarketItemByAppAndName(inv.app, it.get("name").asText()) ?: marketItemRepository.save(MarketItem().apply {
                                appUUID = inv.appUUID
                                name = it.get("name").asText()
                                tracked = inventorySearchConfig.trackUnknownByDefault
                                log.info("Adding unknown market item {} from inventory for user {} and app {}", name, inv.userId, inv.app.id)
                            })
                            marketItemUUID = item.__uuid
                            identity = InventoryItem.Identity(
                                it.get("context-id").asLong(),
                                it.get("asset-id").asLong(),
                                it.get("class-id").asLong(),
                                it.get("instance-id").asLong()
                            )
                            amount = it.get("amount").asInt()
                            automaticFetched = true
                        }
                    }.associateBy { it.identity }
                val currentStateItems = inventoryItemRepository.getItemsCurrentlyInUserInventory(inv).associateBy { it.identity }
                val keys = HashSet<InventoryItem.Identity>().apply {
                    addAll(inventoryItems.keys)
                    addAll(currentStateItems.keys)
                }
                val pairs = keys.stream().map { key -> Pair(inventoryItems.get(key), currentStateItems.get(key)) }
                val deleteItems = ArrayList<InventoryItem>()
                val addItems = ArrayList<InventoryItem>()
                pairs.forEach { pair ->
                    if (pair.first == null && pair.second != null) deleteItems.add(pair.second!!)
                    if (pair.first != null && pair.second == null) addItems.add(pair.first!!)
                    if (pair.first != null && pair.second != null) {
                        if (pair.first?.amount != pair.second?.amount) {
                            deleteItems.add(pair.second!!)
                            addItems.add(pair.first!!)
                        }
                    }
                }
                log.info("Found changes for +{} -{} for user {} and app {}", addItems.size, deleteItems.size, inv.userId, inv.app.id)
                inventoryItemRepository.saveAll(deleteItems.map { it.apply { superseded = OffsetDateTime.now() } })
                inventoryItemRepository.saveAll(addItems)

                inventoryRepository.save(inv.apply { lastItemScan = OffsetDateTime.now() })
            }
            log.info("Update finished")
        } catch (e: Exception) {
            log.error("An exception occurred performing update", e)
        } finally {
            running.set(false)
        }
    }

}