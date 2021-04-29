package team.finder.api.teams

data class Team(val id: Int, val name: String, val roleId: Int) {
    val author: String = "Some nerd"
}
