package team.finder.api.teams

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class TeamsService(val repository: TeamsRepository) {

    fun createTeam(team: Team) = repository.save(team)

    fun getTeams(skillsetMask: Int?): List<Team> {
        var records = repository.findAll().filter { team -> team?.deletedAt.equals(null) }

        if (skillsetMask != null) {
            // Remove all records that don't match the skillsetMask provided
            // In the case where skillsetMask is multiple roles,
            // this will return all teams asking for _both_, not _either_
            records = records.filter { team -> (team.skillsetMask and skillsetMask) == skillsetMask }
        }

        return records
    }

    fun getTeamById(id: Long): Optional<Team> = repository.findById(id).filter { team -> team?.deletedAt.equals(null) }

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
        team.deletedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return repository.save(team)
    }
}
