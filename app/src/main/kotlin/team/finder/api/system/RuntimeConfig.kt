package team.finder.api.system

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Horrific constructor-time call to load config from files
 *
 * If the injection is called at runtime (post-construction) the values will be 'null'
 */
@Component
class RuntimeConfig(
    @Value("\${jwt.secret}") val JwtSecret: String,
    @Value("\${server.ui-domain}") val UiDomain: String
)
