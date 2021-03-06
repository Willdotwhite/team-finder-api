package team.finder.api.users

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UsersRepository : CrudRepository<User, String> {

    fun findByDiscordId(discordId: String): User?
    fun findByIsBanned(isBanned: Boolean): List<User>
}
