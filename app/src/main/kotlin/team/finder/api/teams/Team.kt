package team.finder.api.teams

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Team(
    var author: String,
    var description: String,
    var skillsetMask: Int,
    @Id @GeneratedValue var id: Long? = null
) {
    constructor(a: String, d: String, s: Int) : this(a, d, s, 0)

    constructor() : this("", "", 1)

    companion object {
        fun fromDto(teamDto: TeamDto) = Team(teamDto.author, teamDto.description, teamDto.skillsetMask)
    }
}
