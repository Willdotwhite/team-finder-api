package team.finder.api.system

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatusController {

    @GetMapping("/_system")
    fun status(): Map<String, String> {
        return mapOf("status" to "OK");
    }
}
