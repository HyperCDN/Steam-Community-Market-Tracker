package de.hypercdn.scmt.entities.sql.entities

import jakarta.persistence.*
import jakarta.persistence.Table
import org.hibernate.annotations.*

import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "market_snapshots")
@DynamicInsert
@DynamicUpdate
class MarketSnapshot {

    @Id
    @Column(
        name = "market_snapshot_id",
        nullable = false,
        updatable = false
    )
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    lateinit var id: UUID

    @Column(
        name = "app_id",
        nullable = false,
        updatable = false
    )
    var appId: Int = -1

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "app_id",
        referencedColumnName = "app_id",
        insertable = false,
        updatable = false
    )
    lateinit var app: App

    @Column(
        name = "market_hash_name",
        nullable = false,
        updatable = false
    )
    lateinit var name: String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = [
        JoinColumn(
            name = "app_id",
            referencedColumnName = "app_id",
            updatable = false,
            insertable = false
        ),
        JoinColumn(
            name = "market_hash_name",
            referencedColumnName = "market_hash_name",
            updatable = false,
            insertable = false
        )
    ])
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

    class Price {

        @Column(
            name = "lowest_price",
            updatable = false
        )
        var lowestPrice: Float? = null

        @Column(
            name = "median_price",
            updatable = false
        )
        var medianPrice: Float? = null

        @Column(
            name = "listing_price",
            updatable = false
        )
        var listingPrice: Float? = null

        @Column(
            name = "currency",
            updatable = false
        )
        var currency: String? = null

    }

}