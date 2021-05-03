package team.finder.api.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import team.finder.api.users.User
import team.finder.api.users.UsersRepository

@Service
class CustomOAuth2UserService : DefaultOAuth2UserService() {

    @Autowired
    val userRepository: UsersRepository? = null

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val id = findId(oAuth2User.attributes) ?: throw OAuth2AuthenticationException(OAuth2Error("No discord id available"))
        val user = findUser(id)
        return if (user == null) {
            val u = User(oAuth2User.name, id)
            userRepository!!.save(u)
            CustomUserDetails(u, oAuth2User.attributes)
        } else {
            user.name = oAuth2User.name
            userRepository!!.save(user)
            CustomUserDetails(user, oAuth2User.attributes)
        }
    }

    private fun findUser(id: Long): User? {
        val maybeUser = userRepository!!.findById(id)
        if (maybeUser.isPresent) {
            return maybeUser.get()
        }
        return null
    }

    private fun findId(attributes: Map<String, Any>): Long? {
        for (attribute in attributes) {
            if (attribute.key == "id") {
                if (attribute.value is String) {
                    return (attribute.value as String).toLong()
                }
            }
        }
        return null
    }
}