package de.hypercdn.scmt.util.steam.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.hypercdn.scmt.config.MiscConfig
import de.hypercdn.scmt.util.data.parseCurrencyToNumber
import de.hypercdn.scmt.util.data.parseNumberWithDecorations
import de.hypercdn.scmt.util.delay.Delay
import de.hypercdn.scmt.util.http.HttpFetchException
import de.hypercdn.scmt.util.http.SCMTProxiedCallManager
import lombok.Synchronized
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class SteamFetchService @Autowired constructor(
    val proxyClient: SCMTProxiedCallManager,
    val objectMapper: ObjectMapper,
    val miscConfig: MiscConfig
) {

    val log: Logger = LoggerFactory.getLogger(SteamFetchService::class.java)

    @Synchronized
    fun retrieveAppListFromGithub(): List<JsonNode> {
        log.debug("Fetching app id list from github")
        val request = Request.Builder()
            .url("https://raw.githubusercontent.com/dgibbs64/SteamCMD-AppID-List/main/steamcmd_appid.json")
            .build()
        proxyClient.newCall(request).execute().use {
            if (!it.isSuccessful)
                throw HttpFetchException(it.code, "Failed to fetch url ${request.url} with code ${it.code}")
            return objectMapper
                .readTree(it.body.string())
                .get("applist")
                .get("apps")
                .map {
                    objectMapper.createObjectNode()
                        .put("app-id", it.get("appid").asInt())
                        .put("name", it.get("name").asText())
                }
        }
    }

    @Synchronized
    @Delay(amountPropertyValue = "steam-community-market-tracker.rate-limits.market-item-price-search")
    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 30_000, maxDelay = 120_000, multiplier = 2.0))
    fun retrievePriceOverviewFromSteam(appId: Int, name: String, currency: Int = miscConfig.currency): JsonNode {
        log.debug("Fetching price overview from steam for item {} from app {}", name, appId)
        val request = Request.Builder()
            .url("https://steamcommunity.com/market/priceoverview/?appid=${appId}&market_hash_name=${URLEncoder.encode(name, Charsets.UTF_8)}&currency=${currency}")
            .build()
        proxyClient.newCall(request).execute().use {
            if (!it.isSuccessful)
                throw HttpFetchException(it.code, "Failed to fetch url ${request.url} with code ${it.code}")
            val json = objectMapper.readTree(it.body.string())
            if (json.get("success")?.asBoolean() != true)
                throw HttpFetchException(it.code, "Body indicated failure for ${request.url}")
            return objectMapper.createObjectNode().apply {
                json.get("volume")?.let { jsonNode ->
                    put("volume", parseNumberWithDecorations(jsonNode.asText()).toInt())
                }
                json.get("lowest_price")?.let { jsonNode ->
                    val lowestPrice = parseCurrencyToNumber(jsonNode.asText())
                    put("lowest-price", lowestPrice.first?.toDouble())
                    put("currency", lowestPrice.second)
                }
                json.get("median_price")?.let { jsonNode ->
                    val medianPrice = parseCurrencyToNumber(jsonNode.asText())
                    put("median-price", medianPrice.first?.toDouble())
                    put("currency", medianPrice.second)
                }
            }
        }
    }


    @Synchronized
    @Delay(amountPropertyValue = "steam-community-market-tracker.rate-limits.market-item-search")
    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 15_000, maxDelay = 60_000, multiplier = 2.0))
    fun retrieveMarketItemsFromSteam(appId: Int, start: Int = 0, count: Int = 100): List<JsonNode> {
        log.debug("Fetching items from steam for app {} (start: {})", appId, start)
        val request = Request.Builder()
            .url("https://steamcommunity.com/market/search/render/?appid=${appId}&norender=1&start=${start}&count=${count}")
            .build()
        proxyClient.newCall(request).execute().use {
            if (!it.isSuccessful)
                throw HttpFetchException(it.code, "Failed to fetch url ${request.url} with code ${it.code}")
            return objectMapper
                .readTree(it.body.string())
                .get("results")
                .map {
                    objectMapper.createObjectNode().apply {
                        put("app-id", appId)
                        put("name", it.get("hash_name")?.asText()!!)
                        put("listings", it.get("sell_listings")?.asInt())
                        it.get("sell_price_text")?.let { jsonNode ->
                            val price = parseCurrencyToNumber(jsonNode.asText())
                            put("listings-price", price.first?.toDouble())
                            put("currency", price.second)
                        }
                    }
                }
        }
    }

    @Synchronized
    @Delay(amountPropertyValue = "steam-community-market-tracker.rate-limits.market-inventory-search")
    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 60_000, maxDelay = 960_000, multiplier = 4.0))
    fun retrieveInventory(appId: Int, userId: Long, count: Int = 2000, language: String = "english"): List<JsonNode> {
        log.debug("Fetching inventory from steam for user {} and app {}", userId, appId)
        val request = Request.Builder()
            .url("https://steamcommunity.com/inventory/$userId/$appId/2?l=$language&count=$count")
            .build()
        proxyClient.newCall(request).execute().use {
            if (!it.isSuccessful)
                throw HttpFetchException(it.code, "Failed to fetch url ${request.url} with code ${it.code}")
            val payload = objectMapper.readTree(it.body.string())
            val assets = payload.get("assets").elements().asSequence().associateBy { e -> e.get("classid").asLong() to e.get("instanceid").asLong() }
            val descriptions = payload.get("descriptions").elements().asSequence().associateBy { e -> e.get("classid").asLong() to e.get("instanceid").asLong() }
            val keys = HashSet<Pair<Long, Long>>().apply {
                addAll(assets.keys)
                addAll(descriptions.keys)
            }
            val pairs = keys.stream().map { key -> Pair(assets.get(key), descriptions.get(key)) }
            return pairs.map { pair ->
                objectMapper.createObjectNode().apply {
                    put("app-id", pair.first?.get("appid")?.asInt())
                    put("context-id", pair.first?.get("contextid")?.asInt())
                    put("asset-id", pair.first?.get("assetid")?.asLong())
                    put("class-id", pair.first?.get("classid")?.asLong())
                    put("instance-id", pair.first?.get("instanceid")?.asLong())
                    put("amount", pair.first?.get("amount")?.asInt())
                    put("name", pair.second?.get("market_hash_name")?.asText())
                    put("marketable", pair.second?.get("marketable")?.asInt())
                    put("tradable", pair.second?.get("tradable")?.asInt())
                }
            }.toList()
        }
    }

}