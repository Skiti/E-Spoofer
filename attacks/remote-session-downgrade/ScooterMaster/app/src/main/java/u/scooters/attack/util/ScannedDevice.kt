package u.scooters.attack.util

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.util.Log
import android.util.SparseArray


/**
 * LeScanned Bluetooth Device
 */
class ScannedDevice(device: BluetoothDevice?, rssi: Int, scanRecord: ScanRecord?) {

    val device: BluetoothDevice
    var rssi: Int
    var displayName: String
    var securityLevel: Int

    companion object {
        private const val UNKNOWN = "Unknown"
    }

    init {
        requireNotNull(device) { "BluetoothDevice is null" }
        this.device = device
        displayName = device.name
        if (displayName.length == 0) {
            displayName = UNKNOWN
        }
        this.rssi = rssi
        var   manufacturerData = scanRecord!!.manufacturerSpecificData
        this.securityLevel = manufacturerData.valueAt(0)[1].toInt()
    }
}