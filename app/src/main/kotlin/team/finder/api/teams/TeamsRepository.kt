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
    @Query("SELECT * FROM team t WHERE (t.skillset_mask & :skillsetMask > 0) AND t.deleted_at IS NULL AND t.description LIKE :s1", nativeQuery = true)
    fun getTeams(pageable: Pageable, skillsetMask: Int, s1: String): List<Team>

    @Query("SELECT * FROM team t WHERE (t.skillset_mask & :skillsetMask > 0) AND t.deleted_at IS NULL AND t.description LIKE :s1 AND t.description LIKE :s2", nativeQuery = true)
    fun getTeams(pageable: Pageable, skillsetMask: Int, s1: String, s2: String): List<Team>

    @Query("SELECT * FROM team t WHERE (t.skillset_mask & :skillsetMask > 0) AND t.deleted_at IS NULL AND t.description LIKE :s1 AND t.description LIKE :s2 AND t.description LIKE :s3", nativeQuery = true)
    fun getTeams(pageable: Pageable, skillsetMask: Int, s1: String, s2: String, s3: String): List<Team>

    @Query("SELECT * FROM team t WHERE (t.skillset_mask & :skillsetMask > 0) AND t.deleted_at IS NULL AND t.description LIKE :s1 AND t.description LIKE :s2 AND t.description LIKE :s3 AND t.description LIKE :s4", nativeQuery = true)
    fun getTeams(pageable: Pageable, skillsetMask: Int, s1: String, s2: String, s3: String, s4: String): List<Team>

    @Query("SELECT * FROM team t WHERE (t.skillset_mask & :skillsetMask > 0) AND t.deleted_at IS NULL AND t.description LIKE :s1 AND t.description LIKE :s2 AND t.description LIKE :s3 AND t.description LIKE :s4 AND t.description LIKE :s5", nativeQuery = true)
    fun getTeams(pageable: Pageable, skillsetMask: Int, s1: String, s2: String, s3: String, s4: String, s5: String): List<Team>


    @Query("SELECT t FROM Team t WHERE t.authorId = :id AND t.deletedAt IS NULL")
    fun getTeamByAuthorId(id: String): Team?

    fun findByAuthorIdAndDeletedAtIsNotNull(authorId: String): Team?

    fun findByIdAndDeletedAtIsNull(teamId: Long): Team?
    fun findByIdAndDeletedAtIsNotNull(teamId: Long): Team?

    @Query("SELECT t FROM Team t WHERE t.reportCount > 0 AND t.deletedAt IS NULL ORDER BY t.reportCount DESC")
    fun getTeamsWithReports(): List<Team>

}
