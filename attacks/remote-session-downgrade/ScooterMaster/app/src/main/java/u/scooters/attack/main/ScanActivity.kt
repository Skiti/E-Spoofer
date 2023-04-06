package u.scooters.attack.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import u.scooters.attack.util.BleUtil
import u.scooters.attack.util.BleUtil.getManager


@RuntimePermissions
class ScanActivity : Activity() {
    private var mBTAdapter: BluetoothAdapter? = null
    private var mBTScanner: BluetoothLeScanner? = null
    private var mDeviceAdapter: DeviceAdapter? = null
    private var mIsScanning = false
    private var scanCallback = object:ScanCallback(){

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            var newDevice = result!!.device
            var newRssi = result!!.rssi
            var newScanRecord = result!!.scanRecord

            runOnUiThread { mDeviceAdapter!!.update(newDevice, newRssi, newScanRecord) }
            return
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScan()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (mIsScanning) {
            menu.findItem(R.id.action_scan).isVisible = false
            menu.findItem(R.id.action_stop).isVisible = true
        } else {
            menu.findItem(R.id.action_scan).isVisible = true
            menu.findItem(R.id.action_stop).isVisible = false
        }

        menu.findItem(R.id.start_service).setOnMenuItemClickListener{
            Log.d("ScanActivity","Total lock attack launched - using a service.")
            startService(Intent(applicationContext, BackgroundAttack::class.java))


            true;
        }

        return super.onPrepareOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_scan) {
            startScan()
            return true
        } else if (itemId == R.id.action_stop) {
            stopScan()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun onLeScan(newDevice: BluetoothDevice, newRssi: Int,
                          newScanRecord: ByteArray) {


    }
    private fun init() {
        // BLE check
        if (!BleUtil.isBLESupported(this)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // BT check
        val manager = getManager(this)
        if (manager != null) {
            mBTAdapter = manager.adapter
        }
        if (mBTAdapter == null) {
            Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (!mBTAdapter!!.isEnabled) {
            Toast.makeText(this, R.string.bt_disabled, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        mBTScanner = mBTAdapter!!.bluetoothLeScanner


        // init listview
        val deviceListView = findViewById<View>(R.id.list) as ListView
        mDeviceAdapter = DeviceAdapter(this, R.layout.listitem_device,
                ArrayList())
        deviceListView.adapter = mDeviceAdapter
        deviceListView.onItemClickListener = OnItemClickListener { adapterview, view, position, id ->
            val item = mDeviceAdapter!!.getItem(position)
            if (item != null) {
                val intent = Intent(view.context, DeviceActivity::class.java)
                val selectedDevice = item.device

                intent.putExtra(DeviceActivity.SECURITY_LEVEL, item.securityLevel)
                intent.putExtra(DeviceActivity.EXTRAS_DEVICE_NAME, selectedDevice.name)
                intent.putExtra(DeviceActivity.EXTRAS_DEVICE_ADDRESS, selectedDevice.address)

                startActivity(intent)

                // stop before change Activity
                stopScan()
            }
        }
        stopScan()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        if (mBTScanner != null && !mIsScanning) {
            mBTScanner!!.startScan(scanCallback)
            mIsScanning = true
            invalidateOptionsMenu()
        }
    }

    private fun stopScan() {
        if (mBTScanner != null) {
            mBTScanner!!.stopScan(scanCallback)
        }
        mIsScanning = false
        invalidateOptionsMenu()
    }
}