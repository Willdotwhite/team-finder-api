package team.finder.api.teams

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import team.finder.api.utils.TimestampUtils


@Service
class TeamsService(val repository: TeamsRepository) {

    private val logger: Logger = LoggerFactory.getLogger(TeamsService::class.java)

    /**
     * How many records should be returned per page?
     */
    val pageSize: Int = 25

    val queryCounter: Counter = Metrics.counter("teams.counter")
    val queryTimer: Timer = Metrics.timer("teams.query")


    fun createTeam(team: Team) = repository.save(team)
    fun saveTeam(team: Team) = repository.save(team) // Yeah, I know...

    @Cacheable("teams")
    fun getTeams(query: String, pageIdx: Int, skillsetMask: Int, sortingOption: SortingOptions): List<Team> {
        queryCounter.increment()

        // If we have a 'query' param to search for, use the native query option for ease of expanding multiple
        // keywords into a single query statement
        val willPerformNativeQuery: Boolean = skillsetMask > 0 || query.isNotEmpty()
        val sort: Sort = getSort(sortingOption, willPerformNativeQuery)

        // Pagination needs to be offset by -1 from expectations, but can't be set below 0
        val queryPageable: PageRequest = PageRequest.of(pageIdx - 1, pageSize, sort)

        var teams: List<Team> = listOf()


        queryTimer.record {
            teams = if (willPerformNativeQuery) {
                if (query.isNotEmpty()) {
                    logger.info("[QUERY] Custom query used: $query")
                }

                // The query term will be wrapped in \' characters by JPA, so we need to set the terms of the LIKE here
                // i.e. LIKE '%game%' for a global search
                val queryTerms = query.split("-").map { "%$it%" }

                // If we're using the native query for a given keyword search term, use a mask of b111... to allow everything
                val querySkillsetMask = if (query.isNotEmpty() && skillsetMask == 0) 255 else skillsetMask

                // There's a nicer way to do it, but I have no idea how
                when (queryTerms.size) {
                    1 ->    repository.getTeams(queryPageable, querySkillsetMask, queryTerms[0]) // An empty search becomes "LIKE '%%'", and gets everything
                    2 ->    repository.getTeams(queryPageable, querySkillsetMask, queryTerms[0], queryTerms[1])
                    3 ->    repository.getTeams(queryPageable, querySkillsetMask, queryTerms[0], queryTerms[1], queryTerms[2])
                    4 ->    repository.getTeams(queryPageable, querySkillsetMask, queryTerms[0], queryTerms[1], queryTerms[2], queryTerms[3])
                    5 ->    repository.getTeams(queryPageable, querySkillsetMask, queryTerms[0], queryTerms[1], queryTerms[2], queryTerms[3], queryTerms[4])
                    else -> repository.getTeams(queryPageable, querySkillsetMask, queryTerms[0], queryTerms[1], queryTerms[2], queryTerms[3], queryTerms[4])
                }
            } else {
                repository.findByDeletedAtIsNullAndDescriptionContains(queryPageable, query)
            }
        }

        return teams
    }

    fun getTeamByAuthorId(authorId: String): Team? = repository.getTeamByAuthorId(authorId)
    fun getDeletedTeamByAuthorId(authorId: String): Team? = repository.findByAuthorIdAndDeletedAtIsNotNull(authorId)
    fun getTeamById(teamId: Long): Team? = repository.findByIdAndDeletedAtIsNull(teamId)
    fun getDeletedTeamById(teamId: Long): Team? = repository.findByIdAndDeletedAtIsNotNull(teamId)
    fun getTeamsWithActiveReports(): List<Team> = repository.getTeamsWithReports()

    fun updateTeam(authorId: String, description: String, skillsetMask: Int, languages: Collection<Language>): Team? {
        val team = this.getTeamByAuthorId(authorId) ?: return null

        team.description = description
        team.skillsetMask = skillsetMask
        team.languages = languages.joinToString(",")

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
