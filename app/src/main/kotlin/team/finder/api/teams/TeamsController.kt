package team.finder.api.teams

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import team.finder.api.auth.HttpCookieOAuth2AuthorizationRequestRepository
import team.finder.api.users.UsersService
import team.finder.api.utils.AuthUtil
import java.util.*
import javax.validation.Valid

@RestController
@CrossOrigin
class TeamsController(
    val usersService: UsersService,
    val service: TeamsService
) {

    private val logger: Logger = LoggerFactory.getLogger(TeamsController::class.java)

    @GetMapping("/teams")
    fun index(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "0") skillsetMask: Int,
        @RequestParam(defaultValue = "asc", name = "order") strSortingOption: String,
    ) : ResponseEntity<Any> {
        val pageIdx = if (page > 0) page else 1
        val boundedSkillsetMask = if (skillsetMask in 1..127) skillsetMask else 0
        val sortType = service.getSortType(strSortingOption)
        return ResponseEntity(service.getTeams(pageIdx, boundedSkillsetMask, sortType), HttpStatus.OK)
    }

    @PostMapping("/teams")
    fun add(@Valid @RequestBody teamDto: TeamDto, @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String): ResponseEntity<Any> {
        if (userIsBanned()) return ResponseEntity(HttpStatus.FORBIDDEN)

        val userDetails = AuthUtil.getUserDetails()

        val authorId: String = userDetails.discordId
        if (service.getTeamByAuthorId(authorId) != null) {
            // Only one active Team per user
            return ResponseEntity(HttpStatus.CONFLICT)
        }

        teamDto.author = userDetails.name
        teamDto.authorId = authorId

        // Check this author doesn't already have a team before creating one
        val team = service.createTeam(Team.fromDto(teamDto))
        return ResponseEntity(team, HttpStatus.CREATED)
    }

    @GetMapping("/teams/mine")
    fun view() : ResponseEntity<Any> {
        if (userIsBanned()) return ResponseEntity(HttpStatus.FORBIDDEN)

        val userDetails = AuthUtil.getUserDetails()

        val jsonSerializableTeam = Optional.ofNullable(service.getTeamByAuthorId(userDetails.discordId))
        return ResponseEntity(jsonSerializableTeam, HttpStatus.OK)
    }

    @PutMapping("/teams/mine")
    fun update(@Valid @RequestBody teamDto: TeamDto, @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String) : ResponseEntity<Any> {
        if (userIsBanned()) return ResponseEntity(HttpStatus.FORBIDDEN)

        val userDetails = AuthUtil.getUserDetails()

        service.updateTeam(userDetails.discordId, teamDto.description, teamDto.skillsetMask, teamDto.languages)
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/teams/mine")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String) : ResponseEntity<Any> {
        if (userIsBanned()) return ResponseEntity(HttpStatus.FORBIDDEN)

        val userDetails = AuthUtil.getUserDetails()

        service.deleteTeam(userDetails.discordId)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/teams/report")
    fun report(@RequestParam("teamId") teamId: Long, @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String) : ResponseEntity<Any> {
        if (userIsBanned()) return ResponseEntity(HttpStatus.FORBIDDEN)

        val team = service.getTeamById(teamId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val user = usersService.getUser(AuthUtil.getUserDetails().discordId)!!
        logger.info("[REPORT] User ${user.discordId} reported Team ${team.id}")

        team.reportCount = team.reportCount + 1
        service.saveTeam(team)

        return ResponseEntity(HttpStatus.OK)
    }

    fun userIsBanned() : Boolean {
        val userDetails = AuthUtil.getUserDetails()
        val user = usersService.getUser(userDetails.discordId)
        return user != null && user.isBanned
    }
}
