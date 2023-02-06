import Amplitude.getAmplitude
import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.Integer.max
import java.time.*
import java.time.format.DateTimeFormatter

class APIClient(token: String) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun querySoundData(sessionDate: String, from: String, to: String): MutableList<Pair<String, Double>> {
        val cloudData = mutableListOf<Pair<String, Double>>()
        val startDatetime = ZonedDateTime.parse(from)
        val endDatetime = ZonedDateTime.parse(to)
        val minutes: Int = timeDifferenceInMinutes(startDatetime, endDatetime)
        val dataPoints: Int = max((minutes*Math.random()).toInt() - 1, 1)
        for(i in 0..dataPoints){
            cloudData.add(Pair( addTime(startDatetime, i), getAmplitude()))
        }
        cloudData.add(Pair(to, getAmplitude()))
        return cloudData;
    }


    // assuming this would only be called when the dates are the same
    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeDifferenceInMinutes(startDatetime: ZonedDateTime, endDatetime: ZonedDateTime): Int {
        return (endDatetime.hour - startDatetime.hour)*60 + endDatetime.minute - startDatetime.minute

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTime(datetime: ZonedDateTime, minutes: Int): String {
        return datetime.minusMinutes(-minutes.toLong()).format(DateTimeFormatter.ISO_DATE_TIME)
    }

}
object Amplitude{
    fun getAmplitude(): Double {
        return 100*Math.random()
    }
}