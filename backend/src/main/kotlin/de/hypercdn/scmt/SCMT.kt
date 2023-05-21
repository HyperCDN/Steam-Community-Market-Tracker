package de.hypercdn.scmt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SteamCommunityMarketTrackerApplication

fun main(args: Array<String>) {
	runApplication<SteamCommunityMarketTrackerApplication>(*args)
}
