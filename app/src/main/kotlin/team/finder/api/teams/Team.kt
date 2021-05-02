package team.finder.api.teams

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Team(
    var author: String,
    var description: String,
    var skillsetMask: Int,

    // Managed by DB
    val createdAt: String,
    var updatedAt: String,
    var deletedAt: String,

    @Id val id: Long
) {

    constructor(_author: String, _description: String, _skillsetMask: Int) :
            this(_author, _description, _skillsetMask, "", "", "", 0)

    constructor() : this("", "", 1)

    companion object {
        fun fromDto(teamDto: TeamDto) = Team(teamDto.author, teamDto.description, teamDto.skillsetMask)
    }
}
