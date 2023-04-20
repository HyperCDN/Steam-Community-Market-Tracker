package de.hypercdn.scmt.util.code429

import de.hypercdn.scmt.util.steam.api.SteamFetchService
import lombok.Synchronized
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.OffsetDateTime

@Aspect
@Component
class Code429Aspect {

    val log: Logger = LoggerFactory.getLogger(Code429Aspect::class.java)
    val activeCode429: HashMap<String, OffsetDateTime> = HashMap()
    val knownWaitDelay: HashMap<String, Duration> = HashMap()

    @Synchronized
    @Around("@annotation(Code429)")
    fun run(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature.name
        if (hasActiveCode429(signature))
            throw SteamFetchService.HttpFetchException(429, "Fetch currently unavailable due to previously received code 429 for ${getRemainingDuration(signature)}")
        try {
            val result = joinPoint.proceed()
            disable(signature)
            return result
        } catch (e: SteamFetchService.HttpFetchException) {
            if (e.code == 429) enable(signature)
            throw e
        }

    }

    fun hasPendingCode429(signature: String): Boolean {
        return activeCode429.contains(signature)
    }

    fun hasActiveCode429(signature: String): Boolean {
        return getRemainingDuration(signature).isNegative
    }

    fun getRemainingDuration(signature: String): Duration {
        val known429 = activeCode429.get(signature) ?: return Duration.ZERO
        val waitDelay = knownWaitDelay.getOrPut(signature){ Duration.ofMinutes(30) }
        return Duration.between(known429, OffsetDateTime.now()).minus(waitDelay)
    }

    fun disable(signature: String) {
        if (hasPendingCode429(signature) && !hasActiveCode429(signature)) {
            log.info("Code 429 no longer pending for $signature")
            activeCode429.remove(signature)
        }
    }

    fun enable(signature: String) {
        if (hasPendingCode429(signature) && !hasActiveCode429(signature))
            knownWaitDelay.put(signature, knownWaitDelay.getOrPut(signature){ Duration.ofMinutes(30) }.multipliedBy(2))
        else
            knownWaitDelay.put(signature, Duration.ofMinutes(30))
        activeCode429.put(signature, OffsetDateTime.now())
        log.warn("Caught HttpFetchException with code 429 on $signature")
    }

}