package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.App
import de.hypercdn.scmt.entities.sql.entities.UserInventory
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.Duration
import java.util.*

interface UserInventoryRepository : CrudRepository<UserInventory, UUID> {

    @Query(
        """
        FROM UserInventory inventory
        WHERE inventory.app = :app
    """
    )
    fun findUserInventoriesByApp(
        @Param("app") app: App,
        pageable: Pageable = Pageable.unpaged()
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


    @Query(
        """
        FROM UserInventory inventory
        WHERE inventory.tracked = true
            AND inventory.lastItemScan is null OR inventory.lastItemScan < (NOW() - :scanDelayDuration)
    """
    )
    fun getInventoriesDueToItemScan(
        @Param("scanDelayDuration") scanDelayDuration: Duration
    ): List<UserInventory>

    @Query("""
        SELECT 
            SUM(1),
            SUM(case when inv.tracked = true then 1 else 0 end),
            SUM(case when inv.tracked = false then 1 else 0 end)
        FROM UserInventory inv
    """)
    fun getGlobalStatisticCounts(): Any

}