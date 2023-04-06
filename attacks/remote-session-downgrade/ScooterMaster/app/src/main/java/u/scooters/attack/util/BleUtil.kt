package u.scooters.attack.util

import android.content.pm.PackageManager
import android.bluetooth.BluetoothManager
import android.content.Context

/**
 * Util for Bluetooth Low Energy
 */
object BleUtil {
    /**
     * check if BLE Supported device
     */
    fun isBLESupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    /**
     * get BluetoothManager
     */
    fun getManager(context: Context): BluetoothManager {
        return context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
}