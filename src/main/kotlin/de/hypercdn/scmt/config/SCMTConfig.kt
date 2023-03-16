package de.hypercdn.scmt.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.time.Duration

@Configuration
@ConfigurationProperties(prefix = "steam-community-market-tracker")
class GenericSCMTConfig {
    var currency: Int = -1
    var marketSearchDelay: Duration = Duration.ZERO
    var marketPriceOverviewDelay: Duration = Duration.ZERO
    var entityLookup: EntityLookupConfig = EntityLookupConfig()
}

@Configuration
@ConfigurationProperties(prefix = "steam-community-market-tracker.entity-lookup")
class EntityLookupConfig {

    var app: EntityLookupApp = EntityLookupApp()
    class EntityLookupApp {
        var onStartup: Boolean = false
        var onCron: Boolean = false
        var cron: String = Scheduled.CRON_DISABLED
        var trackByDefault: Boolean = false
        var deleteNotFoundEntities: Boolean = false
    }

    var marketItem: EntityLookupMarketItem = EntityLookupMarketItem()
    class EntityLookupMarketItem {
        var onStartup: Boolean = false
        var onCron: Boolean = false
        var cron: String = Scheduled.CRON_DISABLED
        var trackByDefault: Boolean = false
        var disableTrackingOnEmptyResponse: Boolean = false
        var deleteNotFoundEntities: Boolean = false
    }

    var marketItemPriceOverview: EntityLookupMarketItemPriceOverview = EntityLookupMarketItemPriceOverview()
    class EntityLookupMarketItemPriceOverview {
        var onStartup: Boolean = false
        var onCron: Boolean = false
        var cron: String = Scheduled.CRON_DISABLED
    }
}
