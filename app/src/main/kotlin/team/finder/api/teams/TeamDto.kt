package team.finder.api.teams

import javax.validation.constraints.Max
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

class TeamDto(
    @NotNull
    @Pattern(regexp="^.{3,32}#[0-9]{4}\$",message="username must follow Discord Username guidelines") //3-32 of any chars followed by # followed by exactly 4 numbers
    var author: String,

    @NotNull
    @Max(140)
    var description: String,

    @Max(1048575)
    var skillsetMask: Int,
)
