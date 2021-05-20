package team.finder.api.admin

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.finder.api.users.UsersService
import team.finder.api.utils.AuthUtil

@RestController
@CrossOrigin
class AdminController(val service: UsersService) {

    @GetMapping("/admin/reports")
    fun get(): String {
        val userDetails = AuthUtil.getUserDetails()

        val user = service.getUser(userDetails.discordId)
        if (user == null || !user.isAdmin) {
            return "lolno"
        }

        return user.name + " is an admin"
    }

}
