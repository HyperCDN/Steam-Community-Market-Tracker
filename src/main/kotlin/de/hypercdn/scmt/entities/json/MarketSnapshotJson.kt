package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.entities.MarketSnapshot
import java.time.OffsetDateTime

class MarketSnapshotJson(
    @JsonIgnore
    val snapshot: MarketSnapshot? = null
) {

    @JsonProperty("item")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var item: MarketItemJson? = null

    @JsonProperty("properties")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var properties: Properties? = null

    class Properties {

        @JsonProperty("timestamp")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var timestamp: OffsetDateTime? = null

    }

    @JsonProperty("availability")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var availability: MarketItemAvailabilityJson? = null

    class MarketItemAvailabilityJson {

        @JsonProperty("listings")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var listings: Int? = null

        @JsonProperty("volume")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var volume: Int? = null

    }

    @JsonProperty("price")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var price: MarketItemPriceJson? = null

    class MarketItemPriceJson {

        @JsonProperty("min")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var lowestPrice: Float? = null

        @JsonProperty("median")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var medianPrice: Float? = null

        @JsonProperty("listing")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var listingPrice: Float? = null

        @JsonProperty("currency")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var currency: String? = null

    }

    fun includeItem(skip: Boolean = false, itemProvider: ((item: MarketItem) -> MarketItemJson?)? = null): MarketSnapshotJson {
        if (snapshot == null || skip) return this
        this.item = itemProvider?.invoke(snapshot.marketItem)
        return this
    }

    fun includeProperties(skip: Boolean = false): MarketSnapshotJson {
        if (snapshot == null || skip) return this
        this.properties = Properties().apply {
            timestamp = snapshot.createdAt
        }
        return this
    }

    fun includeAvailability(skip: Boolean = false): MarketSnapshotJson {
        if (snapshot == null || skip) return this
        this.availability = MarketItemAvailabilityJson().apply {
            listings = snapshot.stats.listings
            volume = snapshot.stats.volume
        }
        return this
    }

    fun includePrice(skip: Boolean = false): MarketSnapshotJson {
        if (snapshot == null || skip) return this
        this.price = MarketItemPriceJson().apply {
            lowestPrice = snapshot.price.lowestPrice
            medianPrice = snapshot.price.medianPrice
            listingPrice = snapshot.price.listingPrice
            currency = snapshot.price.currency
        }
        return this
    }

}