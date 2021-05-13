package team.finder.api.teams

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import team.finder.api.utils.TimestampUtils
import java.util.*

@Service
class TeamsService(val repository: TeamsRepository) {

    fun createTeam(team: Team) = repository.save(team)

    fun getTeams(pageable: Pageable): List<Team> = repository.getTeams(pageable)
    fun getTeams(pageable: Pageable, skillsetMask: Int): List<Team> = repository.getTeams(pageable, skillsetMask)

    fun getTeamByAuthorId(authorId: Long): Optional<Team> = repository.getTeamByAuthorId(authorId)

    fun updateTeam(authorId: Long, description: String, skillsetMask: Int): Team? {
        val maybeTeam = this.getTeamByAuthorId(authorId)
        if (!maybeTeam.isPresent) {
            return null
        }

        val team = maybeTeam.get()
        team.description = description
        team.skillsetMask = skillsetMask

        return repository.save(team)
    }

    fun deleteTeam(id: Long): Team? {
        // TODO: Enforce user permissions; only author (/admin?) can delete their own team

        val maybeTeam = this.getTeamByAuthorId(id)
        if (!maybeTeam.isPresent) {
            return null
        }

        val team = maybeTeam.get()
        team.deletedAt = TimestampUtils.getCurrentTimeStamp()

        return repository.save(team)
    }
}
