package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.hypercdn.scmt.entities.sql.entities.App

class AppJson (
    @JsonIgnore
    var app: App
) {

    @JsonProperty("app_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var id: Int? = null

    @JsonProperty("app_name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var name: String? = null

    @JsonProperty("is_tracked")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var tracked: Boolean? = null

    init {
        id = app.id
        name = app.name
        tracked = app.tracked
    }

}