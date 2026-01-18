package com.example.demo.security.config

import com.example.demo.common.config.CorsConfig
import com.example.demo.persistence.auth.provider.AuthProvider
import com.example.demo.persistence.auth.provider.JWTProvider
import com.example.demo.security.component.SecurityErrorResponseWriter
import com.example.demo.security.filter.JWTAuthFilter
import com.example.demo.security.handler.CustomAccessDeniedHandler
import com.example.demo.security.handler.CustomAuthenticationEntryPoint
import jakarta.servlet.DispatcherType
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Profile("local")
@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
class LocalSecurityConfig(
	private val corsConfig: CorsConfig,
	private val jwtProvider: JWTProvider,
	private val authProvider: AuthProvider,
	private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
	private val customAccessDeniedHandler: CustomAccessDeniedHandler,
	private val securityErrorResponseWriter: SecurityErrorResponseWriter
) {
	@Bean
	@Throws(Exception::class)
	fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager = authenticationConfiguration.authenticationManager

	@Bean
	@Throws(Exception::class)
	fun filterChain(http: HttpSecurity): SecurityFilterChain {
		val permitEndpoints = authProvider.getAllPermitEndpoints()

		return http
			.csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
			.httpBasic { httpBasic: HttpBasicConfigurer<HttpSecurity> -> httpBasic.disable() }
			.formLogin { formLogin: FormLoginConfigurer<HttpSecurity> -> formLogin.disable() }
			.sessionManagement { sessionManagement: SessionManagementConfigurer<HttpSecurity> ->
				sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			}.cors { cors: CorsConfigurer<HttpSecurity> ->
				cors.configurationSource(corsConfig.corsConfigurationSource())
			}.authorizeHttpRequests { auth ->
				auth.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

				permitEndpoints.forEach { endpoint ->
					auth.requestMatchers(endpoint).permitAll()
				}
				auth.requestMatchers(PathRequest.toH2Console()).permitAll()

				auth.anyRequest().authenticated()
			}.addFilterBefore(
				JWTAuthFilter(jwtProvider, securityErrorResponseWriter),
				UsernamePasswordAuthenticationFilter::class.java
			).headers { headers ->
				headers.frameOptions { frameOptions ->
					frameOptions.sameOrigin()
				}
			}.exceptionHandling { exception ->
				exception
					.authenticationEntryPoint(customAuthenticationEntryPoint)
					.accessDeniedHandler(customAccessDeniedHandler)
			}.build()
	}
}
