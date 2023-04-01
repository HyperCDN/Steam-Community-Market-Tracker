package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.hypercdn.scmt.entities.sql.entities.UserInventory

class UserInventoryJson(
    val userInventory: UserInventory
) {

    @JsonProperty("app")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var app: AppJson? = null

    @JsonProperty("user_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var userId: Long? = null

    @JsonProperty("is_tracked")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var tracked: Boolean? = null


}