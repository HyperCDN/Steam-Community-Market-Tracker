package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.hypercdn.scmt.entities.sql.entities.InventoryItem
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.entities.UserInventory
import java.time.OffsetDateTime
import java.util.*

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

    @JsonProperty("identity")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var identity: Identity? = null

    class Identity {

        @JsonProperty("context-id")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var contextId: Long? = null

        @JsonProperty("asset-id")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var assetId: Long? = null

        @JsonProperty("class-id")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var classId: Long? = null

        @JsonProperty("instance-id")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var instanceId: Long? = null

    }


    @JsonProperty("properties")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var properties: Properties? = null

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

    @JsonProperty("last-snapshot")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var snapshot: MarketSnapshotJson? = null

    fun includeInventory(skip: Boolean = false, inventoryProvider: ((app: UserInventory) -> UserInventoryJson?)? = null): InventoryItemJson {
        if (inventoryItem == null || skip) return this
        inventory = inventoryProvider?.invoke(inventoryItem.userInventory)
        return this
    }

    fun includeItem(skip: Boolean = false, itemProvider: ((item: MarketItem) -> MarketItemJson?)? = null): InventoryItemJson {
        if (inventoryItem == null || skip) return this
        item = itemProvider?.invoke(inventoryItem.marketItem)
        return this
    }

    fun includeIdentity(skip: Boolean = false): InventoryItemJson {
        if (inventoryItem == null || skip) return this
        identity = Identity().apply {
            contextId = inventoryItem.identity.contextId
            assetId = inventoryItem.identity.assetId
            classId = inventoryItem.identity.classId
            instanceId = inventoryItem.identity.instanceId
        }
        return this
    }

    fun includeProperties(skip: Boolean = false): InventoryItemJson {
        if (inventoryItem == null || skip) return this
        properties = Properties().apply {
            amount = inventoryItem.amount
            createdAt = inventoryItem.createdAt
            superseded = inventoryItem.superseded
        }
        return this
    }

    fun includeSnapshot(skip: Boolean = false, snapshotProvider: ((marketItemUUID: UUID) -> MarketSnapshotJson?)?): InventoryItemJson {
        if (inventoryItem == null || skip) return this
        snapshot = snapshotProvider?.invoke(inventoryItem.marketItemUUID)
        return this
    }


}