package team.finder.api.teams

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@RestController
class TeamsController(val service: TeamsService) {

    @GetMapping("/teams")
    fun index(@RequestParam(defaultValue = "1") page: Int, @RequestParam skillsetMask: Int?) : List<Team> {

        // Pagination needs to be offset by -1 from expectations, but can't be set below 0
        val pageIdx = if (page > 0) page else 1
        val queryPageable: PageRequest = PageRequest.of(pageIdx - 1, 50)

        return if (skillsetMask?.equals(null) == false) {
            service.getTeams(queryPageable, skillsetMask)
        } else {
            service.getTeams(queryPageable)
        }
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
