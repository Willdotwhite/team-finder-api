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
                Team("TestUser#1234", "Test Name 1", 33, 1),
                Team("TestUser#1234", "Test Name 2", 96, 2),
                Team("TestUser#1234", "Test Name 3", 127, 3),
                Team("TestUser#1234", "Test Name 4", 44, 4),
                Team("TestUser#1234", "Test Name 5", 17, 5),
                Team("TestUser#1234", "Test Name 6", 77, 6),
                Team("TestUser#1234", "Test Name 7", 8, 7),
                Team("TestUser#1234", "Test Name 8", 25, 8),
                Team("TestUser#1234", "Test Name 9", 83, 9),
                Team("TestUser#1234", "Test Name 10", 101, 10),
                Team("TestUser#1234", "Test Name 11", 33, 11),
                Team("TestUser#1234", "Test Name 12", 63, 12)
        )
        this.teamsRepository.saveAll(teams)
        println("Seeded teams for Dev")
    }

}
