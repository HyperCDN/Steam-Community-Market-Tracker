package de.hypercdn.scmt.util.steam

import com.fasterxml.jackson.databind.ObjectMapper
import de.hypercdn.scmt.config.GenericSCMTConfig
import de.hypercdn.scmt.entities.sql.entities.App
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.entities.MarketSnapshot
import de.hypercdn.scmt.util.data.parseCurrencyToNumber
import de.hypercdn.scmt.util.data.parseNumberWithDecorations
import de.hypercdn.scmt.util.data.sleepWithoutException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

@Component
class SteamFetchService @Autowired constructor(
    var okHttpClient: OkHttpClient,
    var objectMapper: ObjectMapper,
    var genericSCMTConfig: GenericSCMTConfig,
) {

    var log: Logger = LoggerFactory.getLogger(SteamFetchService::class.java)

    fun retrieveAppListFromGithub(): List<App> {
        log.info("Fetching app id list from github")
        val request = Request.Builder()
            .url("https://raw.githubusercontent.com/dgibbs64/SteamCMD-AppID-List/main/steamcmd_appid.json")
            .build()
        okHttpClient.newCall(request).execute().use {
            if (!it.isSuccessful) {
                throw HttpFetchException(it.code, "Failed to fetch url ${request.url}")
            }
            return objectMapper.readTree(it.body?.string()).get("applist").get("apps").map { App().apply {
                id = it.get("appid").asInt()
                name = it.get("name").asText()
                tracked = genericSCMTConfig.entityLookup.app.trackByDefault
            }}
        }
    }

    fun retrievePriceOverviewFromSteam(appId: Int, name: String, currency: Int = genericSCMTConfig.currency): MarketSnapshot? {
        sleepWithoutException(TimeUnit.SECONDS, genericSCMTConfig.marketPriceOverviewDelay.seconds)
        log.info("Fetching price overview from steam for item {} from app {}", name, appId)
        val request = Request.Builder()
            .url("https://steamcommunity.com/market/priceoverview/?appid=${appId}&market_hash_name=${URLEncoder.encode(name, Charsets.UTF_8)}&currency=${currency}")
            .build()
        okHttpClient.newCall(request).execute().use {
            if (!it.isSuccessful) {
                throw HttpFetchException(it.code, "Failed to fetch url ${request.url}")
            }
            val json = objectMapper.readTree(it.body?.string())
            if (json.get("success")?.asBoolean() != true) {
                throw HttpFetchException(it.code, "Body indicated failure for ${request.url}")
            }
            return MarketSnapshot().apply {
                this.appId = appId
                this.name = name
                this.stats = MarketSnapshot.Stats().apply {
                    json.get("volume")?.let { jsonNode ->
                        this.volume = parseNumberWithDecorations(jsonNode.asText()).toInt()
                    }
                }
                this.price = MarketSnapshot.Price().apply {
                    json.get("lowest_price")?.let {jsonNode ->
                        val lowestPrice = parseCurrencyToNumber(jsonNode.asText())
                        this.lowestPrice = lowestPrice.first?.toFloat()
                        lowestPrice.second?.let {currency ->
                            this.currency = currency
                        }
                    }
                    json.get("median_price")?.let {jsonNode ->
                        val medianPrice = parseCurrencyToNumber(jsonNode.asText())
                        this.medianPrice = medianPrice.first?.toFloat()
                        medianPrice.second?.let {currency ->
                            this.currency = currency
                        }
                    }
                }
            }
        }
    }

    fun retrieveAllMarketItemsFromSteam(appId: Int): List<Pair<MarketItem, MarketSnapshot>> {
        val marketItems = ArrayList<Pair<MarketItem, MarketSnapshot>>()
        var start = 0
        while (true) {
            val retrieved = retrieveMarketItemsFromSteam(appId, start)
            start += retrieved.size
            marketItems.addAll(retrieved)
            if (retrieved.isEmpty()) {
                break
            }
        }
        return marketItems
    }

    fun retrieveMarketItemsFromSteam(appId: Int, start: Int = 0, count: Int = 100): List<Pair<MarketItem, MarketSnapshot>>{
        sleepWithoutException(TimeUnit.SECONDS, genericSCMTConfig.marketSearchDelay.seconds)
        log.info("Fetching items from steam for app {} (start: {})", appId, start)
        val request = Request.Builder()
            .url("https://steamcommunity.com/market/search/render/?appid=${appId}&norender=1&start=${start}&count=${count}")
            .build()
        okHttpClient.newCall(request).execute().use {
            if (!it.isSuccessful) {
                throw HttpFetchException(it.code, "Failed to fetch url ${request.url}")
            }
            return objectMapper.readTree(it.body?.string()).get("results").map {
                Pair(
                    MarketItem().apply {
                        this.appId = appId
                        this.name = it.get("hash_name")?.asText()!!
                        this.tracked = genericSCMTConfig.entityLookup.marketItem.trackByDefault
                    },
                    MarketSnapshot().apply {
                        this.appId = appId
                        this.name = it.get("hash_name")?.asText()!!
                        this.stats = MarketSnapshot.Stats().apply {
                            this.listings = it.get("sell_listings")?.asInt()
                        }
                        this.price = MarketSnapshot.Price().apply {
                            it.get("sell_price_text")?.let { jsonNode ->
                                this.listingPrice = parseCurrencyToNumber(jsonNode.asText()).first?.toFloat()
                            }
                        }
                    }
                )
            }
        }
    }

    fun retrieveMarketItemsFrom(appId: Int, userId: Int, count: Int = 5000, language: String = "english") {
        val request = Request.Builder()
            .url("https://steamcommunity.com/inventory/$userId/$appId/2?l=$language&count=$count")
            .build()
        okHttpClient.newCall(request).execute().use {
            if (!it.isSuccessful) {
                throw HttpFetchException(it.code, "Failed to fetch url ${request.url}")
            }
            return objectMapper.readTree(it.body?.string())
        }
    }

    class HttpFetchException(
        var code: Int,
        override var message: String? = "${code}: No Message Provided"
    ) : RuntimeException("${code}: $message")

}