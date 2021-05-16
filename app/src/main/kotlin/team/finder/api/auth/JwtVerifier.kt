package team.finder.api.auth

import com.nimbusds.jwt.SignedJWT
import org.springframework.stereotype.Component

@Component
class JwtVerifier {
    constructor() {

    }

    fun verify(jwt: SignedJWT): Boolean {
        return false
    }
}