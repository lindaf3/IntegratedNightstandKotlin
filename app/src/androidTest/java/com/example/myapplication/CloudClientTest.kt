import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.Duration
import java.time.format.DateTimeFormatter
import CloudClient

fun main() {
    val cloudClient = CloudClient("/path/key.pem", "longAPIToken")

    val localDate = LocalDateTime.of(2022, 1, 3, 16, 36)
    //Convert local datetime from PST to UTC (make it adaptive in-app)
    val offsetDate = OffsetDateTime.of(localDate + Duration.ofHours(-8), ZoneOffset.UTC)
    val dateString = offsetDate.format(DateTimeFormatter.ISO_DATE)
    val timestamp = offsetDate.format(DateTimeFormatter.ISO_DATE_TIME)
    val data: Array<Pair<String, Double>> = arrayOf(Pair(timestamp, 0.0))


//    cloudClient.uploadSoundData(data)

    val cloudData = cloudClient.querySoundData(dateString)
    // visualize cloud data in app
}