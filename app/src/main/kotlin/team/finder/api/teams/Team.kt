package team.finder.api.teams

import team.finder.api.utils.TimestampUtils
import javax.persistence.*
import java.io.Serializable as JavaSerializable


@Entity
@Table(name = "team")
class Team (
    var author: String?,
    var authorId: String?,
    var description: String,
    var skillsetMask: Int,

    var languages: String,

    // Managed by DB
    val createdAt: String,
    var updatedAt: String,
    var deletedAt: String?,

    var reportCount: Int,

    @Id val id: Long
) : JavaSerializable {

    constructor(_author: String?, _authorId: String?, _description: String, _skillsetMask: Int, _languages: String) :
            this(_author, _authorId, _description, _skillsetMask, _languages, TimestampUtils.getCurrentTimeStamp(), TimestampUtils.getCurrentTimeStamp(), null, 0, 0)

    constructor() : this("", "1", "", 1, Language.en.toString())

    companion object {
        fun fromDto(teamDto: TeamDto) = Team(
            teamDto.author,
            teamDto.authorId,
            teamDto.description,
            teamDto.skillsetMask,
            teamDto.languages.joinToString(",")
        )
    }
}
