package net.j2i.wiolocatorcollector
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


@Serializable
 public data class ScanItem(var BSSID:String)  {
    constructor():this(BSSID = "00:00:00:00:00:00") {
    }

    fun getJobject()= JsonObject (
        mapOf(
            "BSSID" to JsonPrimitive(BSSID),
            "SSID" to JsonPrimitive(SSID),
            "level" to JsonPrimitive(level),
            "capabilities" to JsonPrimitive(capabilities),
            "latitude" to JsonPrimitive(latitude),
            "longitude" to JsonPrimitive(longitude),
            "altitude" to JsonPrimitive(altitude),
            "horizontalAccuracy" to JsonPrimitive(horizontalAccuracy),
            "verticalAccuracy" to JsonPrimitive(verticalAccuracy),
            "datetime" to JsonPrimitive(datetime),
            "frequency" to JsonPrimitive(frequency)
            )
    )


    var ID: Long? = null
    var SSID: String = ""
    var level: Int = 0
    var capabilities: String = ""
    var frequency: Int = 0
    var latitude: Double = 720.0
    var longitude: Double = 720.0
    var horizontalAccuracy: Float = -1.0f
    var altitude: Float? = -1.0f
    var verticalAccuracy: Float? = -1.0f
    var datetime:Long = 0
}
