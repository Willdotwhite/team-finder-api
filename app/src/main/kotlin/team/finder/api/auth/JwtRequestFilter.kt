package team.finder.api.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.text.ParseException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList

@Component
class JwtRequestFilter : OncePerRequestFilter() {
    val matchers: MutableList<RequestMatcher> = ArrayList()

    @Autowired
    val mapper: ObjectMapper? = null

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val tokenHeader = request.getHeader("Authorization")
                ?: return sendErrorMessage(response, HttpStatus.BAD_REQUEST, "No authorization token send")

        if (!tokenHeader.startsWith("Bearer "))
            return sendErrorMessage(response, HttpStatus.BAD_REQUEST, "Authorization token doesn't start with Bearer")

        val token = tokenHeader.substring(7);
        val parsedToken: SignedJWT
        try {
            parsedToken = SignedJWT.parse(token)
        } catch (e: ParseException) {
            return sendErrorMessage(response, HttpStatus.BAD_REQUEST, "Malformed token")
        }

        val iatClaim = parsedToken.jwtClaimsSet.getClaim("iat")
                ?: return sendErrorMessage(response, HttpStatus.BAD_REQUEST, "Malformed token")


        if (iatClaim !is Date) {
            return sendErrorMessage(response, HttpStatus.BAD_REQUEST, "Malformed token")
        }

        val iat = iatClaim.toInstant();
        val expired = iat.plus(5, ChronoUnit.HOURS);
        if (expired < Instant.now()) {
            return sendErrorMessage(response, HttpStatus.UNAUTHORIZED, "Expired token")
        }

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return this.matchers.any { it.matches(request) }
    }

    fun addIgnoredPatterns(vararg patterns: Pair<String, HttpMethod>) {
        for (pattern in patterns) {
            this.matchers.add(AntPathRequestMatcher(pattern.first, pattern.second.name))
        }
    }

    fun addIgnoredPatterns(vararg patterns: String) {
        for (pattern in patterns) {
            this.matchers.add(AntPathRequestMatcher(pattern))
        }
    }

    fun sendErrorMessage(response: HttpServletResponse, httpStatus: HttpStatus, message: String) {
        val map = mapOf(Pair("message", message))
        response.status = httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        mapper?.writeValue(response.writer, map)
    }
}