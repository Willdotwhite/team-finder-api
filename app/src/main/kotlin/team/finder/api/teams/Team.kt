package team.finder.api.teams

import java.time.Instant
import java.time.format.DateTimeFormatter

data class Team(val id: Int, val name: String, val skillsetMask: Int) {
    val author: String = "DiscordName#1234"
    val createdAt: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
}
