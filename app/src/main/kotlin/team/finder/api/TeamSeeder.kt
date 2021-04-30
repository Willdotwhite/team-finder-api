package team.finder.api

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import team.finder.api.teams.Team
import team.finder.api.teams.TeamsRepository

@Component
@Profile("dev")
class TeamSeeder(val teamsRepository: TeamsRepository) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val teams = listOf(
                Team("Test Name 1", 33, 1),
                Team("Test Name 2", 96, 2),
                Team("Test Name 3", 127, 3),
                Team("Test Name 4", 44, 4),
                Team("Test Name 5", 17, 5),
                Team("Test Name 6", 77, 6),
                Team("Test Name 7", 8, 7),
                Team("Test Name 8", 25, 8),
                Team("Test Name 9", 83, 9),
                Team("Test Name 10", 101, 10),
                Team("Test Name 11", 33, 11),
                Team("Test Name 12", 63, 12)
        )
        this.teamsRepository.saveAll(teams)
        println("Seeded teams for Dev")
    }

}