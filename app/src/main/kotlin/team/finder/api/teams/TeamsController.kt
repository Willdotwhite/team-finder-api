package team.finder.api.teams

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    /**
     * How many records should be returned per page?
     */
    val pageSize: Int = 25

    val queryCounter: Counter = Metrics.counter("teams.counter")
    val queryTimer: Timer = Metrics.timer("teams.query")

    // TODO: Suppress reportCount from being returned from API
    @GetMapping("/teams")
    fun index(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "0") skillsetMask: Int,
        @RequestParam(defaultValue = "asc", name = "order") strSortingOption: String,
    ) : ResponseEntity<Any> {
        queryCounter.increment()

        val pageIdx = if (page > 0) page else 1

        // A power-of-2 mask being set to 0 is meaningless - AKA "do not use"
        val willPerformNativeQuery: Boolean = skillsetMask > 0
        val sort: Sort = service.getSort(strSortingOption, willPerformNativeQuery)

        // Pagination needs to be offset by -1 from expectations, but can't be set below 0
        val queryPageable: PageRequest = PageRequest.of(pageIdx - 1, pageSize, sort)

        var teams: List<Team> = listOf()

        queryTimer.record {
            teams = if (willPerformNativeQuery) {
                service.getTeams(queryPageable, skillsetMask)
            } else {
                service.getTeams(queryPageable)
            }
        }

        return ResponseEntity(teams, HttpStatus.OK)
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
        return ResponseEntity(service.getTeamByAuthorId(userDetails.discordId), HttpStatus.OK)
    }

    // TODO: Only changed fields
    @PutMapping("/teams/mine")
    fun update(@Valid @RequestBody teamDto: TeamDto, @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String) : ResponseEntity<Any> {
        if (userIsBanned()) return ResponseEntity(HttpStatus.FORBIDDEN)

        val userDetails = AuthUtil.getUserDetails()

        service.updateTeam(userDetails.discordId, teamDto.description, teamDto.skillsetMask)
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

        // TODO: Audit message about this action
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
