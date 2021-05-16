package team.finder.api.utils

import org.springframework.security.core.context.SecurityContextHolder
import team.finder.api.auth.CustomUserDetails

object AuthUtil {
    fun getUserDetails(): CustomUserDetails {
        val principal = SecurityContextHolder.getContext().authentication.principal
        return principal as CustomUserDetails
    }
}
