package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.hypercdn.scmt.entities.sql.entities.MarketSnapshot
import java.time.OffsetDateTime

class MarketSnapshotJson(
    @JsonIgnore
    var snapshot: MarketSnapshot
) {

    @JsonProperty("timestamp")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    var timestamp: OffsetDateTime? = null

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

    init {
        timestamp = snapshot.createdAt
        availability = MarketItemAvailabilityJson().apply {
            listings = snapshot.stats.listings
            volume = snapshot.stats.volume
        }
        price = MarketItemPriceJson().apply {
            lowestPrice = snapshot.price.lowestPrice
            medianPrice = snapshot.price.medianPrice
            listingPrice = snapshot.price.listingPrice
            currency = snapshot.price.currency
        }
    }

}