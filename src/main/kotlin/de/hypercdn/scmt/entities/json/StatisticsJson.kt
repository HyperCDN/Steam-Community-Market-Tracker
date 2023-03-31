package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

class StatisticsJson {

    @JsonProperty("min")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var min: Double? = null

    @JsonProperty("min_t")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var minTimestamp: LocalDateTime? = null

    @JsonProperty("min_c")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var minCurrency: String? = null

    @JsonProperty("avg")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var avg: Double? = null

    @JsonProperty("max")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var max: Double? = null

    @JsonProperty("max_t")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var maxTimestamp: LocalDateTime? = null

    @JsonProperty("max_c")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var maxCurrency: String? = null

    companion object {

        fun from(input: List<List<Any>>): StatisticsJson {
            val map = input.associateBy(keySelector = {
                it.get(0) as String
            }, valueTransform = {
                Triple(it.get(1) as Double?, it.get(2) as LocalDateTime?, it.get(3) as String?)
            })
            return StatisticsJson().apply {
                min = map.get("min")?.first
                minTimestamp = map.get("min")?.second
                minCurrency = map.get("min")?.third
                avg = map.get("avg")?.first
                max = map.get("max")?.first
                maxTimestamp = map.get("max")?.second
                maxCurrency = map.get("max")?.third
            }
        }

    }

}