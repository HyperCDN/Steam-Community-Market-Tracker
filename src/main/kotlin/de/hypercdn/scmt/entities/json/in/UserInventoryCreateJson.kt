package de.hypercdn.scmt.entities.json.`in`

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

class UserInventoryCreateJson {

    @JsonProperty("properties", required = true)
    var userId: Long = -1

    @JsonProperty("properties")
    var properties: Properties? = null

    class Properties {

        @JsonProperty("is-tracked")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var tracked: Boolean? = null

    }

}