package team.finder.api.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import team.finder.api.system.RuntimeConfig


@EnableWebSecurity
@Configuration
class OAuth2LoginSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    val config: RuntimeConfig? = null

    @Autowired
    val customOAuth2UserService: CustomOAuth2UserService? = null
    @Autowired
    val jwtRequestFilter: JwtRequestFilter? = null

    override fun configure(http: HttpSecurity) {
        http
                .cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .oauth2Login()
                .authorizationEndpoint()
                .authorizationRequestRepository(this.cookieAuthorizationRequestRepository())
                .and()
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(OAuth2AuthenticationSuccessHandler(config))
                .failureHandler(OAuth2AuthenticationFailureHandler(config))

        jwtRequestFilter?.addIgnoredPatterns("/error")
        jwtRequestFilter?.addIgnoredPatterns("/login/*")
        jwtRequestFilter?.addIgnoredPatterns("/_system")
        jwtRequestFilter?.addIgnoredPatterns(Pair("/teams", HttpMethod.GET))

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
    }

    fun cookieAuthorizationRequestRepository(): AuthorizationRequestRepository<OAuth2AuthorizationRequest> = HttpCookieOAuth2AuthorizationRequestRepository()
}
