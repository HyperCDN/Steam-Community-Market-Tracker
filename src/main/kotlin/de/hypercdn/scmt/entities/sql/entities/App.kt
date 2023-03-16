package de.hypercdn.scmt.entities.sql.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import java.time.OffsetDateTime

@Entity
@Table(name = "apps")
@DynamicInsert
@DynamicUpdate
class App {

    @Id
    @Column(
        name = "app_id",
        nullable = false,
        updatable = false
    )
    var id: Int = -1

    @Column(
        name = "app_name",
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