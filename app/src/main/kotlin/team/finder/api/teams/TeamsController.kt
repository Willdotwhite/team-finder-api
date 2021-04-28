package team.finder.api.teams

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TeamsController {

    val teams = mapOf(
        1 to Team(1, "Test Name 1"),
        2 to Team(2, "Test Name 2"),
        3 to Team(3, "Test Name 3"),
        4 to Team(4, "Test Name 4"),
        5 to Team(5, "Test Name 5"),
        6 to Team(6, "Test Name 6"),
        7 to Team(7, "Test Name 7"),
        8 to Team(8, "Test Name 8"),
        9 to Team(9, "Test Name 9"),
        10 to Team(10, "Test Name 10"),
        11 to Team(11, "Test Name 11"),
        12 to Team(12, "Test Name 12"),
    )

    @GetMapping("/teams")
    fun index() : Map<Int, Team> = teams

    @GetMapping("/teams/{id}")
    fun view(@PathVariable id: Int) : Team? = teams[id]
}
