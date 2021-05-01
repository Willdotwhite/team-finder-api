package team.finder.api.auth

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.util.Base64Utils
import org.springframework.util.SerializationUtils
import team.finder.api.utils.CookieUtils
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.cast

class HttpCookieOAuth2AuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private val requestCookieName: String = "auth_req"
    private val redirectCookieName: String = "redirect_uri"
    private val maxAge: Int = 120

    override fun loadAuthorizationRequest(request: HttpServletRequest?): OAuth2AuthorizationRequest {
        val data = request?.cookies?.find { it.name == requestCookieName }?.value?.let { Base64Utils.decodeFromString(it) }
        return OAuth2AuthorizationRequest::class.cast(SerializationUtils.deserialize(data))
    }

    override fun saveAuthorizationRequest(authorizationRequest: OAuth2AuthorizationRequest?, request: HttpServletRequest?, response: HttpServletResponse?) {
        if (authorizationRequest == null) {
            request?.cookies?.find {it.name == requestCookieName}?.let {
                CookieUtils.resetCookie(it)
                response?.addCookie(it)
            }
            request?.cookies?.find {it.name == redirectCookieName}?.let {
                CookieUtils.resetCookie(it)
                response?.addCookie(it)
            }
            return
        }

        val authCookie = Cookie(requestCookieName, SerializationUtils.serialize(authorizationRequest)?.let { Base64Utils.encodeToString(it) })
        authCookie.maxAge = maxAge
        authCookie.path = "/"
//        authCookie.secure = true
        response?.addCookie(authCookie)

        request?.getParameter("redirect_uri")?.let { println(it) }
    }

    override fun removeAuthorizationRequest(request: HttpServletRequest?): OAuth2AuthorizationRequest =
            this.loadAuthorizationRequest(request)
}