package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.InventoryItem
import de.hypercdn.scmt.entities.sql.entities.UserInventory
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface InventoryItemRepository : CrudRepository<InventoryItem, UUID> {

    @Query(
        """
        FROM InventoryItem item
        WHERE item.userInventory = :inventory
            AND item.superseded is null
    """
    )
    fun getItemsCurrentlyInUserInventory(
        @Param("inventory") inventory: UserInventory
    )

}