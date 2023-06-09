package de.hypercdn.scmt.entities.sql.entities

import jakarta.persistence.*
import jakarta.persistence.Table
import org.hibernate.annotations.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "market_item_snapshots")
@DynamicInsert
@DynamicUpdate
class MarketItemSnapshot {

    @Id
    @Column(
        name = "__uuid",
        nullable = false,
        updatable = false
    )
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    lateinit var __uuid: UUID

    @Column(
        name = "market_item_uuid",
        nullable = false,
        updatable = false
    )
    lateinit var marketItemUUID: UUID

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "market_item_uuid",
        referencedColumnName = "__uuid",
        updatable = false,
        insertable = false
    )
    lateinit var marketItem: MarketItem

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    @ColumnDefault("NOW()")
    @CreationTimestamp
    lateinit var createdAt: OffsetDateTime

    @Embedded
    lateinit var stats: Stats

    @Embeddable
    class Stats {

        @Column(
            name = "volume",
            updatable = false
        )
        var volume: Int? = null

        @Column(
            name = "listings",
            updatable = false
        )
        var listings: Int? = null
    }

    @Embedded
    lateinit var price: Price

    @Embeddable
    class Price {

        @Column(
            name = "lowest_price",
            updatable = false
        )
        var lowestPrice: Double? = null

        @Column(
            name = "median_price",
            updatable = false
        )
        var medianPrice: Double? = null

        @Column(
            name = "listing_price",
            updatable = false
        )
        var listingPrice: Double? = null

        @Column(
            name = "currency",
            updatable = false
        )
        var currency: String? = null

    }

}