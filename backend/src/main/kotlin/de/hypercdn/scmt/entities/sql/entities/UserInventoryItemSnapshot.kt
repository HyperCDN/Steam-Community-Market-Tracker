package de.hypercdn.scmt.entities.sql.entities

import jakarta.persistence.*
import jakarta.persistence.Table
import org.hibernate.annotations.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "user_inventory_item_snapshots")
@DynamicInsert
@DynamicUpdate
class UserInventoryItemSnapshot {

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
        name = "user_inventory_uuid",
        nullable = false,
        updatable = false
    )
    lateinit var userInventoryUUID: UUID

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "user_inventory_uuid",
        referencedColumnName = "__uuid",
        insertable = false,
        updatable = false
    )
    lateinit var userInventory: UserInventory

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
        insertable = false,
        updatable = false
    )
    lateinit var marketItem: MarketItem


    @Embedded
    lateinit var identity: Identity

    @Embeddable
    data class Identity(
        @Column(
            name = "context_id",
            nullable = false,
            updatable = false
        )
        var contextId: Long,

        @Column(
            name = "asset_id",
            nullable = false,
            updatable = false
        )
        var assetId: Long,

        @Column(
            name = "class_id",
            nullable = false,
            updatable = false
        )
        var classId: Long,

        @Column(
            name = "instance_id",
            nullable = false,
            updatable = false
        )
        var instanceId: Long,
    )

    @Column(
        name = "amount",
        nullable = false,
        updatable = false
    )
    var amount: Int = 0

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    @ColumnDefault("NOW()")
    @CreationTimestamp
    lateinit var createdAt: OffsetDateTime

    @Column(
        name = "superseded",
        nullable = false
    )
    @ColumnDefault("NULL")
    var superseded: OffsetDateTime? = null

    @Column(
        name = "automatic_fetched",
        nullable = false,
        updatable = false
    )
    var automaticFetched: Boolean = true

}