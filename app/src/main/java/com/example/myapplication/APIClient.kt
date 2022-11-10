import android.os.Build
import androidx.annotation.RequiresApi
import java.time.*
import java.time.format.DateTimeFormatter

class APIClient(token: String) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun querySoundData(sessionDate: String, from: String, to: String): Array<Pair<String, Double>> {
        val date = ZonedDateTime.now()
        val timestamp = date.format(DateTimeFormatter.ISO_DATE_TIME)
        println(timestamp)
        return arrayOf(Pair(timestamp, 100*Math.random()))
    }

}