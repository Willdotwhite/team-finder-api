package team.finder.api.admin

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.finder.api.teams.Team
import team.finder.api.teams.TeamsService
import team.finder.api.users.UsersService
import team.finder.api.utils.AuthUtil

@RestController
@CrossOrigin
class AdminController(
    val teamsService: TeamsService,
    val usersService: UsersService
) {

    @GetMapping("/admin/reports")
    fun get(): List<Team> {
        val userDetails = AuthUtil.getUserDetails()

        val user = usersService.getUser(userDetails.discordId)
        if (user == null || !user.isAdmin) {
            // TODO: Respond with error code, don't let users know anything is here
            return listOf()
        }

        return teamsService.getTeamsWithActiveReports()
    }

}
