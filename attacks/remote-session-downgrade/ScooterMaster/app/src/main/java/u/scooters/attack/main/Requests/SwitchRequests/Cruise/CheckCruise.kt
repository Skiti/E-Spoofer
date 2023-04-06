package u.scooters.attack.main.Requests.SwitchRequests.Cruise

import u.scooters.attack.main.Statistics.isCruiseActive
import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands

class CheckCruise : IRequest {
    override val requestBit = "7C"
    override val requestType = RequestType.CRUISE
    private val startTime: Long
    override val delay: Int
        get() = Companion.delay
    override val requestString: String
        get() = LegacyPacketBuilder()
            .setDirection(Commands.MASTER_TO_M365)
            .setRW(Commands.READ)
            .setPosition(0x7C)
            .setPayload(0x02)
            .build()

    override fun handleResponse(request: Array<String?>?): String? {
        if (request!![6] == "01") {
            isCruiseActive = true
        } else {
            isCruiseActive = false
        }
        return ""
    }

    companion object {
        private const val delay = 100
    }

    init {
        startTime = System.currentTimeMillis() + Companion.delay
    }
}