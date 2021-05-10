package team.finder.api.auth

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.util.Base64Utils
import org.springframework.util.SerializationUtils
import team.finder.api.utils.CookieUtils
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.cast

class HttpCookieOAuth2AuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private val logger: Logger = LoggerFactory.getLogger(HttpCookieOAuth2AuthorizationRequestRepository::class.java)

    private val requestCookieName: String = "auth_req"
    private val redirectCookieName: String = "redirect_uri"
    private val maxAge: Int = 120

    override fun loadAuthorizationRequest(request: HttpServletRequest?): OAuth2AuthorizationRequest {
        val cookies = request?.cookies
        logger.warn("AUTH_REQ cookie: " + cookies.toString())

        val authReqCookie = cookies?.find { it.name == requestCookieName }?.value
        logger.warn("AUTH_REQ authReq: " + authReqCookie.toString())

        val serData = authReqCookie?.let { Base64Utils.decodeFromString(it) }
        logger.warn("AUTH_REQ serData: " + serData.toString())

        val data = SerializationUtils.deserialize(serData)
        logger.warn("AUTH_REQ data: " + data.toString())

        return OAuth2AuthorizationRequest::class.cast(data)
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
        authCookie.secure = true
        response?.addCookie(authCookie)

        request?.getParameter("redirect_uri")?.let { println(it) }
    }

    override fun removeAuthorizationRequest(request: HttpServletRequest?): OAuth2AuthorizationRequest =
            this.loadAuthorizationRequest(request)
}
