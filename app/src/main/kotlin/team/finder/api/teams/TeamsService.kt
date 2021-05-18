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
import java.util.*


@Service
class TeamsService(val repository: TeamsRepository) {

    /**
     * How many records should be returned per page?
     */
    val pageSize: Int = 25

    val queryCounter: Counter = Metrics.counter("teams.counter")
    val queryTimer: Timer = Metrics.timer("teams.query")


    fun createTeam(team: Team) = repository.save(team)

    @Cacheable("teams")
    fun getTeams(pageIdx: Int, skillsetMask: Int, strSortingOption: String): List<Team> {
        queryCounter.increment()

        // A power-of-2 mask being set to 0 is meaningless - AKA "do not use"
        val willPerformNativeQuery: Boolean = skillsetMask > 0
        val sort: Sort = getSort(strSortingOption, willPerformNativeQuery)

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
