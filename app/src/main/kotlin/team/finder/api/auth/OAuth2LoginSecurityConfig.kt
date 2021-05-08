package team.finder.api.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@EnableWebSecurity
@Configuration
class OAuth2LoginSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    val userDetailsService: CustomUserDetailsService? = null
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
                .successHandler(OAuth2AuthenticationSuccessHandler())
                .failureHandler(OAuth2AuthenticationFailureHandler())

        jwtRequestFilter?.addIgnoredPatterns("/error", "/login/oauth2/code/discord")
        jwtRequestFilter?.addIgnoredPatterns(Pair("/teams", HttpMethod.GET))
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
    }

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth?.userDetailsService(userDetailsService)
    }

    fun cookieAuthorizationRequestRepository(): AuthorizationRequestRepository<OAuth2AuthorizationRequest> = HttpCookieOAuth2AuthorizationRequestRepository()
}
