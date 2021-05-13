package team.finder.api.teams

import com.nimbusds.jwt.SignedJWT
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import team.finder.api.utils.AuthUtil
import java.util.*
import javax.validation.Valid

@RestController
@CrossOrigin
class TeamsController(val service: TeamsService) {

    val queryCounter: Counter = Metrics.counter("teams.counter")
    val queryTimer: Timer = Metrics.timer("teams.query")

    @GetMapping("/teams")
    fun index(@RequestParam(defaultValue = "1") page: Int, @RequestParam skillsetMask: Int?) : List<Team> {
        queryCounter.increment()

        val pageIdx = if (page > 0) page else 1

        // Pagination needs to be offset by -1 from expectations, but can't be set below 0
        val queryPageable: PageRequest = PageRequest.of(pageIdx - 1, 50)

        var teams: List<Team> = listOf()

        queryTimer.record {
            teams = if (skillsetMask?.equals(null) == false) {
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

        val authorId: Long = userDetails.discordId as Long
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
