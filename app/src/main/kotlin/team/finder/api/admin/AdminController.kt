package team.finder.api.admin

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import team.finder.api.teams.Team
import team.finder.api.teams.TeamsService
import team.finder.api.users.UsersService
import team.finder.api.utils.AuthUtil
import team.finder.api.utils.TimestampUtils

@RestController
@CrossOrigin
class AdminController(
    val teamsService: TeamsService,
    val usersService: UsersService
) {

    @GetMapping("/admin/reports")
    fun reports(): List<Team> {
        val userDetails = AuthUtil.getUserDetails()

        val user = usersService.getUser(userDetails.discordId)
        if (user == null || !user.isAdmin) {
            // TODO: Respond with error code, don't let users know anything is here
            return listOf()
        }

        return teamsService.getTeamsWithActiveReports()
    }

    @DeleteMapping("/admin/delete-team")
    fun deleteTeam(@RequestParam("userId") discordIdOfUserToBan: String) : ResponseEntity<Any> {
        val userDetails = AuthUtil.getUserDetails()
        val user = usersService.getUser(userDetails.discordId)
        if (user == null || !user.isAdmin) {
            // TODO: Respond with error code, don't let users know anything is here
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        val teamToDelete = teamsService.getTeamByAuthorId(discordIdOfUserToBan) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        teamToDelete.deletedAt = TimestampUtils.getCurrentTimeStamp()
        teamsService.saveTeam(teamToDelete)

        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/admin/ban-user")
    fun banUser(@RequestParam("userId") discordIdOfUserToBan: String): ResponseEntity<Any> {
        val userDetails = AuthUtil.getUserDetails()

        val user = usersService.getUser(userDetails.discordId)
        if (user == null || !user.isAdmin) {
            // TODO: Respond with error code, don't let users know anything is here
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        val userToBan = usersService.getUser(discordIdOfUserToBan) ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        if (userToBan.isAdmin) {
            // If you really need to do this, don't do it anonymously
            return ResponseEntity(HttpStatus.CONFLICT)
        }

        // TODO: Audit record
        userToBan.isBanned = true
        usersService.saveUser(userToBan)

        // Delete current team as well
        deleteTeam(userToBan.discordId)

        return ResponseEntity(HttpStatus.OK)
    }

}
