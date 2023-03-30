package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

class StatisticsJson {

    @JsonProperty("min")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var min: Value? = null

    @JsonProperty("avg")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var avg: Value? = null

    @JsonProperty("max")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var max: Value? = null

    class Value {
        @JsonProperty("v")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var value: Double? = null

        @JsonProperty("t")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var timestamp: LocalDateTime? = null
    }

    companion object {

        fun from(input: List<List<Any>>): StatisticsJson {
            val map = input.associateBy(keySelector = {
                it.get(0) as String
            }, valueTransform = {
                Pair(it.get(1) as Double?, it.get(2) as LocalDateTime?)
            })
            return StatisticsJson().apply {
                min = Value().apply {
                    value = map.get("min")?.first
                    timestamp = map.get("min")?.second
                }
                avg = Value().apply {
                    value = map.get("avg")?.first
                }
                max = Value().apply {
                    value = map.get("max")?.first
                    timestamp = map.get("max")?.second
                }
            }
        }

    }

}