package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.App
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.Duration
import java.util.*

interface MarketItemRepository : CrudRepository<MarketItem, UUID> {

    @Query(
        """
        FROM MarketItem item
        WHERE item.app = :app
            AND (:tracked is null 
                OR (item.tracked = :tracked AND item.app.tracked = :tracked))
    """
    )
    fun getAllMarketItemsByApp(
        @Param("app") app: App,
        @Param("tracked") tracked: Boolean? = null,
        pageable: Pageable = Pageable.unpaged()
    ): List<MarketItem>

    @Query(
        """
        FROM MarketItem item
        WHERE item.app = :app
            AND item.app.tracked = true
            AND item.tracked = true
            AND item.lastItemScan is null OR item.lastItemScan < (NOW() - :scanDelayDuration)
    """
    )
    fun getMarketItemsDueToItemScan(
        @Param("app") app: App,
        @Param("scanDelayDuration") scanDelayDuration: Duration
    ): List<MarketItem>

    @Query(
        """
        FROM MarketItem item
        WHERE item.app = :app
            AND item.name = :name
    """
    )
    fun findMarketItemByAppAndName(
        @Param("app") app: App,
        @Param("name") name: String
    ): MarketItem?

    @Query("""
        SELECT 
            SUM(1),
            SUM(case when item.tracked = true then 1 else 0 end),
            SUM(case when item.tracked = false then 1 else 0 end)
        FROM MarketItem item
    """)
    fun getGlobalStatisticCounts(): Any

    @Query("""
        SELECT 
            SUM(1),
            SUM(case when item.tracked = true then 1 else 0 end),
            SUM(case when item.tracked = false then 1 else 0 end)
        FROM MarketItem item
            where item.app = :app
    """)
    fun getStatisticCounts(
        @Param("app") app: App
    ): Any

}