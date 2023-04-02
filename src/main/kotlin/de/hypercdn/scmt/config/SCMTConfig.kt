package de.hypercdn.scmt.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.time.Duration

@Configuration
@ConfigurationProperties(prefix = "steam-community-market-tracker.misc")
class MiscConfig {
    var currency: Int = 3
    var currencyName: String = "EUR"
}


@Configuration
@ConfigurationProperties(prefix = "steam-community-market-tracker.rate-limits")
class RateLimitConfig {
    var marketItemSearch: Duration = Duration.ZERO
    var marketItemPriceSearch: Duration = Duration.ZERO
    var marketInventorySearch: Duration = Duration.ZERO
}

@Configuration
@ConfigurationProperties("steam-community-market-tracker.app-search")
class AppConfig {
    var updateOnStartup: Boolean = false
    var updateOnCron: Boolean = false
    var cron: String = Scheduled.CRON_DISABLED

    var trackNewByDefault: Boolean = false
    var untrackOnNoFoundItems: Boolean = true
    var deleteNotFoundApp: Boolean = false
}

@Configuration
@ConfigurationProperties("steam-community-market-tracker.item-search")
class ItemSearchConfig {
    var updateOnStartup: Boolean = false
    var updateOnCron: Boolean = false
    var cron: String = Scheduled.CRON_DISABLED
    var noUpdateBefore: Duration = Duration.ZERO

    var trackNewByDefault: Boolean = false
    var disableNotFoundEntities: Boolean = true
    var deleteNotFoundEntities: Boolean = false
}

@Configuration
@ConfigurationProperties("steam-community-market-tracker.item-price-search")
class ItemPriceSearchConfig {
    var updateOnStartup: Boolean = false
    var updateOnCron: Boolean = false
    var cron: String = Scheduled.CRON_DISABLED
    var noUpdateBefore: Duration = Duration.ZERO
}

@Configuration
@ConfigurationProperties("steam-community-market-tracker.inventory-search")
class InventorySearchConfig {
    var onStartup: Boolean = false
    var onCron: Boolean = false
    var cron: String = Scheduled.CRON_DISABLED
    var noUpdateBefore: Duration = Duration.ZERO
    var disableNotFoundEntities: Boolean = true
    var deleteNotFoundEntities: Boolean = false
}
