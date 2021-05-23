package team.finder.api.users

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class User(
    var name: String,
    @Id val discordId: String,
    val isAdmin: Boolean,
    var isBanned: Boolean,
) : org.springframework.security.core.userdetails.User(name, "", emptyList())  {
    constructor() : this("dummy", "123", false, false)
}

