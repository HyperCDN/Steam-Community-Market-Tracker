package de.hypercdn.scmt.entities.sql.entities

import jakarta.persistence.*
import jakarta.persistence.Table
import org.hibernate.annotations.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "market_items")
@DynamicInsert
@DynamicUpdate
class MarketItem {

    @Id
    @Column(
        name = "market_item_uuid",
        nullable = false,
        updatable = false
    )
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    lateinit var __uuid: UUID

    @Column(
        name = "app_uuid",
        nullable = false,
        updatable = false
    )
    lateinit var appUUID: UUID

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "app_uuid",
        referencedColumnName = "app_uuid",
        insertable = false,
        updatable = false
    )
    lateinit var app: App

    @Column(
        name = "context_id",
    )
    var contextId: Long? = null

    @Column(
        name = "asset_id",
    )
    var assetId: Long? = null

    @Column(
        name = "market_hash_name",
        nullable = false,
        updatable = false
    )
    lateinit var name: String

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    @ColumnDefault("NOW()")
    @CreationTimestamp
    lateinit var createdAt: OffsetDateTime

    @Column(
        name = "tracked",
        nullable = false
    )
    @ColumnDefault("FALSE")
    var tracked: Boolean = false

    @Column(
        name = "last_item_scan",
        nullable = false
    )
    @ColumnDefault("NULL")
    var lastItemScan: OffsetDateTime? = null

}