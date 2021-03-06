package team.finder.api.auth

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.web.util.UriComponentsBuilder
import team.finder.api.system.RuntimeConfig
import team.finder.api.utils.CookieUtils
import java.time.Instant
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OAuth2AuthenticationSuccessHandler(
    @Autowired val config: RuntimeConfig?
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        if (response?.isCommitted == true) {
            logger.warn("Response has already been committed.");
            return;
        }

        if (authentication?.principal == null || authentication.principal !is CustomUserDetails) {
            logger.warn("Something went wrong while authenticating. Please try again");
            return;
        }

        val details: CustomUserDetails = authentication.principal as CustomUserDetails
        val id = details.attributes["id"].toString()

        val claimsBuilder = JWTClaimsSet.Builder()
        claimsBuilder.claim("id", id)
        claimsBuilder.claim("iat", Instant.now().epochSecond)

        // FIXME: Create proper collection, this is *not* how this should work
        claimsBuilder.claim("sub", "${details.name}#${details.attributes.get("discriminator")}")
        claimsBuilder.claim("aud", "https://cdn.discordapp.com/avatars/${id}/${details.attributes.get("avatar")}.png?size=128")

        val token = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsBuilder.build())
        token.sign(MACSigner(config?.JwtSecret))

        val serialize = token.serialize()
        val uri = UriComponentsBuilder.fromUriString("${config?.UiDomain}/login/authorized")
                .queryParam("token", serialize)
                .build()
                .toUriString()

        this.clearAuthenticationAttributes(request)
        request?.cookies?.find {it.name == "auth_req"}?.let {
            CookieUtils.resetCookie(it)
            response?.addCookie(it)
        }
        request?.cookies?.find {it.name == "redirect_uri"}?.let {
            CookieUtils.resetCookie(it)
            response?.addCookie(it)
        }

        redirectStrategy.sendRedirect(request, response, uri)
    }
}
