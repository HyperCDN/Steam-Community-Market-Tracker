package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.hypercdn.scmt.entities.sql.entities.App
import java.time.OffsetDateTime

class AppJson (
    @JsonIgnore
    val app: App? = null
) {

    @JsonProperty("app_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var id: Int? = null

    @JsonProperty("app_name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var name: String? = null

    @JsonProperty("properties")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var properties: Properties? = null

    class Properties {

        @JsonProperty("is_tracked")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var tracked: Boolean? = null

        @JsonProperty("last_item_scan")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var lastItemScan: OffsetDateTime? = null

    }

    fun includeId(skip: Boolean = false): AppJson {
        if (app == null || skip) return this
        this.id = app.id
        return this
    }

    fun includeName(skip: Boolean = false): AppJson {
        if (app == null || skip) return this
        this.name = app.name
        return this
    }

    fun includeProperties(skip: Boolean = false): AppJson {
        if (app == null || skip) return this
        this.properties = Properties().apply {
            tracked = app.tracked
            lastItemScan = app.lastItemScan
        }
        return this
    }

}