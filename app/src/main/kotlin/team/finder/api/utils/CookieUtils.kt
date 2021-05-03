package team.finder.api.utils

import javax.servlet.http.Cookie

class CookieUtils {
    companion object {
        fun resetCookie(c: Cookie) {
            c.path = "/"
            c.maxAge = 0
            c.value = ""
        }
    }
}