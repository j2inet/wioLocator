package net.j2i.wiolocatorcollector

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Instrumentation.ActivityResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import java.io.*

import java.util.logging.Logger
import kotlin.math.log


const val TAG = "MainActivity"
const val COURSE_LOCATION_REQUEST = 0x8001

class MainActivity : AppCompatActivity() {

    val PICKER_JSON_SAVE = 100;
    var isScanning = false
    lateinit var wifiManager:WifiManager
    val intentFilter = IntentFilter().also {
        it.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    }

    lateinit var wifiReceiver: BroadcastReceiver
    lateinit var receiver:BroadcastReceiver
    lateinit var wifiInfo:WifiInfo

    class WifiReceiver:BroadcastReceiver {
        var wifiManager:WifiManager
        constructor(wifiManager:WifiManager) {
            this.wifiManager = wifiManager;
        }

        override fun onReceive(p0: Context?, intent: Intent?) {
            var action:String? = intent?.action
            var result = wifiManager.scanResults
            Log.d(TAG, "results received");
            wifiManager.startScan()
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
                wifiManager.startScan();
            }
        } else {
            Toast.makeText(this, "scanning", Toast.LENGTH_SHORT).show();
            wifiManager.startScan();
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "Turning on Wifi...", Toast.LENGTH_LONG).show()
            wifiManager.isWifiEnabled = true
        }

        wifiReceiver = WifiReceiver(wifiManager)
        wifiInfo = wifiManager.connectionInfo
        registerReceiver(this.wifiReceiver, intentFilter)
        getWifi()
    }

    override fun onResume() {
        registerReceiver(this.wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        if(isScanning) {
            //wifiManager.startScan()
        }
        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(this.wifiReceiver);
        super.onPause()
    }

    public fun onStartRecordingClicked(startButton: View) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE);
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "wifi-data.json")
        }
        this.startActivityForResult(intent, PICKER_JSON_SAVE)
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
                            val os: OutputStream? = contentResolver.openOutputStream(uri)
                            if(os != null) {
                                val pw = OutputStreamWriter(os);
                                try {
                                    pw.write("Success\r\n");
                                    isScanning = true;
                                    wifiManager.startScan()
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