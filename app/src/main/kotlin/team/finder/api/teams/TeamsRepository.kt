package team.finder.api.teams

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
interface TeamsRepository : PagingAndSortingRepository<Team, Long> {

    fun findByDeletedAtIsNullAndDescriptionContains(pageable: Pageable, query: String): List<Team>

    // JPA doesn't handle bitwise OR very well, so a native query is the easiest way to save the hassle
    @Query("SELECT * FROM team t WHERE (t.skillset_mask & :skillsetMask > 0) AND t.deleted_at IS NULL AND t.description LIKE %:query%", nativeQuery = true)
    fun getTeams(pageable: Pageable, query: String, skillsetMask: Int): List<Team>

    @Query("SELECT t FROM Team t WHERE t.authorId = :id AND t.deletedAt IS NULL")
    fun getTeamByAuthorId(id: String): Team?

    fun findByAuthorIdAndDeletedAtIsNotNull(authorId: String): Team?

    fun findByIdAndDeletedAtIsNull(teamId: Long): Team?
    fun findByIdAndDeletedAtIsNotNull(teamId: Long): Team?

    @Query("SELECT t FROM Team t WHERE t.reportCount > 0 AND t.deletedAt IS NULL ORDER BY t.reportCount DESC")
    fun getTeamsWithReports(): List<Team>

}
