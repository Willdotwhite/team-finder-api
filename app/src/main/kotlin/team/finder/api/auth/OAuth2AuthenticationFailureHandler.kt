package team.finder.api.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.web.util.UriComponentsBuilder
import team.finder.api.system.RuntimeConfig
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OAuth2AuthenticationFailureHandler(
    @Autowired val config: RuntimeConfig?
) : SimpleUrlAuthenticationFailureHandler() {

    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {
        if (response?.isCommitted == true) {
            println("Response has already been committed.")
            return
        }

        val errDescription = request?.getParameter("error_description")

        val uri = UriComponentsBuilder.fromUriString("${config?.UiDomain}/login/failed")
            .queryParam("error_description", errDescription)
            .build()
            .toUriString()

        redirectStrategy.sendRedirect(request, response, uri)
    }
}
