package team.finder.api.auth

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import team.finder.api.system.RuntimeConfig

@Component
class JwtVerifier(config: RuntimeConfig) {
    val verifier: JWSVerifier = MACVerifier(config.JwtSecret)

    fun verify(jwt: SignedJWT): Boolean = jwt.verify(verifier)
}