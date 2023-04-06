package u.scooters.attack.main.Requests.SwitchRequests.Locking

import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands
import java.util.*

class LockOn : IRequest {
    override val requestBit = "70"
    override val requestType = RequestType.NOCOUNT
    private val startTime: Long
    override val delay: Int
        get() = Companion.delay
    override val requestString: String
        get() = LegacyPacketBuilder()
            .setDirection(Commands.MASTER_TO_M365)
            .setRW(Commands.WRITE)
            .setPosition(0x70)
            .setPayload(0x0001)
            .build()

    override fun handleResponse(request: Array<String?>?): String? {
        return Arrays.toString(request)
    }

    companion object {
        private const val delay = 100
    }

    init {
        startTime = System.currentTimeMillis() + Companion.delay
    }
}