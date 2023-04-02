package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.App
import de.hypercdn.scmt.entities.sql.entities.MarketItem
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
    """
    )
    fun getAllMarketItemsByApp(@Param("app") app: App): List<MarketItem>

    @Query(
        """
        FROM MarketItem item
        WHERE item.app = :app
            AND item.tracked = true
    """
    )
    fun getAllTrackedMarketItemsByApp(@Param("app") app: App): List<MarketItem>

    @Query(
        """
        FROM MarketItem item
        WHERE item.app = :app
            AND item.tracked = true
            AND item.lastItemScan is null OR item.lastItemScan + :scanDelayDuration < NOW()
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

}