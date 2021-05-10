package team.finder.api.teams

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
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
    fun add(@Valid @RequestBody teamDto: TeamDto) = service.createTeam(Team.fromDto(teamDto))

    @GetMapping("/teams/{id}")
    fun view(@PathVariable id: Long) : Optional<Team> = service.getTeamById(id)

    // TODO: Only changed fields
    @PutMapping("/teams/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody teamDto: TeamDto) = service.updateTeam(id, teamDto.author, teamDto.description, teamDto.skillsetMask)

    @DeleteMapping("/teams/{id}")
    fun delete(@PathVariable id: Long) : Team? = service.deleteTeam(id)
}
