import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class CloudClient(key: String, token: String) {
    val iotClient = IoTClient(key);
    val apiClient = APIClient(token);


    @RequiresApi(Build.VERSION_CODES.O)
    fun querySoundData(from: String = "", to: String = ""): MutableList<Pair<String, Double>> {
        return apiClient.querySoundData(from, to)
    }

    // Strings that can be parsed into local datetimes
    @RequiresApi(Build.VERSION_CODES.O)
    fun queryInterval(): Pair<String, String> {
        apiClient.initialize()
        return apiClient.queryInterval()
    }

    // String: strings that can be parsed into LocalDatetime
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateInterval(from: String, to: String) {
        return apiClient.updateInterval(from, to)
    }
}