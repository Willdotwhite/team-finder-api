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

    @GetMapping("/teams/{authorId}")
    fun view(@PathVariable authorId: Long) : Optional<Team> = service.getTeamByAuthorId(authorId)

    // TODO: Only changed fields
    @PutMapping("/teams/{authorId}")
    fun update(@PathVariable authorId: Long, @Valid @RequestBody teamDto: TeamDto, @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String) : ResponseEntity<Any> {
        val userDetails = AuthUtil.getUserDetails()

        // You can only update your own team!
        if (authorId != userDetails.discordId) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }

        // Don't allow a user to change the owner of the team
        // Instead of faffing with the DTO, we can just overwrite the potentially-changed fields
        teamDto.author = userDetails.name
        teamDto.authorId = authorId

        service.updateTeam(authorId, teamDto.description, teamDto.skillsetMask)
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/teams/{authorId}")
    fun delete(@PathVariable authorId: Long, @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String) : ResponseEntity<Any> {
        val userDetails = AuthUtil.getUserDetails();

        // You can only delete your own team!
        if (authorId != userDetails.discordId) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }

        service.deleteTeam(authorId)
        return ResponseEntity(HttpStatus.OK)
    }
}
