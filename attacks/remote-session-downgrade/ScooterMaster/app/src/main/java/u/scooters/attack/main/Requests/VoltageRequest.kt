package u.scooters.attack.main.Requests

import u.scooters.attack.main.Statistics.currentVoltage
import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands

class VoltageRequest : IRequest {
    override val requestBit = "34"
    override val requestType = RequestType.VOLTAGE
    private val startTime: Long
    override val delay: Int
        get() = Companion.delay
    override val requestString: String
        get() = LegacyPacketBuilder()
            .setDirection(Commands.MASTER_TO_BATTERY)
            .setRW(Commands.READ)
            .setPosition(0x34)
            .setPayload(0x02)
            .build()

    override fun handleResponse(request: Array<String?>?): String? {
        val temp = request!![7] + request[6]
        val voltage: Int = temp.toInt(16)
        var v = voltage.toDouble()
        v = v / 100
        currentVoltage = v
        return "$v V"
        //return textViews;
    }

    companion object {
        private const val delay = 500
    }

    init {
        startTime = System.currentTimeMillis() + Companion.delay
    }
}