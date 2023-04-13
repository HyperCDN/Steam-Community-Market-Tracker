package de.hypercdn.scmt.entities.json.out

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.hypercdn.scmt.entities.sql.entities.App
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import java.time.OffsetDateTime

class MarketItemJson (
    @JsonIgnore
    val marketItem: MarketItem? = null
) {

    @JsonProperty("app")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var app: AppJson? = null

    @JsonProperty("market-hash-name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var name: String? = null

    @JsonProperty("properties")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var properties: Properties? = null

    class Properties {

        @JsonProperty("is-tracked")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var tracked: Boolean? = null

        @JsonProperty("last-item-scan")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var lastItemScan: OffsetDateTime? = null

    }

    @JsonProperty("last-snapshot")
    var lastSnapshot: MarketItemSnapshotJson? = null

    fun includeApp(skip: Boolean = false, appProvider: ((app: App) -> AppJson?)? = null): MarketItemJson {
        if (marketItem == null || skip) return this
        this.app = appProvider?.invoke(marketItem.app)
        return this
    }

    fun includeName(skip: Boolean = false): MarketItemJson {
        if (marketItem == null || skip) return this
        name = marketItem.name
        return this
    }

    fun includeProperties(skip: Boolean = false): MarketItemJson {
        if (marketItem == null || skip) return this
        properties = Properties().apply {
            tracked = marketItem.tracked
            lastItemScan = marketItem.lastItemScan
        }
        return this
    }

    fun includeSnapshot(skip: Boolean = false, snapshotProvider: () -> MarketItemSnapshotJson?): MarketItemJson {
        if (marketItem == null || skip) return this
        lastSnapshot = snapshotProvider.invoke()
        return this
    }

}