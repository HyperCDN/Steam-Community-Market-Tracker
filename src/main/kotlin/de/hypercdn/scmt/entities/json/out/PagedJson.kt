package de.hypercdn.scmt.entities.json.out

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.Pageable

class PagedJson<T> {

    constructor(entities: List<T>? = null) {
        this.entities = entities
    }

    constructor(paged: Pageable, entities: List<T>? = null) {
        if (paged.isUnpaged) return
        if (paged.isPaged) {
            this.page = paged.pageNumber
            this.size = paged.pageSize
        }
        this.entities = entities
    }

    @JsonProperty("page")
    var page: Int? = null

    @JsonProperty("count")
    var size: Int? = null

    @JsonProperty("entities")
    var entities: List<T>? = null

}