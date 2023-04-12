package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.UserInventory
import de.hypercdn.scmt.entities.sql.entities.UserInventoryItemSnapshot
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface UserInventoryItemSnapshotRepository : CrudRepository<UserInventoryItemSnapshot, UUID> {

    @Query(
        """
        FROM UserInventoryItemSnapshot snapshot
        WHERE snapshot.userInventory = :inventory
            AND snapshot.superseded is null
    """
    )
    fun getItemsCurrentlyInUserInventory(
        @Param("inventory") inventory: UserInventory
    ): List<UserInventoryItemSnapshot>

}