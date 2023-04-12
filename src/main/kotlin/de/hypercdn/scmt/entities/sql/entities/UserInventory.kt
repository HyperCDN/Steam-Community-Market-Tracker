package de.hypercdn.scmt.entities.sql.entities

import jakarta.persistence.*
import jakarta.persistence.Table
import org.hibernate.annotations.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "user_inventories")
@DynamicInsert
@DynamicUpdate
class UserInventory {

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
        name = "user_id",
        nullable = false,
        updatable = false
    )
    var userId: Long = -1

    @Column(
        name = "app_uuid",
        nullable = false,
        updatable = false
    )
    lateinit var appUUID: UUID

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "app_uuid",
        referencedColumnName = "__uuid",
        insertable = false,
        updatable = false
    )
    lateinit var app: App

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
    lateinit var lastItemScan: OffsetDateTime

}