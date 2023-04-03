package de.hypercdn.scmt.util.delay

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Delay(
    val amountPropertyValue: String = "",
    val amount: Long = -1,
    val unit: TimeUnit = TimeUnit.SECONDS
)