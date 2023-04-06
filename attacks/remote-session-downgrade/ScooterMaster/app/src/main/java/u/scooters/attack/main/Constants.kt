package u.scooters.attack.main

object Constants {


    const val TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e" //write
    const val RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e" //read

    const val UART = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"

    const val AUTH = "0000fe95-0000-1000-8000-00805f9b34fb"
    const val UPNP = "00000010-0000-1000-8000-00805f9b34fb"
    const val AVDTP = "00000019-0000-1000-8000-00805f9b34fb"

    const val NO_SECURITY = 0  //for clear protocols (1)
    const val XOR_SECURITY = 1  //for xiaomi xored 55AB protocol (3)
    const val ENCRYPT_SECURITY = 2 //for ninebot 5AA5 (4) and xiaomi 55AB (5) encrypted protocols


    @JvmField
    var BASE_DELAY = 300
    @JvmField
    var QUEUE_DELAY = 400
    private const val VOLTAGE_DELAY = 400
    private const val AMPERE_DELAY = 100
    private const val BATTERYLIFE_DELAY = 2000
    private const val SPEED_DELAY = 100
    private const val DISTANCE_DELAY = 400
    val voltageDelay: Int
        get() = VOLTAGE_DELAY + BASE_DELAY
    @JvmStatic
    val ampereDelay: Int
        get() = AMPERE_DELAY + BASE_DELAY
    val batterylifeDelay: Int
        get() = BATTERYLIFE_DELAY + BASE_DELAY
    @JvmStatic
    val speedDelay: Int
        get() = SPEED_DELAY + BASE_DELAY
    val distanceDelay: Int
        get() = DISTANCE_DELAY + BASE_DELAY
    @JvmStatic
    val logDelay: Int
        get() = 500
}