package team.finder.api.teams

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import team.finder.api.utils.TimestampUtils
import java.util.*


@Service
class TeamsService(val repository: TeamsRepository) {

    fun createTeam(team: Team) = repository.save(team)
    fun saveTeam(team: Team) = repository.save(team) // Yeah, I know...

    fun getTeams(pageable: Pageable): List<Team> = repository.getTeams(pageable)
    fun getTeams(pageable: Pageable, skillsetMask: Int): List<Team> = repository.getTeams(pageable, skillsetMask)

    fun getTeamByAuthorId(authorId: String): Optional<Team> = repository.getTeamByAuthorId(authorId)
    fun getTeamById(teamId: Long): Team? = repository.getTeamById(teamId)
    fun getTeamsWithActiveReports(): List<Team> = repository.getTeamsWithReports()

    fun updateTeam(authorId: String, description: String, skillsetMask: Int): Team? {
        val maybeTeam = this.getTeamByAuthorId(authorId)
        if (!maybeTeam.isPresent) {
            return null
        }

        val team = maybeTeam.get()
        team.description = description
        team.skillsetMask = skillsetMask

        // This doesn't always get updated in the DB, not sure why
        team.updatedAt = TimestampUtils.getCurrentTimeStamp()

        return repository.save(team)
    }

    fun deleteTeam(id: String): Team? {
        // TODO: Enforce user permissions; only author (/admin?) can delete their own team

        val maybeTeam = this.getTeamByAuthorId(id)
        if (!maybeTeam.isPresent) {
            return null
        }

        val team = maybeTeam.get()
        team.deletedAt = TimestampUtils.getCurrentTimeStamp()

        return repository.save(team)
    }

    fun getSort(strSortingOption: String, isNativeQuery: Boolean): Sort {

        val updatedColumnName = if (isNativeQuery) "updated_at" else "updatedAt"
        val authorColumnName = if (isNativeQuery) "author_id" else "authorId"

        return when (getSortType(strSortingOption)) {
            SortingOptions.Asc -> Sort.by(updatedColumnName).ascending()
            SortingOptions.Desc -> Sort.by(updatedColumnName).descending()
            // Obviously not random, apparently Kotlin Comparators require that the results are reproducible
            // I've probably misunderstood, but for users it'll probably look random enough (just consistently so)
            SortingOptions.Random -> Sort.by(authorColumnName)
        }
    }

    /**
     * Cast user input to known type to avoid concerns about data validation
     */
    fun getSortType(input: String): SortingOptions {
        return when(input.toLowerCase()) {
            "asc"       -> SortingOptions.Asc
            "desc"      -> SortingOptions.Desc
            "random"    -> SortingOptions.Random
            else        -> SortingOptions.Desc
        }
    }
}
