package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.App
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.*

interface AppRepository : CrudRepository<App, UUID> {

    @Query(
        """
        FROM App app
        WHERE app.tracked = true
        AND app.lastItemScan is null OR app.lastItemScan < :lastScanBefore
    """
    )
    fun getAllAppsDueToItemScan(
        @Param("lastScanBefore") lastScanBefore: OffsetDateTime
    ): List<App>

    @Query(
        """
        FROM App app
        WHERE app.id = :appId
    """
    )
    fun findAppByAppId(
        @Param("appId") appId: Int
    ): App?

}