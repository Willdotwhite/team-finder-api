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
import team.finder.api.utils.AuthUtil
import java.util.*
import javax.validation.Valid

@RestController
@CrossOrigin
class TeamsController(val service: TeamsService) {

    /**
     * How many records should be returned per page?
     */
    val pageSize: Int = 25

    val queryCounter: Counter = Metrics.counter("teams.counter")
    val queryTimer: Timer = Metrics.timer("teams.query")

    @GetMapping("/teams")
    fun index(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "0") skillsetMask: Int,
        @RequestParam(defaultValue = "asc", name = "order") strSortingOption: String,
    ) : List<Team> {
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

        return teams;
    }

    @PostMapping("/teams")
    fun add(@Valid @RequestBody teamDto: TeamDto, @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String): ResponseEntity<Any> {
        val userDetails = AuthUtil.getUserDetails()

        val authorId: String = userDetails.discordId
        if (!service.getTeamByAuthorId(authorId).isEmpty) {
            // Only one active Team per user
            return ResponseEntity(HttpStatus.CONFLICT)
        }

        teamDto.author = userDetails.name
        teamDto.authorId = authorId

        // Check this author doesn't already have a team before creating one
        service.createTeam(Team.fromDto(teamDto))
        return ResponseEntity(HttpStatus.CREATED)
    }

    @GetMapping("/teams/mine")
    fun view() : Optional<Team> {
        val userDetails = AuthUtil.getUserDetails()
        return service.getTeamByAuthorId(userDetails.discordId)
    }

    // TODO: Only changed fields
    @PutMapping("/teams/mine")
    fun update(@Valid @RequestBody teamDto: TeamDto, @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String) : ResponseEntity<Any> {
        val userDetails = AuthUtil.getUserDetails()

        service.updateTeam(userDetails.discordId, teamDto.description, teamDto.skillsetMask)
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/teams/mine")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String) : ResponseEntity<Any> {
        val userDetails = AuthUtil.getUserDetails();

        service.deleteTeam(userDetails.discordId)
        return ResponseEntity(HttpStatus.OK)
    }
}
