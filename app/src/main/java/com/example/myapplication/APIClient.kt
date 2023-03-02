import Amplitude.getAmplitude
import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.Integer.max
import java.lang.Integer.min
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class APIClient(token: String) {
    var recentStartDateTime: String = ""
    var recentEndDateTime: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(){
        val startDatetime = LocalDateTime.now()
        recentStartDateTime = startDatetime.format(DateTimeFormatter.ISO_DATE_TIME)
        val endDatetime = startDatetime.minusHours(-1)
        recentEndDateTime = endDatetime.format(DateTimeFormatter.ISO_DATE_TIME)
        println(recentStartDateTime)
        println(recentEndDateTime)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun querySoundData(from: String, to: String): MutableList<Pair<String, Double>> {
        val cloudData = mutableListOf<Pair<String, Double>>()
        val startDateTime = LocalDateTime.parse(from)
        val endDateTime = LocalDateTime.parse(to)


        val minutes = ChronoUnit.MINUTES.between(startDateTime, endDateTime)
        val maxDataPoints = (Math.random()*50).toInt()
        val minutesList = mutableListOf<Long>()
        for (i in 0.. maxDataPoints){
            minutesList.add((minutes*Math.random() + 1).toLong())
        }
        minutesList.sort()
        for(i in 0..maxDataPoints){
            val randomDatetime = endDateTime.minusMinutes(minutesList[i])
            if(randomDatetime != startDateTime){
                cloudData.add(Pair(randomDatetime.format(DateTimeFormatter.ISO_DATE_TIME), getAmplitude()))
            }

        }
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


}
object Amplitude{
    fun getAmplitude(): Double {
        return 5.0*Math.random()
    }
}