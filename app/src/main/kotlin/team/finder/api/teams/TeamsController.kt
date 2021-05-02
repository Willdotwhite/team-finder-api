package team.finder.api.teams

import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletResponse


//        this isn't ideal but it's kind of fine, it means anybody can use the API from a locally hosted webpage
@CrossOrigin("localhost:3000")
@RestController
class TeamsController(val service: TeamsService) {

    @GetMapping("/teams")
    fun index() : MutableIterable<Team> = service.getTeams()

    @PostMapping("/teams")
    fun add(@RequestBody teamDto: TeamDto) = service.createTeam(Team.fromDto(teamDto))

    @GetMapping("/teams/{id}")
    fun view(@PathVariable id: Long) : Optional<Team> = service.getTeamById(id)

    // TODO: Only changed fields
    @PutMapping("/teams/{id}")
    fun update(@PathVariable id: Long, @RequestBody teamDto: TeamDto) = service.updateTeam(id, teamDto.author, teamDto.description, teamDto.skillsetMask)
}
