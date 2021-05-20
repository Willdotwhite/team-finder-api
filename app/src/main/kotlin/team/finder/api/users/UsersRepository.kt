package team.finder.api.users

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UsersRepository : CrudRepository<User, String> {

    @Query("SELECT u FROM User u WHERE u.discordId = :discordId")
    fun getUser(discordId: String): User?
}
