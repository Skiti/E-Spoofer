package u.scooters.attack.main.Requests.SwitchRequests.Locking

import u.scooters.attack.main.Statistics.isScooterLocked
import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands

class CheckLock : IRequest {
    override val requestBit = "B2"
    override val requestType = RequestType.LOCK
    private val startTime: Long
    override val delay: Int
        get() = Companion.delay
    override val requestString: String
        get() = LegacyPacketBuilder()
            .setDirection(Commands.MASTER_TO_M365)
            .setRW(Commands.READ)
            .setPosition(0xB2)
            .setPayload(0x02)
            .build()

    override fun handleResponse(request: Array<String?>?): String? {
        if (request!![6] == "02") {
            isScooterLocked = true
        } else {
            isScooterLocked = false
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