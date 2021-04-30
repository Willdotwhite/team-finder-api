package team.finder.api.teams

import org.springframework.stereotype.Service

@Service
class TeamsService(val repository: TeamsRepository) {
    fun createTeam(team: Team) = repository.save(team)
    fun getTeams(): MutableIterable<Team> = repository.findAll()
    fun getTeamById(id: Long) = repository.findById(id)
    fun updateTeam(id: Long, name: String): Team? {
        val maybeTeam = this.getTeamById(id)
        if (!maybeTeam.isPresent) {
            return null
        }
        val team = maybeTeam.get()
        team.name = name
        return repository.save(team)
    }
}
