package team.finder.api.teams

import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletResponse


@RestController
class TeamsController(val service: TeamsService) {
    @ModelAttribute
    fun setResponseHeader(response: HttpServletResponse) {
//        this isn't ideal but it's kind of fine, it means anybody can use the API from a locally hosted webpage
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000")
    }

    @GetMapping("/teams")
    fun index() : MutableIterable<Team> = service.getTeams()

    @PostMapping("/teams")
    fun add(@RequestBody teamDto: TeamDto) = service.createTeam(Team.fromDto(teamDto))

    @GetMapping("/teams/{id}")
    fun view(@PathVariable id: Long) : Optional<Team> = service.getTeamById(id)

    @PutMapping("/teams/{id}")
    fun update(@PathVariable id: Long, @RequestBody teamDto: TeamDto) = service.updateTeam(id, teamDto.name)
}
