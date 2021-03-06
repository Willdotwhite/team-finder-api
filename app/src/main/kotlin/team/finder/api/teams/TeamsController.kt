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
        @RequestParam(defaultValue = "", name = "languages") languages: String,
        @RequestParam(defaultValue = "", name = "query") query: String
    ) : ResponseEntity<Any> {
        val pageIdx = if (page > 0) page else 1
        val boundedSkillsetMask = if (skillsetMask in 1..255) skillsetMask else 0
        val sortType = service.getSortType(strSortingOption)
        val orderedLanguages = sanitiseFreetextInput(languages)
        val sanitisedQuery = sanitiseFreetextInput(query.trim())

        return ResponseEntity(service.getTeams(sanitisedQuery, pageIdx, boundedSkillsetMask, orderedLanguages, sortType), HttpStatus.OK)
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

    // @todo: should probably go somewhere else (Admin?)
    private fun userIsBanned() : Boolean {
        val userDetails = AuthUtil.getUserDetails()
        val user = usersService.getUser(userDetails.discordId)
        return user != null && user.isBanned
    }

    private fun sanitiseFreetextInput(input: String) : String {
        return input
            .toLowerCase()                          // Standardise casing for cache
            .replace(Regex("[=;,'\"+]"), " ")        // Remove some SQL-specific characters for crude sanitisation
            .split(" ")                             // Break from Spring @RequestParam formatting for sorting
            .distinct()                             // Filter out duplicate entries
            .filter { it.all {
                it.isLetterOrDigit() ||             // Remove all unwanted search characters
                it == '-'                           // Keep '-' characters for languages which include them (e.g. 'pt-BR')
            } }
            .sortedBy { it }                        // Sort terms alphabetically
            .take(5)                                // Limit to five search terms max, for the sanity of this hack
            .joinToString("-")                      // Return to single string for cache entry
    }
}
