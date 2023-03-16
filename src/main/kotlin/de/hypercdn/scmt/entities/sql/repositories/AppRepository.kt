package de.hypercdn.scmt.entities.sql.repositories

import de.hypercdn.scmt.entities.sql.entities.App
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface AppRepository: CrudRepository<App, Int> {

    @Query("""
        FROM App app
        WHERE app.tracked = true
        ORDER BY app.id
    """)
    fun getAllTrackedApps(): List<App>

}