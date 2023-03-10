package net.j2i.wiolocatorcollector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject


//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonNull.serializer
//import com.google.android.gms.location.*;
//import kotlinx.serialization.Serializable;
import java.io.*
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList


const val TAG = "MainActivity"
const val COURSE_LOCATION_REQUEST = 0x8001

class MainActivity : AppCompatActivity() {


    companion object {
        object ScanItemContract :BaseColumns {
            const val TABLE_NAME = "scan_item"
            const val COLUMN_NAME_BSSID = "bssid"
            const val COLUMN_NAME_SSID = "ssid";
            const val COLUMN_NAME_LEVEL = "level"
            const val COLUMN_NAME_CAPABILITIES = "capabilities"
            const val COLUMN_NAME_LATITUDE = "latitude";
            const val COLUMN_NAME_LONGITUDE = "longitude"
            const val COLUMN_NAME_ALTITUDE = "altitude"
            const val COLUMN_NAME_HORIZONTALACCURACY = "ha"
            const val COLUMN_NAME_VERTICALACCURACY = "va"
            const val COLUMN_NAME_FREQUECY = "frequency"
            const val COLUMN_NAME_DATETIME = "timestamp";
            const val COLUMN_NAME_SESSIONID = "sessionID"
            const val COLUMN_NAME_CLIENTID = "clientID"
            const val COLUMN_NAME_LOCATIONLABEL = "locationlabel"


        }
    }

    val PICKER_JSON_SAVE = 100;
    var isScanning = false
    lateinit var wifiManager:WifiManager
    val intentFilter = IntentFilter().also {
        it.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    }

    lateinit var scanItemDataHelper:ScanItemDataHelper

    private var requestingLocationUpdates = false

    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var wifiReceiver: BroadcastReceiver
    lateinit var receiver:BroadcastReceiver
    lateinit var wifiInfo:WifiInfo

    lateinit var newEntryDisplayText:TextView
    lateinit var editTextLabel:EditText

    var newEntryCount = 0;

    var currentLocation: Location? = null

    var locationLabel:String = ""
    var clientID:String = "00000000-00000000-00000000-00000000"

    class WifiReceiver:BroadcastReceiver {
        var SessionID:String = "";
        var wifiManager:WifiManager
        var mainActivity:MainActivity
        constructor(wifiManager:WifiManager, mainActivity:MainActivity) {
            this.wifiManager = wifiManager;
            this.mainActivity = mainActivity
            this.SessionID =  UUID.randomUUID().toString()
        }

        override fun onReceive(p0: Context?, intent: Intent?) {
            var action:String? = intent?.action
            if(mainActivity.currentLocation!=null) {
                val currentLocation = mainActivity.currentLocation!!;
                var result = wifiManager.scanResults
                Log.d(TAG, "results received");

                val scanResultList: List<ScanItem> = ArrayList<ScanItem>(result.size)

                for (r in result) {
                    var item = ScanItem(r.BSSID)
                    item.apply {
                        sessionID = SessionID
                        clientID = mainActivity.clientID
                        latitude = currentLocation.latitude
                        longitude = currentLocation.longitude
                        if (currentLocation.hasAccuracy()) {
                            horizontalAccuracy = currentLocation.accuracy
                        }
                        if (currentLocation.hasVerticalAccuracy()) {
                            verticalAccuracy = currentLocation.verticalAccuracyMeters
                        }
                        altitude = currentLocation.altitude.toFloat()
                        BSSID = r.BSSID
                        SSID = r.SSID
                        level = r.level
                        capabilities = r.capabilities //http://w1.fi/cgit/hostap/tree/wpa_supplicant/ctrl_iface.c?id=7b42862ac87f333b0efb0f0bae822dcdf606bc69#n2169
                        frequency = r.frequency
                        datetime = currentLocation.time
                        locationLabel = mainActivity.locationLabel
                    }
                    mainActivity.scanItemDataHelper.insert(item)
                }
                mainActivity.currentLocation = null
                mainActivity.addNewEntryCount(result.size)
                Toast.makeText(mainActivity, "scan Saved", Toast.LENGTH_SHORT).show();
            }
            wifiManager.startScan()
        }
    }


    fun resetNewEntryCount() {
        newEntryCount = 0;
        newEntryDisplayText.text = newEntryCount.toString()
    }

