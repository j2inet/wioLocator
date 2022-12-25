package net.j2i.wiolocatorcollector
//import kotlinx.serialization.Serializable

//@Serializable
public class ScanItem {

    constructor() {
        BSSID = ""
        SSID = ""
        level = 0;
        capabilities = ""

        latitude = 720.0
        longitude = 720.0
        horizontalAccuracy = 0.0f
        altitude = null
        verticalAccuracy = null
    }
    var ID:Long? = null

    var BSSID:String
    var SSID:String
    var level:Int
    var capabilities:String
    var frequency  =0

    public var latitude:Double
    public var longitude:Double
    public var horizontalAccuracy:Float?
    public var altitude:Float?
    public var verticalAccuracy:Float?

}