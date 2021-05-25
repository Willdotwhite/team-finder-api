package team.finder.api.teams

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import team.finder.api.utils.TimestampUtils


@Service
class TeamsService(val repository: TeamsRepository) {

    /**
     * How many records should be returned per page?
     */
    val pageSize: Int = 25

    val queryCounter: Counter = Metrics.counter("teams.counter")
    val queryTimer: Timer = Metrics.timer("teams.query")


    fun createTeam(team: Team) = repository.save(team)
    fun saveTeam(team: Team) = repository.save(team) // Yeah, I know...

    @Cacheable("teams")
    fun getTeams(pageIdx: Int, skillsetMask: Int, sortingOption: SortingOptions): List<Team> {
        queryCounter.increment()

        // A power-of-2 mask being set to 0 is meaningless - AKA "do not use"
        val willPerformNativeQuery: Boolean = skillsetMask > 0
        val sort: Sort = getSort(sortingOption, willPerformNativeQuery)

        // Pagination needs to be offset by -1 from expectations, but can't be set below 0
        val queryPageable: PageRequest = PageRequest.of(pageIdx - 1, pageSize, sort)

        var teams: List<Team> = listOf()


        queryTimer.record {
            teams = if (willPerformNativeQuery) {
                repository.getTeams(queryPageable, skillsetMask)
            } else {
                repository.getTeams(queryPageable)
            }
        }

        return teams
    }

    fun getTeamByAuthorId(authorId: String): Team? = repository.getTeamByAuthorId(authorId)
    fun getTeamById(teamId: Long): Team? = repository.findByIdAndDeletedAtIsNull(teamId)
    fun getTeamsWithActiveReports(): List<Team> = repository.getTeamsWithReports()

    fun updateTeam(authorId: String, description: String, skillsetMask: Int): Team? {
        val team = this.getTeamByAuthorId(authorId) ?: return null

        team.description = description
        team.skillsetMask = skillsetMask

        // This doesn't always get updated in the DB, not sure why
        team.updatedAt = TimestampUtils.getCurrentTimeStamp()

        return repository.save(team)
    }

    fun deleteTeam(authorId: String): Team? {
        val team = this.getTeamByAuthorId(authorId) ?: return null
        team.deletedAt = TimestampUtils.getCurrentTimeStamp()

        return repository.save(team)
    }

    fun getSort(sortingOption: SortingOptions, isNativeQuery: Boolean): Sort {

        val updatedColumnName = if (isNativeQuery) "updated_at" else "updatedAt"
        val authorColumnName = if (isNativeQuery) "author_id" else "authorId"

        return when (sortingOption) {
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
