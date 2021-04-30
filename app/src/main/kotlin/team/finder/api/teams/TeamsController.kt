package team.finder.api.teams

import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class TeamsController(val service: TeamsService) {

    @GetMapping("/teams")
    fun index() : MutableIterable<Team> = service.getTeams()

    @GetMapping("/teams/{id}")
    fun view(@PathVariable id: Long) : Optional<Team> = service.getTeamById(id)

    @PostMapping("/teams")
    fun add(@RequestBody teamDto: TeamDto) = service.createTeam(Team.fromDto(teamDto))
}
