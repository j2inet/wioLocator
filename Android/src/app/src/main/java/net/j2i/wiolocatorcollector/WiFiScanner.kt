package net.j2i.wiolocatorcollector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log


class WiFiScanner : BroadcastReceiver{
    val TAG = "WiFiScanner"
    val context:Context
    var isScanning = false
    var wifiManager:WifiManager




    constructor(context:Context) {
        this.context = context;
        wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun start() {
        if(isScanning) {
            return;
        }
        isScanning = true;
        wifiManager.startScan()
    }

    public fun pause() {
        context.unregisterReceiver(this);

    }

    public fun resume() {
        context.registerReceiver(this, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        var wifiScanList:List<ScanResult> = this.wifiManager.getScanResults()
         Log.d(TAG, "Scan Count: "+wifiScanList.size.toString())
    }
}