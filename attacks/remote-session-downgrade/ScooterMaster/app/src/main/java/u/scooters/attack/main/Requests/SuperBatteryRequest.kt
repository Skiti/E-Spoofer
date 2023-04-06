package u.scooters.attack.main.Requests

import u.scooters.attack.main.Statistics.setCurrentAmpere
import u.scooters.attack.main.Statistics.currentVoltage
import u.scooters.attack.main.Statistics.batteryTemperature
import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.main.Statistics
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands

class SuperBatteryRequest : IRequest {
    override val requestBit = "31"
    override val requestType = RequestType.SUPERBATTERY
    override val delay: Int
        get() = 0

    //55 aa 03 22 01 31 0a 9e ff
    override val requestString: String
        get() =//55 aa 03 22 01 31 0a 9e ff
            LegacyPacketBuilder()
                .setDirection(Commands.MASTER_TO_BATTERY)
                .setRW(Commands.READ)
                .setPosition(0x31)
                .setPayload(0x0a)
                .build()

    override fun handleResponse(request: Array<String?>?): String? {
        var temp: String? = request!![7] + request[6]
        val remainingCapacity: Int = temp!!.toInt(16)
        Statistics.remainingCapacity = remainingCapacity
        temp = request[9] + request[8]
        val batteryLife: Int = temp.toInt(16)
        Statistics.batteryLife = batteryLife
        temp = request[11] + request[10]
        val amps: Int = temp.toInt(16)
        var c = amps.toDouble()
        c = c / 100
        setCurrentAmpere(c)
        temp = request[13] + request[12]
        val voltage: Int = temp.toInt(16)
        var v = voltage.toDouble()
        v = v / 100
        currentVoltage = v
        temp = request[14]
        val battTemp1: Int = temp!!.toInt(16)
        temp = request[15]
        val battTemp2: Int = temp!!.toInt(16)
        val maxBattTemp = Math.max(battTemp1, battTemp2) - 20
        batteryTemperature = maxBattTemp
        return "$c A"
    }
}