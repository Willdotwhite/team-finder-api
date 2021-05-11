package team.finder.api.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OAuth2AuthenticationFailureHandler : SimpleUrlAuthenticationFailureHandler() {

    @Value("\${server.uiDomain}")
    val uiDomain: String? = null

    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {
        if (response?.isCommitted == true) {
            println("Response has already been committed.");
            return;
        }

        val errDescription = request?.getParameter("error_description")

        val uri = UriComponentsBuilder.fromUriString(uiDomain!! + "/login/failed")
            .queryParam("error_description", errDescription)
            .build()
            .toUriString()

        redirectStrategy.sendRedirect(request, response, uri)
    }
}
