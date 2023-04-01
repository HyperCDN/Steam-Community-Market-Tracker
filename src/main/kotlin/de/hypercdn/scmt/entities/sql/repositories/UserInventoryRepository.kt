package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.App
import de.hypercdn.scmt.entities.sql.entities.UserInventory
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.*

interface UserInventoryRepository : CrudRepository<UserInventory, UUID> {

    @Query(
        """
        FROM UserInventory inventory
        WHERE inventory.app = :app
            AND inventory.tracked = true
            AND inventory.lastItemScan < :lastScanBefore
    """
    )
    fun findUserInventoriesDueToItemScanByApp(
        @Param("app") app: App,
        @Param("lastScanBefore") lastScanBefore: OffsetDateTime
    ): List<UserInventory>

    @Query(
        """
        FROM UserInventory inventory
        WHERE inventory.app = :app
            AND inventory.userId = :userId
    """
    )
    fun findUserInventoryByAppAndUserId(
        @Param("app") app: App,
        @Param("userId") userId: Long
    ): UserInventory?

}