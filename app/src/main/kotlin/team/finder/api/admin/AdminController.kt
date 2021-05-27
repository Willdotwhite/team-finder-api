package team.finder.api.admin

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import team.finder.api.teams.TeamsService
import team.finder.api.users.User
import team.finder.api.users.UsersService
import team.finder.api.utils.AuthUtil
import team.finder.api.utils.TimestampUtils


@RestController
@CrossOrigin
class AdminController(
    val teamsService: TeamsService,
    val usersService: UsersService
) {

    @Autowired
    private val cacheManager: CacheManager? = null

    private val logger: Logger = LoggerFactory.getLogger(AdminController::class.java)
    private val ADMIN_TAG : String = "ADMIN"

    private fun getAuthorisedUser() : User? {
        val userDetails = AuthUtil.getUserDetails()
        val user = usersService.getUser(userDetails.discordId)
        return if (user?.isAdmin == true) user else null
    }

    @GetMapping("/admin/reports")
    fun reports(): ResponseEntity<Any> {
        val adminUser = getAuthorisedUser() ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        return ResponseEntity(teamsService.getTeamsWithActiveReports(), HttpStatus.OK)
    }

    @DeleteMapping("/admin/delete-team")
    fun deleteTeam(@RequestParam("teamId") idOfTeamToBan: Long) : ResponseEntity<Any> {
        val adminUser = getAuthorisedUser() ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val teamToDelete = teamsService.getTeamById(idOfTeamToBan) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        teamToDelete.deletedAt = TimestampUtils.getCurrentTimeStamp()
        teamsService.saveTeam(teamToDelete)

        logger.info("[${ADMIN_TAG}] ${adminUser.name} has deleted Team ${teamToDelete.id}")

        // Clear Teams cache to remove any offensive material
        clearCache()

        return ResponseEntity(teamToDelete, HttpStatus.OK)
    }

    @PostMapping("/admin/ban-user")
    fun banUser(@RequestParam("userId") discordIdOfUserToBan: String): ResponseEntity<Any> {
        val adminUser = getAuthorisedUser() ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val userToBan = usersService.getUser(discordIdOfUserToBan) ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        if (userToBan.isAdmin) {
            // If you really need to do this, don't do it anonymously
            return ResponseEntity(HttpStatus.CONFLICT)
        }

        userToBan.isBanned = true
        usersService.saveUser(userToBan)

        logger.info("[${ADMIN_TAG}] ${adminUser.name} has banned User ${userToBan.discordId}")

        // Delete current team as well
        val teamCreatedByUser = teamsService.getTeamByAuthorId(userToBan.discordId)
        if (teamCreatedByUser != null) {
            deleteTeam(teamCreatedByUser.id)
        }

        return ResponseEntity(userToBan, HttpStatus.OK)
    }

    fun clearCache() {
        if (cacheManager == null) {
            return
        }

        // Clear all caches
        for (name in cacheManager.cacheNames) {
            cacheManager.getCache(name)?.clear()
        }
    }

}
