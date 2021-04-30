package team.finder.api.teams

import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class TeamsController(val service: TeamsService) {

    @GetMapping("/teams")
    fun index() : MutableIterable<Team> = service.getTeams()

    @PostMapping("/teams")
    fun add(@RequestBody teamDto: TeamDto) = service.createTeam(Team.fromDto(teamDto))

    @GetMapping("/teams/{id}")
    fun view(@PathVariable id: Long) : Optional<Team> = service.getTeamById(id)

    @PutMapping("/teams/{id}")
    fun update(@PathVariable id: Long, @RequestBody teamDto: TeamDto) = service.updateTeam(id, teamDto.name)
}
