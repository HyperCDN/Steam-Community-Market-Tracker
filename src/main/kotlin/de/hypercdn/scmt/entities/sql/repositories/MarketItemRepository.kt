package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.App
import de.hypercdn.scmt.entities.sql.entities.MarketItem
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface MarketItemRepository: CrudRepository<MarketItem, MarketItem.Key> {

    @Query("""
        FROM MarketItem item
        WHERE item.app = :app
    """)
    fun getAllMarketItemsByApp(@Param("app") app: App): List<MarketItem>

    @Query("""
        FROM MarketItem item
        WHERE item.app = :app
            AND item.tracked = true
    """)
    fun getAllTrackedMarketItemsByApp(@Param("app") app: App): List<MarketItem>

    @Query("""
        SELECT COUNT(1)
        FROM MarketItem item
        WHERE item.app = :app
    """)
    fun getCountyByApp(@Param("app") app: App): Long

}