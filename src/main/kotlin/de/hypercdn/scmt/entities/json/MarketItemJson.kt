package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.hypercdn.scmt.entities.sql.entities.MarketItem

class MarketItemJson (
    @JsonIgnore
    val marketItem: MarketItem
) {

    @JsonProperty("app")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var app: AppJson? = null

    @JsonProperty("context_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var contextId: Long? = null

    @JsonProperty("asset_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var assetId: Long? = null

    @JsonProperty("market_hash_name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var name: String? = null

    @JsonProperty("is_tracked")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var tracked: Boolean? = null

    @JsonProperty("snapshots")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var history: List<MarketSnapshotJson>? = null

    @JsonProperty("statistics")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var statistics: HashMap<String, StatisticsJson>? = null

    init {
        app = AppJson(marketItem.app)
        contextId = marketItem.contextId
        assetId = marketItem.assetId
        name = marketItem.name
        tracked = marketItem.tracked
    }

}