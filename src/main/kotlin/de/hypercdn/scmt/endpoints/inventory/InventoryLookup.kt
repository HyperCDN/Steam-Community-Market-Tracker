package de.hypercdn.scmt.endpoints.inventory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class InventoryLookup @Autowired constructor(

) {

    @GetMapping("/lookup/inventory")
    fun getInfoForInventory() {

    }

}