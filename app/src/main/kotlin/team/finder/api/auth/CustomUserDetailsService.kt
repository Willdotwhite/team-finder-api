package team.finder.api.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import team.finder.api.users.UsersRepository


@Service
class CustomUserDetailsService : UserDetailsService {

    @Autowired
    val userRepo: UsersRepository? = null

    override fun loadUserByUsername(username: String?): UserDetails {
        return CustomUserDetails(123, "name", HashMap())
//        val user: UserDetails = this.users.get(username!!.toLowerCase()) ?: throw UsernameNotFoundException(username)
//        return User(user.username, user.password, user.isEnabled, user.isAccountNonExpired,
//                user.isCredentialsNonExpired, user.isAccountNonLocked, user.authorities)
    }
}
