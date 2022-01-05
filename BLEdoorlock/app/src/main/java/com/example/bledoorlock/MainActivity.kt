package com.example.bledoorlock

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.bluetooth.BluetoothGattService
import java.lang.String


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

// provides the initialization of needed ble objects
fun startScan() {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    val scanner = adapter.bluetoothLeScanner

    // TODO: make sure adapter is enabled in havePerms()

    // creates the filter for the carlock uuid service and the name of the device
    var CARLOCK_SERVICE_UUID = ParcelUuid(UUID.fromString("C6E07731-CAA0-45D2-BA20-9AEC4D403D73"))
    val uuidFilter = ScanFilter.Builder().setServiceUuid(CARLOCK_SERVICE_UUID).build()
    // TODO: not using name filter now
    //val nameFilter = ScanFilter.Builder().setDeviceName("MyCar").build()
    val filters = listOf( uuidFilter )

    // creates the settings profile for the scan
    val scanSettings = ScanSettings
        .Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    // runs the scan if possible
    if (scanner != null) {
        scanner.startScan(filters, scanSettings, scanCallback)
        Log.d("BLEdoorlock.startScan", "scan started")
    } else {
        Log.e("BLEdoorlock.startScan", "could not get scanner object")
    }
}

// TODO: only check perms when app is opened again instead of every button press
fun havePerms() {
    var perms = true

    // check android version
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val wrongVersionToast = Toast.makeText(MyApplication.appContext, "wrong android version", Toast.LENGTH_SHORT)
        wrongVersionToast.show()
        perms = false
    }

    // TODO: check location permission
    if (false) {
        val noLocationToast = Toast.makeText(MyApplication.appContext, "need location permissions", Toast.LENGTH_SHORT)
        noLocationToast.show()
        perms = false
    }

    // TODO: check if bluetooth is enabled
    if (false) {
        val noBluetoothToast = Toast.makeText(MyApplication.appContext, "please enable bluetooth", Toast.LENGTH_SHORT)
        noBluetoothToast.show()
        perms = false
    }
}

// callback functions when the scan is complete
private val scanCallback: ScanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
        val device: BluetoothDevice = result.getDevice()
        device.connectGatt(MyApplication.appContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    override fun onBatchScanResults(results: List<ScanResult?>?) {
        // TODO: deal with multiple conflicting devices
    }

    override fun onScanFailed(errorCode: Int) {
        // TODO: throw error or something
    }
}

// gatt callback methods
private val gattCallback = object : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        //var GATT_ERROR = 133
        when (status) {

            BluetoothGatt.GATT_SUCCESS -> {
                when (newState) {

                    BluetoothGatt.STATE_CONNECTED -> {
                        // TODO: doesnt handle bonding state
                        // need delay for android 7 and below
                        gatt?.discoverServices()
                    }

                    BluetoothGatt.STATE_DISCONNECTED -> gatt?.close()

                    // else do nothing
                }
            }

            else -> {
                val gattErrorToast = Toast.makeText(MyApplication.appContext, "gatt error", Toast.LENGTH_SHORT)
                gattErrorToast.show()
                gatt?.close()
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        // Check if the service discovery succeeded. If not disconnect
        var GATT_INTERNAL_ERROR = 129
        if (status == GATT_INTERNAL_ERROR) {
            Log.e("BLEdoorlock.gattCallback.onServicesDiscovered", "Service discovery failed");
            gatt?.disconnect();
            return;
        }

        val services = gatt?.services
        Log.i("BLEdoorlock.gattCallback.onServicesDiscovered", String.format(Locale.ENGLISH, "discovered %d services for the device", services.size) )
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        // TODO: stuff
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        // TODO: stuff
    }

}

// application class for accessing context in static methods
// if it doesnt work may have to make class for all the methods
// or maybe pass context between all function calls
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        private var context: Context? = null
        val appContext: Context?
            get() = context
    }
}