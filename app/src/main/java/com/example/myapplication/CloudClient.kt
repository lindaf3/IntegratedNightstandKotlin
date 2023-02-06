import android.os.Build
import androidx.annotation.RequiresApi

class CloudClient(key: String, token: String) {
    val iotClient = IoTClient(key);
    val apiClient = APIClient(token);

    fun uploadSoundData(data: Array<Pair<String, Double>> ): Boolean {
        return iotClient.uploadSoundData(data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun querySoundData(sessionDate: String, from: String = "", to: String = ""): MutableList<Pair<String, Double>> {
        return apiClient.querySoundData(sessionDate, from, to)
    }
}