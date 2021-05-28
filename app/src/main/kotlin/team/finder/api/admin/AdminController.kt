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

    @PostMapping("/admin/reports/clear")
    fun clearReports(@RequestParam("teamId") teamId: Long) : ResponseEntity<Any> {
        val adminUser = getAuthorisedUser() ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val teamToRedeem = teamsService.getTeamById(teamId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        if (teamToRedeem.reportCount == 0) {
            // Return early to save on unnecessary DB Update
            return ResponseEntity(teamToRedeem, HttpStatus.OK)
        }

        teamToRedeem.reportCount = 0
        teamsService.saveTeam(teamToRedeem)

        logger.info("[${ADMIN_TAG}] ${adminUser.name} has cleared reports for Team ${teamToRedeem.id}")

        return ResponseEntity(teamToRedeem, HttpStatus.OK)
    }

    @DeleteMapping("/admin/delete-team")
    fun deleteTeam(@RequestParam("teamId") idOfTeamToBan: Long) : ResponseEntity<Any> {
        val adminUser = getAuthorisedUser() ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val teamToDelete = teamsService.getTeamById(idOfTeamToBan) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        teamToDelete.deletedAt = TimestampUtils.getCurrentTimeStamp()
        teamsService.saveTeam(teamToDelete)

        logger.info("[${ADMIN_TAG}] ${adminUser.name} has deleted Team ${teamToDelete.id}")

        // Clear Teams cache to remove any offensive material
        clearCache(adminUser.name)

        return ResponseEntity(teamToDelete, HttpStatus.OK)
    }

    @PostMapping("/admin/reinstate-team")
    fun reinstateTeam(@RequestParam("teamId") idOfTeamToRedeem: Long) : ResponseEntity<Any> {
        val adminUser = getAuthorisedUser() ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val teamToRedeem = teamsService.getDeletedTeamById(idOfTeamToRedeem) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        teamToRedeem.deletedAt = null
        teamsService.saveTeam(teamToRedeem)

        logger.info("[${ADMIN_TAG}] ${adminUser.name} has reinstated Team ${teamToRedeem.id}")

        return ResponseEntity(teamToRedeem, HttpStatus.OK)
    }

    @PostMapping("/admin/ban-user")
    fun banUser(@RequestParam("userId") discordIdOfUserToBan: String): ResponseEntity<Any> {
        val adminUser = getAuthorisedUser() ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val userToBan = usersService.getUser(discordIdOfUserToBan) ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        if (userToBan.isAdmin) {
            // If you really need to do this, don't do it anonymously
            return ResponseEntity(HttpStatus.CONFLICT)
        }

        if (userToBan.isBanned) {
            // Return early to save on unnecessary DB Update
            return ResponseEntity(userToBan, HttpStatus.OK)
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

    @PostMapping("/admin/redeem-user")
    fun redeemUser(
        @RequestParam("userId") discordIdOfUserToRedeem: String,
        @RequestParam("reinstateTeam", defaultValue = "false") shouldReinstateTeam: Boolean
    ): ResponseEntity<Any> {
        val adminUser = getAuthorisedUser() ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val userToRedeem = usersService.getUser(discordIdOfUserToRedeem) ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        userToRedeem.isBanned = false
        usersService.saveUser(userToRedeem)

        logger.info("[${ADMIN_TAG}] ${adminUser.name} has redeemed User ${userToRedeem.discordId}")

        // @todo should we always re-instate the team automatically? Maybe they were banned for a bad team but are being given a second chance (in which case we shouldn't reinstate the team)
        // Re-instate deleted team as well (if any)
        if (shouldReinstateTeam) {
            val teamCreatedByUser = teamsService.getDeletedTeamByAuthorId(userToRedeem.discordId)
            if (teamCreatedByUser != null) {
                reinstateTeam(teamCreatedByUser.id)
            }
        }

        return ResponseEntity(userToRedeem, HttpStatus.OK)
    }

    fun clearCache(userName: String) {
        if (cacheManager == null) {
            logger.info("[${ADMIN_TAG}] [CACHE] No cache manager detected/hydrated on cache clear attempt")
            return
        }

        logger.info("[${ADMIN_TAG}] [CACHE] $userName has actioned cache clear")

        // Clear all caches
        for (name in cacheManager.cacheNames) {
            logger.info("[${ADMIN_TAG}] [CACHE] Clearing $name cache")

            cacheManager.getCache(name)?.invalidate()
        }
    }

}

private val ADMIN_TAG : String = "ADMIN"
