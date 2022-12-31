package net.j2i.wiolocatorcollector

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import kotlin.math.sign

//https://developer.android.com/training/data-storage/sqlite
const val DATABASE_NAME = "scan_item_database"
const val DATABASE_VERSION = 1

class ScanItemDataHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    object ScanItemContract : BaseColumns {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "ScanItem.db"

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
        const val COLUMN_NAME_LOCATIONLABEL = "locationlabel"
        const val COLUMN_NAME_CLIENTID = "clientID"

        const val CREATE_TABLE_QUERY = "CREATE TABLE ${MainActivity.Companion.ScanItemContract.TABLE_NAME} ("+
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_SESSIONID}  TEXT, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_CLIENTID}  TEXT, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_BSSID}  TEXT NOT NULL, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_SSID}  TEXT NOT NULL, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_LEVEL}  INT, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_CAPABILITIES}  TEXT, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_FREQUECY}  INT, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_DATETIME} INT," +
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_LATITUDE}  REAL NOT NULL, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_LONGITUDE}  REAL NOT NULL, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_ALTITUDE}  REAL, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_HORIZONTALACCURACY}  REAL, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_VERTICALACCURACY}  REAL, "+
                "${MainActivity.Companion.ScanItemContract.COLUMN_NAME_LOCATIONLABEL}  REAL "+
                ")"
        const val DELETE_TABLES_QUERY = "DROP TABLE IF EXISTS ${TABLE_NAME}";
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ScanItemContract.CREATE_TABLE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(ScanItemContract.DELETE_TABLES_QUERY)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun insert(si:ScanItem) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(ScanItemContract.COLUMN_NAME_SSID, si.SSID)
            put(ScanItemContract.COLUMN_NAME_SESSIONID, si.sessionID)
            put(ScanItemContract.COLUMN_NAME_CLIENTID, si.clientID)
            put(ScanItemContract.COLUMN_NAME_BSSID, si.BSSID)
            put(ScanItemContract.COLUMN_NAME_ALTITUDE, si.altitude)
            put(ScanItemContract.COLUMN_NAME_LONGITUDE, si.longitude)
            put(ScanItemContract.COLUMN_NAME_LATITUDE, si.latitude)
            put(ScanItemContract.COLUMN_NAME_LEVEL, si.level)
            put(ScanItemContract.COLUMN_NAME_CAPABILITIES, si.capabilities)
            put(ScanItemContract.COLUMN_NAME_HORIZONTALACCURACY, si.horizontalAccuracy)
            put(ScanItemContract.COLUMN_NAME_VERTICALACCURACY, si.verticalAccuracy)
            put(ScanItemContract.COLUMN_NAME_FREQUECY, si.frequency)
            put(ScanItemContract.COLUMN_NAME_DATETIME, si.datetime)
            put(ScanItemContract.COLUMN_NAME_LOCATIONLABEL, si.locationLabel)
        }
        val newRow = db?.insert(ScanItemContract.TABLE_NAME, null, values)
        si.ID = newRow
    }


    fun deleteAll() {
        val db = this.writableDatabase
        // Define 'where' part of query.
        val selection:String? = null;//"${BaseColumns._ID} LIKE ?"
        val selectionArgs:Array<String>? = null
        val deletedRowCount = db.delete(ScanItemContract.TABLE_NAME, selection, selectionArgs)
        Log.d(TAG, "Deleted item count "+deletedRowCount)
    }

    fun retrieveAll(): List<ScanItem>  {
        var retVal = ArrayList<ScanItem>()
        val retreivalProjection = arrayOf(
            BaseColumns._ID,
            ScanItemContract.COLUMN_NAME_BSSID,
            ScanItemContract.COLUMN_NAME_SESSIONID,
            ScanItemContract.COLUMN_NAME_CLIENTID,
            ScanItemContract.COLUMN_NAME_SSID,
            ScanItemContract.COLUMN_NAME_ALTITUDE,
            ScanItemContract.COLUMN_NAME_LONGITUDE,
            ScanItemContract.COLUMN_NAME_LATITUDE,
            ScanItemContract.COLUMN_NAME_LEVEL,
            ScanItemContract.COLUMN_NAME_CAPABILITIES,
            ScanItemContract.COLUMN_NAME_HORIZONTALACCURACY,
            ScanItemContract.COLUMN_NAME_VERTICALACCURACY,
            ScanItemContract.COLUMN_NAME_DATETIME,
            ScanItemContract.COLUMN_NAME_FREQUECY,
            ScanItemContract.COLUMN_NAME_LOCATIONLABEL
        )

        var selection:String? = null //Not filtering
        val sortOrder = "" //Not sorting
        val selectionArgs:Array<String>? = null

        val cursor = readableDatabase.query(
            ScanItemContract.TABLE_NAME,
            retreivalProjection, selection,selectionArgs,null, null, sortOrder)
        with(cursor) {
            while(moveToNext()) {
                val si = ScanItem()
                si.ID = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                si.SSID = getString(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_SSID))
                si.BSSID = getString(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_BSSID))

                si.longitude = getDouble(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_LONGITUDE))
                si.latitude = getDouble(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_LATITUDE))
                si.altitude = getDouble(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_ALTITUDE)).toFloat()
                si.horizontalAccuracy = getDouble(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_HORIZONTALACCURACY)).toFloat()
                si.verticalAccuracy = getDouble(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_VERTICALACCURACY)).toFloat()
                si.capabilities = getString(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_CAPABILITIES))
                si.level = getInt(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_LEVEL))

                si.frequency = getInt(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_FREQUECY))
                si.datetime = getLong(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_DATETIME))
                si.sessionID = getString(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_SESSIONID))
                si.clientID = getString(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_CLIENTID))

                si.locationLabel = getString(getColumnIndexOrThrow(ScanItemContract.COLUMN_NAME_LOCATIONLABEL))

                retVal.add(si);
            }
        }
        return retVal
    }

}