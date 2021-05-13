package team.finder.api.teams

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional
interface TeamsRepository : PagingAndSortingRepository<Team, Long> {

    @Query("SELECT t FROM Team t WHERE t.deletedAt IS NULL")
    fun getTeams(pageable: Pageable): List<Team>

    // JPA doesn't handle bitwise OR very well, so a native query is the easiest way to save the hassle
    @Query("SELECT * FROM team t WHERE (t.skillset_mask & :skillsetMask = :skillsetMask) AND t.deleted_at IS NULL", nativeQuery = true)
    fun getTeams(pageable: Pageable, skillsetMask: Int): List<Team>

    @Query("SELECT t FROM Team t WHERE t.authorId = :id AND t.deletedAt IS NULL")
    fun getTeamByAuthorId(id: Long): Optional<Team>
}
