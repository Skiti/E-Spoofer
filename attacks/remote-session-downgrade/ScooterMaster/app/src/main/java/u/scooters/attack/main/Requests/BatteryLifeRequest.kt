package u.scooters.attack.main.Requests

import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.main.Statistics
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands

class BatteryLifeRequest : IRequest {
    override val requestBit = "32"
    override val requestType = RequestType.BATTERYLIFE
    private val startTime: Long
    override val delay: Int
        get() = Companion.delay
    override val requestString: String
        get() = LegacyPacketBuilder()
            .setDirection(Commands.MASTER_TO_BATTERY)
            .setRW(Commands.READ)
            .setPosition(0x32)
            .setPayload(0x02)
            .build()

    override fun handleResponse(request: Array<String?>?): String? {
        val temp = request!![7] + request[6]
        val batteryLife: Int = temp.toInt(16)
        Statistics.batteryLife = batteryLife
        return "$batteryLife %"
        //return textViews;
    }

    companion object {
        private const val delay = 1000
    }

    init {
        startTime = System.currentTimeMillis() + Companion.delay
    }
}