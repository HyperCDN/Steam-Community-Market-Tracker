package de.hypercdn.scmt.util.delay

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Aspect
@Component
class DelayAspect @Autowired constructor(
    val env: Environment
) {

    val log: Logger = LoggerFactory.getLogger(DelayAspect::class.java)

    @Around("@annotation(Delay)")
    fun delayExecution(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val annotation = signature.method.getAnnotation(Delay::class.java)
        if (annotation.amountPropertyValue.isEmpty() && annotation.amount > 0) {
            log.debug("Delaying execution of {} for {} {}", joinPoint, annotation.unit, annotation.amount)
            sleepWithoutException(annotation.unit, annotation.amount)
        } else if (annotation.amountPropertyValue.isNotEmpty()) {
            val durationString = env.getProperty(annotation.amountPropertyValue) ?: throw IllegalArgumentException("No value found for property key")
            val duration = Duration.parse("PT${durationString}");
            log.debug("Delaying execution of {} for {} {}", joinPoint, duration.toMillis(), TimeUnit.MILLISECONDS)
            sleepWithoutException(TimeUnit.MILLISECONDS, duration.toMillis())
        }
        log.debug("Continuing execution of {}", joinPoint)
        return joinPoint.proceed()
    }

    fun sleepWithoutException(unit: TimeUnit, value: Long) {
        try {
            unit.sleep(value)
        } catch (_: Exception) {
        }
    }
}