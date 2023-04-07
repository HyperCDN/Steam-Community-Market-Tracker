package de.hypercdn.scmt.entities.json

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class StatisticsJson<T> {

    @JsonProperty("min")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var min: T? = null

    @JsonProperty("min-timestamp")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var minTimestamp: OffsetDateTime? = null

    @JsonProperty("avg")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var avg: T? = null

    @JsonProperty("max")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var max: T? = null

    @JsonProperty("max-timestamp")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var maxTimestamp: OffsetDateTime? = null

    companion object {

        fun <T : Any> from(input: List<List<Any>>): StatisticsJson<T> {
            if (input.isEmpty()) return StatisticsJson()
            val map = input.associateBy(keySelector = {
                it.get(0) as String
            }, valueTransform = {
                Pair(it.get(1) as T?, if (it.get(2) != null) OffsetDateTime.of(it.get(2) as LocalDateTime?, ZoneOffset.UTC) else null)
            })
            return StatisticsJson<T>().apply {
                min = map.get("min")?.first
                minTimestamp = map.get("min")?.second
                avg = map.get("avg")?.first
                max = map.get("max")?.first
                maxTimestamp = map.get("max")?.second
            }
        }

    }

}