    fun addNewEntryCount(c:Int) {
        newEntryCount += c
        newEntryDisplayText.text = newEntryCount.toString()
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10_000
            fastestInterval = 10_000
            smallestDisplacement = 3.0f
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                locationResult ?: return
                for (location in locationResult.locations){
                    currentLocation = location
                    // Update UI with location data
                    // ...
                }
            }
        }


        locationRequest?.let {
            fusedLocationClient.requestLocationUpdates(
                it,
                locationCallback,
                Looper.getMainLooper())
        }
    }

    fun getWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(this, "version> = marshmallow", Toast.LENGTH_SHORT).show();
            if (checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "location turned off", Toast.LENGTH_SHORT).show();
                var s = arrayOf<String>(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

                this.requestPermissions(s, COURSE_LOCATION_REQUEST);
            } else {
                Toast.makeText(this, "location turned on", Toast.LENGTH_SHORT).show();
                getLocationUpdates()
                wifiManager.startScan();
            }
        } else {
            Toast.makeText(this, "scanning", Toast.LENGTH_SHORT).show();
            getLocationUpdates();
            wifiManager.startScan();
        }
    }


    fun hashString(s:String): ULong {
        val A:ULong =  54059u ;
        val B:ULong = 76963u ;
        val C:ULong = 86969u;
        val FIRSTH:ULong = 37u;
        var h = FIRSTH;
        var stringBytes = s.toByteArray()
        for ( i in 0..stringBytes.size-1) {
            var c = stringBytes[i].toULong();
            h = ((h * A) xor (((c) * B))) and 0xFFFFFFFFFFFFFFFFu;
        }
        return h;
    }

    override fun onUserLeaveHint() {
        val pictureInPictureParams = PictureInPictureParams.Builder().setAspectRatio(rational).build()
        enterPictureInPictureMode(pictureInPictureParams)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var x = hashString("joel");
        var y = x.toString(16)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pref = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE);
        this.clientID = pref.getString("ClientID", "")!!
        if(this.clientID == "") {
            val editor = pref.edit()
            this.clientID = UUID.randomUUID().toString()
            editor.putString("ClientID", this.clientID)
            editor.commit()
        }


        newEntryDisplayText = findViewById(R.id.entryCountTextDisplay)
        editTextLabel = findViewById(R.id.edtTextLabel);
        this.scanItemDataHelper = ScanItemDataHelper(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "Turning on Wifi...", Toast.LENGTH_LONG).show()
            wifiManager.isWifiEnabled = true
        }

        wifiReceiver = WifiReceiver(wifiManager, this)
        wifiInfo = wifiManager.connectionInfo
        registerReceiver(this.wifiReceiver, intentFilter)
        getWifi()
    }

    override fun onResume() {
        if(isScanning) {
            registerReceiver(this.wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            wifiManager.startScan()
        }
        getLocationUpdates()
        super.onResume()
    }

    override fun onPause() {
        //unregisterReceiver(this.wifiReceiver);
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onPause()
    }

    public fun onStartRecordingClicked(startButton: View) {
        if(!isScanning) {
            resetNewEntryCount();
            isScanning = true;
            wifiManager.startScan()
        }


    }

    fun onUpdateLabelClicked(button:View) {
        this.locationLabel = editTextLabel.text.toString()
    }

    fun onStopRecordingClicked(stopButton:View) {
        if(!isScanning) return
        unregisterReceiver(this.wifiReceiver)
        isScanning = false;
    }

    fun exportDataClicked(exportData:View) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE);
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "wifi-data.json")
        }
        this.startActivityForResult(intent, PICKER_JSON_SAVE)
    }

    fun clearDataClick(clearData:View) {
        scanItemDataHelper.deleteAll()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        when(requestCode) {
            PICKER_JSON_SAVE -> {
                if(resultCode == Activity.RESULT_OK) {

                    if(intentData?.data != null) {
                        var uri: Uri = intentData?.data as Uri;
                        if(uri!=null) {
                            println(uri.toString());
                            var itemList = scanItemDataHelper.retrieveAll().toTypedArray()
                            var jlist:ArrayList<JsonObject> = ArrayList<JsonObject>()
                            for(item in itemList) {
                                jlist.add(item.getJobject())
                            }
                            //var dataString = Json.encodeToString(itemList[0])
                            var dataString = jlist.toString()
                            val os: OutputStream? = contentResolver.openOutputStream(uri)
                            if(os != null) {
                                val pw = OutputStreamWriter(os);
                                try { pw.write(dataString);

                                }finally {
                                    pw.flush()
                                    os.close()
                                    Log.d(TAG, "File data written")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}