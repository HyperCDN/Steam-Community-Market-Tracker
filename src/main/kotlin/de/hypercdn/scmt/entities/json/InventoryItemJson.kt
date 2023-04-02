package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.hypercdn.scmt.entities.sql.entities.InventoryItem
import java.time.OffsetDateTime

class InventoryItemJson(
    @JsonIgnore
    val inventoryItem: InventoryItem? = null
) {

    @JsonProperty("inventory")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var inventory: UserInventoryJson? = null

    @JsonProperty("item")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var item: MarketItemJson? = null

    class Properties {

        @JsonProperty("amount")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var amount: Int? = null

        @JsonProperty("added")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var createdAt: OffsetDateTime? = null

        @JsonProperty("removed")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var superseded: OffsetDateTime? = null

    }


}