package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.App
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.Duration
import java.util.*

interface AppRepository : CrudRepository<App, UUID> {

    @Query("""
        FROM App app
        WHERE (:tracked is null OR app.tracked = :tracked)
    """)
    fun findAll(
        @Param("tracked") tracked: Boolean? = null,
        pageable: Pageable = Pageable.unpaged()
    ): List<App>

    @Query(
        """
        FROM App app
        WHERE app.tracked = true
            AND (app.lastItemScan is null 
                OR app.lastItemScan < (Now() - :scanDelayDuration))
    """
    )
    fun getAllAppsDueToItemScan(
        @Param("scanDelayDuration") scanDelayDuration: Duration
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

    @Modifying
    @Query(
        """
        DELETE FROM App app
            WHERE app.id in :appIds
    """
    )
    fun deleteAppByAppIds(
        @Param("appIds") appIds: Collection<Int>
    ): Int

    @Query("""
        SELECT 
            SUM(1),
            SUM(case when app.tracked = true then 1 else 0 end),
            SUM(case when app.tracked = false then 1 else 0 end)
        FROM App app
    """)
    fun getGlobalStatisticCounts(): Array<Int>

}