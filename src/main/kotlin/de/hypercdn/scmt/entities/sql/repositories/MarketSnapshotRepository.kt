package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.entities.MarketSnapshot
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.*

interface MarketSnapshotRepository: CrudRepository<MarketSnapshot, UUID> {

    @Query(
        """
        FROM MarketSnapshot snapshot
        WHERE snapshot.marketItem = :item
    """
    )
    fun getAllFor(
        @Param("item") item: MarketItem,
        page: Pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))
    ): List<MarketSnapshot>

    @Query(
        """
        SELECT "min", cast(snapshot.price.lowestPrice as double), cast(snapshot.createdAt as localdatetime) FROM MarketSnapshot snapshot 
            WHERE snapshot.marketItem = :item
                AND snapshot.price.lowestPrice = (
                    SELECT MIN(inner_.price.lowestPrice) FROM MarketSnapshot inner_ 
                        WHERE inner_.marketItem = :item
                        AND inner_.createdAt between :endDate and :startDate
                )
        UNION
        SELECT "avg", cast(AVG(snapshot.price.lowestPrice) as double), cast(null as localdatetime) FROM MarketSnapshot snapshot
            WHERE snapshot.marketItem = :item
            AND snapshot.createdAt between :endDate and :startDate
        UNION 
        SELECT "max", cast(snapshot.price.lowestPrice as double), cast(snapshot.createdAt as localdatetime) FROM MarketSnapshot snapshot 
            WHERE snapshot.marketItem = :item
                AND snapshot.price.lowestPrice = (
                    SELECT MAX(inner_.price.lowestPrice) FROM MarketSnapshot inner_
                        WHERE inner_.marketItem = :item
                        AND inner_.createdAt between :endDate and :startDate
                )
    """
    )
    fun getPriceStatisticsForLowestBetween(
        @Param("item") item: MarketItem,
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime,
    ): List<List<Any>>

    @Query(
        """
        SELECT "min", cast(snapshot.price.medianPrice as double), cast(snapshot.createdAt as localdatetime) FROM MarketSnapshot snapshot 
            WHERE snapshot.marketItem = :item
                AND snapshot.price.medianPrice = (
                    SELECT MIN(inner_.price.medianPrice) FROM MarketSnapshot inner_ 
                        WHERE inner_.marketItem = :item
                        AND inner_.createdAt between :endDate and :startDate
                )
        UNION
        SELECT "avg", cast(AVG(snapshot.price.medianPrice) as double), cast(null as localdatetime) FROM MarketSnapshot snapshot
            WHERE snapshot.marketItem = :item
            AND snapshot.createdAt between :endDate and :startDate
        UNION 
        SELECT "max", cast(snapshot.price.medianPrice as double), cast(snapshot.createdAt as localdatetime) FROM MarketSnapshot snapshot 
            WHERE snapshot.marketItem = :item
                AND snapshot.price.medianPrice = (
                    SELECT MAX(inner_.price.medianPrice) FROM MarketSnapshot inner_
                        WHERE inner_.marketItem = :item
                        AND inner_.createdAt between :endDate and :startDate
                )
    """
    )
    fun getPriceStatisticsForMedianBetween(
        @Param("item") item: MarketItem,
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime,
    ): List<List<Any>>

    @Query(
        """
        SELECT "min", cast(snapshot.stats.volume as integer), cast(snapshot.createdAt as localdatetime) FROM MarketSnapshot snapshot 
            WHERE snapshot.marketItem = :item
                AND snapshot.stats.volume = (
                    SELECT MIN(inner_.stats.volume) FROM MarketSnapshot inner_ 
                        WHERE inner_.marketItem = :item
                        AND inner_.createdAt between :endDate and :startDate
                )
        UNION
        SELECT "avg", cast(AVG(snapshot.stats.volume) as integer), cast(null as localdatetime) FROM MarketSnapshot snapshot
            WHERE snapshot.marketItem = :item
            AND snapshot.createdAt between :endDate and :startDate
        UNION 
        SELECT "max", cast(snapshot.stats.volume as integer), cast(snapshot.createdAt as localdatetime) FROM MarketSnapshot snapshot 
            WHERE snapshot.marketItem = :item
                AND snapshot.stats.volume = (
                    SELECT MAX(inner_.stats.volume) FROM MarketSnapshot inner_
                        WHERE inner_.marketItem = :item
                        AND inner_.createdAt between :endDate and :startDate
                )
    """
    )
    fun getVolumeStatisticsBetween(
        @Param("item") item: MarketItem,
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime,
    ): List<List<Any>>

    @Query(
        """
        FROM MarketSnapshot snapshot
        WHERE snapshot.marketItem in :items
            AND snapshot.createdAt = (
                SELECT MAX(inner_.createdAt)
                FROM MarketSnapshot inner_
                WHERE inner_.marketItem = snapshot.marketItem
            )
    """
    )
    fun getLatestFor(
        @Param("items") items: List<MarketItem>
    ): List<MarketSnapshot>

    @Query(
        """
        FROM MarketSnapshot snapshot
        WHERE snapshot.marketItem = :item
            AND snapshot.createdAt = (
                SELECT MAX(inner_.createdAt)
                FROM MarketSnapshot inner_
                WHERE inner_.marketItem = snapshot.marketItem
            )
    """
    )
    fun getLatestFor(
        @Param("item") item: MarketItem
    ): MarketSnapshot?

}