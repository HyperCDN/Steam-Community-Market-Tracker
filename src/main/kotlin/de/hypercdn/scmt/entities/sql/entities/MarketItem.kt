package de.hypercdn.scmt.entities.sql.entities

import jakarta.persistence.*
import lombok.NoArgsConstructor
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import java.io.Serializable
import java.time.OffsetDateTime

@Entity
@IdClass(MarketItem.Key::class)
@Table(name = "market_items")
@DynamicInsert
@DynamicUpdate
class MarketItem {

    @NoArgsConstructor
    class Key(
        var appId: Int,
        var name: String
    ): Serializable

    @Id
    @Column(
        name = "app_id",
        nullable = false,
        updatable = false
    )
    var appId: Int = -1

    @PrimaryKeyJoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "app_id",
        referencedColumnName = "app_id",
        insertable = false,
        updatable = false
    )
    lateinit var app: App

    @Id
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

}