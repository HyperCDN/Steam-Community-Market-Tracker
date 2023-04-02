package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.App
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.Duration
import java.util.*

interface AppRepository : CrudRepository<App, UUID> {

    @Query(
        """
        FROM App app
        WHERE app.tracked = true
        AND app.lastItemScan is null OR app.lastItemScan + :scanDelayDuration < NOW()
    """
    )
    fun getAllAppsDueToItemScan(
        @Param("scanDelayDuration") scanDelayDuration: Duration
    ): List<App>

    @Query(
        """
        FROM App app
        WHERE app.tracked = true
    """
    )
    fun getAllTrackedApps(): List<App>

    @Query(
        """
        FROM App app
        WHERE app.id = :appId
    """
    )
    fun findAppByAppId(
        @Param("appId") appId: Int
    ): App?

    @Query(
        """
        DELETE FROM App app
            WHERE app.id in :appIds
    """
    )
    fun deleteAppByAppIds(
        @Param("appIds") appIds: Collection<Int>
    )

}