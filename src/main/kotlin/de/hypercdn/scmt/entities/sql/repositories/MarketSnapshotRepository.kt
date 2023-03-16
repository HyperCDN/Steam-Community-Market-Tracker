package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.MarketItem
import de.hypercdn.scmt.entities.sql.entities.MarketSnapshot
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface MarketSnapshotRepository: CrudRepository<MarketSnapshot, UUID> {

    @Query("""
        FROM MarketSnapshot snapshot
        WHERE snapshot.marketItem = :item
        ORDER BY snapshot.createdAt DESC
    """)
    fun getAllByAppIdAndName(
        @Param("item") item: MarketItem
    ): List<MarketSnapshot>

}