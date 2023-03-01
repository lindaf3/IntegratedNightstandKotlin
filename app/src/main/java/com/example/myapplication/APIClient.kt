import Amplitude.getAmplitude
import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.Integer.max
import java.time.*
import java.time.format.DateTimeFormatter

class APIClient(token: String) {
    var recentStartDateTime: String = ""
    var recentEndDateTime: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(){
        val startDatetime = LocalDateTime.now()
        recentStartDateTime = startDatetime.format(DateTimeFormatter.ISO_DATE_TIME)
        val endDatetime = startDatetime.minusHours(-1)
        recentEndDateTime = endDatetime.format(DateTimeFormatter.ISO_DATE_TIME)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun querySoundData(from: String, to: String): MutableList<Pair<String, Double>> {
        val cloudData = mutableListOf<Pair<String, Double>>()
        val startDatetime = LocalDateTime.parse(from)
        val endDatetime = LocalDateTime.parse(to)
        val minutes: Int = timeDifferenceInMinutes(startDatetime, endDatetime)
        val dataPoints: Int = max((minutes*Math.random()).toInt() - 1, 1)
        for(i in 0..dataPoints){
            cloudData.add(Pair( addTime(startDatetime, i), getAmplitude()))
        }
        cloudData.add(Pair(to, getAmplitude()))
        return cloudData;
    }

    // Strings that can be parsed into local datetimes
    @RequiresApi(Build.VERSION_CODES.O)
    fun queryInterval(): Pair<String, String> {
        return Pair(recentStartDateTime, recentEndDateTime)
    }

    // String: strings that can be parsed into LocalDatetime
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateInterval(from: String, to: String) {
        recentStartDateTime = from
        recentEndDateTime = to
    }


    // assuming this would only be called when the dates are the same
    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeDifferenceInMinutes(startDatetime: LocalDateTime, endDatetime: LocalDateTime): Int {
        return (endDatetime.hour - startDatetime.hour)*60 + endDatetime.minute - startDatetime.minute

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTime(datetime: LocalDateTime, minutes: Int): String {
        return datetime.minusMinutes(-minutes.toLong()).format(DateTimeFormatter.ISO_DATE_TIME)
    }

}
object Amplitude{
    fun getAmplitude(): Double {
        return 5.0*Math.random()
    }
}