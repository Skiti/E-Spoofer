package u.scooters.attack.main.Requests

import u.scooters.attack.main.Statistics.setCurrentAmpere
import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands

class AmpereRequest : IRequest {
    override val requestBit = "33"
    override val requestType = RequestType.AMPERE
    private val startTime: Long
    override val delay: Int
        get() = Companion.delay
    override val requestString: String
        get() = LegacyPacketBuilder()
            .setDirection(Commands.MASTER_TO_BATTERY)
            .setRW(Commands.READ)
            .setPosition(0x33)
            .setPayload(0x02)
            .build()

    override fun handleResponse(request: Array<String?>?): String? {
        val temp = request!![7] + request[6]
        val amps: Int = temp.toInt(16)
        var c = amps.toDouble()
        c = c / 100
        setCurrentAmpere(c)
        return "$c A"
    }

    companion object {
        private const val delay = 100
    }

    init {
        startTime = System.currentTimeMillis() + Companion.delay
    }
}