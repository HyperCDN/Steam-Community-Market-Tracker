package de.hypercdn.scmt.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain? {
        http
            .csrf().disable()
            .cors().disable()
            .anonymous().disable()
            .authorizeHttpRequests {
                // no auth or optional
                it.requestMatchers(
                    HttpMethod.GET,
                    "/app/*",
                    "/item/*/*", "/item/*/*/snapshots", "/item/*/*/price", "/item/*/*/volume",
                    "/inventory/*/*", "/inventory/*/*/items"
                ).permitAll()
                it.requestMatchers(
                    HttpMethod.POST,
                    ""
                ).permitAll()
                it.requestMatchers(
                    HttpMethod.PATCH,
                    ""
                ).permitAll()
                it.requestMatchers(
                    HttpMethod.DELETE,
                    ""
                ).permitAll()
                // auth required
                it.requestMatchers(
                    HttpMethod.GET,
                    "/apps",
                    "/items/*",
                    "/inventories/*",
                    "/internal/**"
                ).authenticated()
                it.requestMatchers(
                    HttpMethod.POST,
                    "/inventory/*"
                ).authenticated()
                it.requestMatchers(
                    HttpMethod.PATCH,
                    "/app/*",
                    "/item/*/*",
                    "/inventory/*/*"
                ).authenticated()
                it.requestMatchers(
                    HttpMethod.DELETE,
                    "/app/*",
                    "/item/*/*",
                    "/inventory/*"
                ).authenticated()
                // exposing the api documentation behind /docs
                it.requestMatchers("/docs/**").permitAll()
            }.httpBasic()

        return http.build()
    }

}