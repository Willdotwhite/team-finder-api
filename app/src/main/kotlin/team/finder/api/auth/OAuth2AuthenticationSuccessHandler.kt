package team.finder.api.auth

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.MACSigner
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.web.util.UriComponentsBuilder
import team.finder.api.utils.CookieUtils
import java.security.SecureRandom
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OAuth2AuthenticationSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        if (response?.isCommitted == true) {
            println("Response has already been committed.");
            return;
        }

        val jwsObj = JWSObject(JWSHeader(JWSAlgorithm.HS256), Payload("Test"))
        val sharedKey = ByteArray(32)
        SecureRandom().nextBytes(sharedKey)
        jwsObj.sign(MACSigner(sharedKey))
        val serialize = jwsObj.serialize()

        val uri = UriComponentsBuilder.fromUriString("http://localhost:8080/teams")
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