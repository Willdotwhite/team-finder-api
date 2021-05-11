package team.finder.api.auth

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.web.util.UriComponentsBuilder
import team.finder.api.utils.CookieUtils
import java.time.Instant
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OAuth2AuthenticationSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {

    private val logger: Logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)

    @Value("\${jwt.secret}")
    val secret: String? = null

    @Value("\${server.uiDomain}")
    val uiDomain: String = "https://teamfinder.gmtkgamejam.com"

    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        logger.warn("Domain check: $uiDomain")

        if (response?.isCommitted == true) {
            logger.warn("Response has already been committed.");
            return;
        }

        if (authentication?.principal == null || authentication.principal !is CustomUserDetails) {
            logger.warn("Something went wrong while authenticating. Please try again");
            return;
        }
        val id = (authentication.principal as CustomUserDetails).attributes["id"].toString().toLong()

        val claimsBuilder = JWTClaimsSet.Builder()
        claimsBuilder.claim("id", id)
        claimsBuilder.claim("iat", Instant.now().epochSecond)
        val token = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsBuilder.build())

        logger.warn("Pre-sign note")

        token.sign(MACSigner("secretttttttttttttttttttttttttttttttt"))
        val serialize = token.serialize()

        logger.warn("URI check: " + UriComponentsBuilder.fromUriString("$uiDomain/login/authorized").toUriString())

        val uri = UriComponentsBuilder.fromUriString(uiDomain + "/login/authorized")
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
