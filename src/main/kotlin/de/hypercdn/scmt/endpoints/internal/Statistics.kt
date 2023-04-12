package de.hypercdn.scmt.endpoints.internal

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.hypercdn.scmt.entities.sql.repositories.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Statistics @Autowired constructor(
    val objectMapper: ObjectMapper,
    val appRepository: AppRepository,
    val marketItemRepository: MarketItemRepository,
    val userInventoryRepository: UserInventoryRepository,
    val marketItemSnapshotRepository: MarketItemSnapshotRepository,
    val userInventoryItemSnapshotRepository: UserInventoryItemSnapshotRepository
) {

    @GetMapping("/internal/entity-count")
    fun getGlobalEntityStatistic(): ResponseEntity<JsonNode>{
        val appGlobalStatistics = appRepository.getGlobalStatisticCounts()
        val appStatistics = objectMapper.createObjectNode()
            .put("total", appGlobalStatistics[0])
            .put("tracked", appGlobalStatistics[1])
            .put("not-tracked", appGlobalStatistics[2])
        val marketItemGlobalStatistics = marketItemRepository.getGlobalStatisticCounts()
        val marketItemStatistics = objectMapper.createObjectNode()
            .put("total", marketItemGlobalStatistics[0])
            .put("tracked", marketItemGlobalStatistics[1])
            .put("not-tracked", marketItemGlobalStatistics[2])
            .put("snapshots", marketItemSnapshotRepository.count())
        val userInventoryGlobalStatistics = userInventoryRepository.getGlobalStatisticCounts()
        val userInventoryStatistics = objectMapper.createObjectNode()
            .put("total", userInventoryGlobalStatistics[0])
            .put("tracked", userInventoryGlobalStatistics[1])
            .put("not-tracked", userInventoryGlobalStatistics[2])
            .put("snapshots", userInventoryItemSnapshotRepository.count())
        val responseJson = objectMapper.createObjectNode().apply {
            set<JsonNode>("apps", appStatistics)
            set<JsonNode>("items", marketItemStatistics)
            set<JsonNode>("inventories", userInventoryStatistics)
        }
        return ResponseEntity(responseJson, HttpStatus.OK)
    }

}