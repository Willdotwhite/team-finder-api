package team.finder.api.teams

import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
class TeamsController(val service: TeamsService) {

    @GetMapping("/teams")
    fun index(@RequestParam skillsetMask: Int?) : List<Team> = service.getTeams(skillsetMask)

    @PostMapping("/teams")
    fun add(@RequestBody teamDto: TeamDto) = service.createTeam(Team.fromDto(teamDto))

    @GetMapping("/teams/{id}")
    fun view(@PathVariable id: Long) : Optional<Team> = service.getTeamById(id)

    // TODO: Only changed fields
    @PutMapping("/teams/{id}")
    fun update(@PathVariable id: Long, @RequestBody teamDto: TeamDto) = service.updateTeam(id, teamDto.author, teamDto.description, teamDto.skillsetMask)

    @DeleteMapping("/teams/{id}")
    fun delete(@PathVariable id: Long) : Team? = service.deleteTeam(id)
}
