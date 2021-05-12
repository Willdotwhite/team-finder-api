package team.finder.api.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimestampUtils {
    companion object {
        fun getCurrentTimeStamp(): String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}
