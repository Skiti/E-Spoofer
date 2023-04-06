package u.scooters.attack.main

import android.Manifest
import android.annotation.SuppressLint
import u.scooters.attack.util.ScannedDevice
import android.widget.ArrayAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat

class DeviceAdapter(context: Context, private val mResId: Int, private val mList: MutableList<ScannedDevice>) : ArrayAdapter<ScannedDevice?>(context, mResId, mList as List<ScannedDevice?>) {
    private val mInflater: LayoutInflater
    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val item = getItem(position)
        if (convertView == null) {
            convertView = mInflater.inflate(mResId, null)
        }
        val name = convertView!!.findViewById<View>(R.id.device_name) as TextView
        name.text = item!!.displayName
        val address = convertView.findViewById<View>(R.id.device_address) as TextView
        address.text = item.device.address
        val rssi = convertView.findViewById<View>(R.id.device_rssi) as TextView
        rssi.text = PREFIX_RSSI + Integer.toString(item.rssi)
        var security = convertView.findViewById<View>(R.id.security_level) as TextView
        security.text = "Security level: " + Integer.toString(item.securityLevel)

        return convertView
    }

    /**
     * add or update BluetoothDevice
     */
    fun update(newDevice: BluetoothDevice?, rssi: Int, scanRecord: ScanRecord?) {

        if (newDevice == null || newDevice.address == null || newDevice.name == null || !newDevice.name.contains("MIScooter")){
            return
        }
        var contains = false
        for (device in mList) {
            if (newDevice.address == device.device.address) {
                contains = true
                device.rssi = rssi // update
                break
            }

        }

        if (!contains && scanRecord!!.manufacturerSpecificData.valueAt(0) != null) {
            // add new BluetoothDevice
            mList.add(ScannedDevice(newDevice, rssi, scanRecord))
        }
        notifyDataSetChanged()
    }

    companion object {
        private const val PREFIX_RSSI = "RSSI:"
    }

    init {
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}