package team.finder.api.teams

import team.finder.api.utils.TimestampUtils
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "team")
class Team(
    var author: String?,
    var authorId: String?,
    var description: String,
    var skillsetMask: Int,

    // Managed by DB
    val createdAt: String,
    var updatedAt: String,
    var deletedAt: String?,

    @Id val id: Long
) {

    constructor(_author: String?, _authorId: Long?, _description: String, _skillsetMask: Int) :
            this(_author, _authorId.toString(), _description, _skillsetMask, TimestampUtils.getCurrentTimeStamp(), TimestampUtils.getCurrentTimeStamp(), null, 0)

    constructor() : this("", 1, "", 1)

    companion object {
        fun fromDto(teamDto: TeamDto) = Team(teamDto.author, teamDto.authorId, teamDto.description, teamDto.skillsetMask)
    }
}
