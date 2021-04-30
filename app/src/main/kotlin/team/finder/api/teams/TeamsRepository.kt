package team.finder.api.teams

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamsRepository : CrudRepository<Team, Long>
