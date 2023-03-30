package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.entities.MarketSnapshot
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface MarketSnapshotRepository: CrudRepository<MarketSnapshot, UUID> {

    @Query(
        """
        FROM MarketSnapshot snapshot
        WHERE snapshot.marketItem = :item
        ORDER BY snapshot.createdAt DESC
        LIMIT 1
    """
    )
    fun getLatestFor(
        @Param("item") item: MarketItem
    ): MarketSnapshot

    @Query(
        """
        FROM MarketSnapshot snapshot
        WHERE snapshot.marketItem = :item
        ORDER BY snapshot.createdAt DESC
    """
    )
    fun getAllFor(
        @Param("item") item: MarketItem
    ): List<MarketSnapshot>

    @Query(
        """
        SELECT "min", cast(snapshot.price.lowestPrice as double), cast(snapshot.createdAt as localdatetime) FROM MarketSnapshot snapshot WHERE snapshot.price.lowestPrice = (SELECT MIN(inner_.price.lowestPrice) FROM MarketSnapshot inner_)
        UNION
        SELECT "avg", cast(AVG(snapshot.price.lowestPrice) as double), cast(null as localdatetime) FROM MarketSnapshot snapshot
        UNION 
        SELECT "max", cast(snapshot.price.lowestPrice as double), cast(snapshot.createdAt as localdatetime) FROM MarketSnapshot snapshot WHERE snapshot.price.lowestPrice = (SELECT MAX(inner_.price.lowestPrice) FROM MarketSnapshot inner_)
    """
    )
    fun getStatisticsBetween(
        @Param("item") item: MarketItem,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
    ): List<List<Any>>

}