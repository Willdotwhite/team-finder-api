package team.finder.api.users

import org.springframework.stereotype.Service
import team.finder.api.users.UsersRepository


@Service
class UsersService(val repository: UsersRepository) {

    fun getUser(discordId: String) = repository.getUser(discordId)

}
