package team.finder.api.teams

import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Team(var name: String, var skillsetMask: Int, @Id @GeneratedValue var id: Long? = null) {
    constructor() : this("", 0)

    companion object {
        fun fromDto(teamDto: TeamDto) = Team(teamDto.name, 0)
    }

    val author: String = "DiscordName#1234"
    val createdAt: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
}
