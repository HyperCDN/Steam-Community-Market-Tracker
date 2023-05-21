package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.UserInventory
import de.hypercdn.scmt.entities.sql.entities.UserInventoryItemSnapshot
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.*

interface UserInventoryItemSnapshotRepository : CrudRepository<UserInventoryItemSnapshot, UUID> {

    @Query(
        """
        FROM UserInventoryItemSnapshot snapshot
        WHERE snapshot.userInventory = :inventory
            AND ((cast(:stateOf as localdatetime) is null AND snapshot.superseded is null)
                OR (cast(:stateOf as localdatetime) is not null AND (snapshot.superseded <= cast(:stateOf as localdatetime) 
                                                                    OR (snapshot.superseded is null AND snapshot.createdAt <= cast(:stateOf as localdatetime)))))
    """
    )
    fun getItemsFor(
        @Param("inventory") inventory: UserInventory,
        @Param("stateOf") stateOf: OffsetDateTime? = null,
        pageable: Pageable = Pageable.unpaged()
    ): List<UserInventoryItemSnapshot>

}