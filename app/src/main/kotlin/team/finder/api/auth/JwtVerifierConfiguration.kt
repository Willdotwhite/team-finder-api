package team.finder.api.auth

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.MACVerifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.finder.api.system.RuntimeConfig

@Configuration
class JwtVerifierConfiguration(val config: RuntimeConfig) {
    @Bean
    fun jwtVerifier(): JWSVerifier = MACVerifier(config.JwtSecret)
}