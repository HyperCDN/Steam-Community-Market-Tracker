package de.hypercdn.scmt.endpoints.internal

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.hypercdn.scmt.util.http.SCMTProxiedCallManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProxyCount @Autowired constructor(
    val objectMapper: ObjectMapper,
    val proxyClient: SCMTProxiedCallManager
) {

    @GetMapping("/internal/proxy-count")
    fun getProxyCount(): ResponseEntity<JsonNode> {
        val ratio = proxyClient.providerRatio()
        val ratioJson = objectMapper.createObjectNode()
            .put("enabled", ratio.first)
            .put("disabled", ratio.second)
        val supply = proxyClient.supplyInfo()
        val supplyJson = objectMapper.createArrayNode().apply {
            supply.entries.map {
                add(
                    objectMapper.createObjectNode()
                        .put("proxy", it.key.toString())
                        .put("supplied", it.value)
                )
            }
        }
        val responseJson = objectMapper.createObjectNode().apply {
            set<JsonNode>("ratio", ratioJson)
            set<JsonNode>("supply", supplyJson)
        }
        return ResponseEntity(responseJson, HttpStatus.OK)
    }

}