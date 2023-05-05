package de.hypercdn.scmt.util.http

import de.hypercdn.extensions.okhttpktx.proxy.ExtendedProxy
import de.hypercdn.extensions.okhttpktx.proxy.manager.FeedbackAwareCallManager
import de.hypercdn.extensions.okhttpktx.proxy.provider.imp.RotatingFeedbackAwareProxyProvider
import de.hypercdn.extensions.okhttpktx.proxy.utils.*
import de.hypercdn.scmt.config.ProxyConfig
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sockslib.common.UsernamePasswordCredentials
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

@Configuration
class SCMTProxyClientHelper @Autowired constructor(
    val okHttpClient: OkHttpClient,
    val proxyConfig: ProxyConfig
) {

    private val log: Logger = LoggerFactory.getLogger(SCMTProxyClientHelper::class.java)

    @Bean
    fun scmtCallManager(): SCMTProxiedCallManager {

        fun enableAfter(duration: Duration): EnableStrategy {
            return enableAfter(OffsetDateTime.now().plus(duration.toJavaDuration()))
        }

        val handlingRules = HashMap<DisableStrategy, () -> EnableStrategy>().apply {
            put(disableOnStatusCode(429), { enableAfter(6.hours) })
            put(disableOnStatusCode(403), { enableAfter(24.hours) })
            put(disableOnException(), { enableAfter(1.hours) })
        }
        val proxies = ConcurrentLinkedQueue(proxyConfig.proxies.map { it.asExtendedProxy() as Proxy }.toMutableList())
        log.info("Using ${proxies.size} proxies to dispatch requests. (enabled = ${proxyConfig.enabled})")

        val proxyProvider = RotatingFeedbackAwareProxyProvider(proxies, handlingRules)
        return SCMTProxiedCallManager(proxyConfig, okHttpClient, proxyProvider)
    }

}

class SocksProxyConfig(
    val hostname: String,
    val port: Int,
    val username: String?,
    val password: String?
) {

    fun asExtendedProxy(): ExtendedProxy {
        if (username == null || password == null)
            return ExtendedProxy(Proxy.Type.SOCKS, InetSocketAddress(hostname, port), null)
        return ExtendedProxy(Proxy.Type.SOCKS, InetSocketAddress(hostname, port), UsernamePasswordCredentials(username, password))
    }

}

class SCMTProxiedCallManager(val proxyConfig: ProxyConfig, okHttpClient: OkHttpClient, proxyProvider: RotatingFeedbackAwareProxyProvider) : FeedbackAwareCallManager(okHttpClient, proxyProvider) {

    override fun newCall(request: Request): Call {
        if (!proxyConfig.enabled)
            return okHttpClient.newCall(request)
        return super.newCall(request)
    }

    fun providerRatio(): Pair<Int, Int> {
        return (feedbackAwareProxyProvider as RotatingFeedbackAwareProxyProvider).ratio()
    }

    fun supplyInfo(): Map<Proxy, Long> {
        return (feedbackAwareProxyProvider as RotatingFeedbackAwareProxyProvider).supplyInfo()
    }

}