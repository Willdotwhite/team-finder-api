package team.finder.api.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import team.finder.api.users.User

class CustomUserDetails(private val discordId: Long, private val name: String, private val attributes: MutableMap<String, Any>) : OAuth2User, UserDetails {
    constructor(u: User, attributes: MutableMap<String, Any>) : this(u.discordId, u.name, attributes)

    override fun getName(): String = name

    override fun getAttributes(): MutableMap<String, Any> = attributes

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = ArrayList()

    override fun getPassword(): String? = null

    override fun getUsername(): String = name

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}