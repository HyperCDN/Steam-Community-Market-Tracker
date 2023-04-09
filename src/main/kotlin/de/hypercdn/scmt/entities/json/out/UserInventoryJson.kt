package de.hypercdn.scmt.entities.json.out

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.hypercdn.scmt.entities.sql.entities.App
import de.hypercdn.scmt.entities.sql.entities.UserInventory
import java.time.OffsetDateTime

class UserInventoryJson(
    @JsonIgnore
    val userInventory: UserInventory? = null
) {

    @JsonProperty("app")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var app: AppJson? = null

    @JsonProperty("user-id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var userId: Long? = null

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

    fun includeApp(skip: Boolean = false, appProvider: ((app: App) -> AppJson?)? = null): UserInventoryJson {
        if (userInventory == null || skip) return this
        this.app = appProvider?.invoke(userInventory.app)
        return this
    }

    fun includeUserId(skip: Boolean = false): UserInventoryJson {
        if (userInventory == null || skip) return this
        this.userId = userInventory.userId
        return this
    }

    fun includeProperties(skip: Boolean = false): UserInventoryJson {
        if (userInventory == null || skip) return this
        this.properties = Properties().apply {
            tracked = userInventory.tracked
            lastItemScan = userInventory.lastItemScan
        }
        return this
    }

}