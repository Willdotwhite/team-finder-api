package team.finder.api.teams

import org.springframework.data.domain.Pageable
import org.springframework.data.util.Streamable
import org.springframework.stereotype.Service
import team.finder.api.utils.TimestampUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class TeamsService(val repository: TeamsRepository) {

    fun createTeam(team: Team) = repository.save(team)

    fun getTeams(pageable: Pageable): List<Team> = repository.getTeams(pageable)
    fun getTeams(pageable: Pageable, skillsetMask: Int): List<Team> = repository.getTeams(pageable, skillsetMask)

    fun getTeamById(id: Long): Optional<Team> = repository.getTeamById(id)

    fun updateTeam(id: Long, author: String, description: String, skillsetMask: Int): Team? {
        val maybeTeam = this.getTeamById(id)
        if (!maybeTeam.isPresent) {
            return null
        }

        val team = maybeTeam.get()
        team.author = author
        team.description = description
        team.skillsetMask = skillsetMask

        return repository.save(team)
    }

    fun deleteTeam(id: Long): Team? {
        // TODO: Enforce user permissions; only author (/admin?) can delete their own team

        val maybeTeam = this.getTeamById(id)
        if (!maybeTeam.isPresent) {
            return null
        }

        val team = maybeTeam.get()
        team.deletedAt = TimestampUtils.getCurrentTimeStamp()

        return repository.save(team)
    }
}
