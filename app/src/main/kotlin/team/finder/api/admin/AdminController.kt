package team.finder.api.admin

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
    fun reports(): ResponseEntity<Any> {
        val userDetails = AuthUtil.getUserDetails()
        val user = usersService.getUser(userDetails.discordId)
        if (user == null || !user.isAdmin) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        return ResponseEntity(teamsService.getTeamsWithActiveReports(), HttpStatus.OK)
    }

    @DeleteMapping("/admin/delete-team")
    fun deleteTeam(@RequestParam("teamId") idOfTeamToBan: Long) : ResponseEntity<Any> {
        val userDetails = AuthUtil.getUserDetails()
        val user = usersService.getUser(userDetails.discordId)
        if (user == null || !user.isAdmin) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        val teamToDelete = teamsService.getTeamById(idOfTeamToBan) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        teamToDelete.deletedAt = TimestampUtils.getCurrentTimeStamp()
        teamsService.saveTeam(teamToDelete)

        return ResponseEntity(teamToDelete, HttpStatus.OK)
    }

    @PostMapping("/admin/ban-user")
    fun banUser(@RequestParam("userId") discordIdOfUserToBan: String): ResponseEntity<Any> {
        val userDetails = AuthUtil.getUserDetails()
        val user = usersService.getUser(userDetails.discordId)
        if (user == null || !user.isAdmin) {
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
        val teamCreatedByUser = teamsService.getTeamByAuthorId(userToBan.discordId)
        if (teamCreatedByUser != null) {
            deleteTeam(teamCreatedByUser.id)
        }

        return ResponseEntity(userToBan, HttpStatus.OK)
    }

}
