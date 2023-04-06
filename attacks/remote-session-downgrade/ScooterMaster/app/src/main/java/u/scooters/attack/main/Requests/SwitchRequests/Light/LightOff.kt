package u.scooters.attack.main.Requests.SwitchRequests.Light

import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands
import java.util.*

class LightOff : IRequest {
    override val requestBit = "7D"
    override val requestType = RequestType.NOCOUNT
    private val startTime: Long
    override val delay: Int
        get() = Companion.delay
    override val requestString: String
        get() = LegacyPacketBuilder()
            .setDirection(Commands.MASTER_TO_M365)
            .setRW(Commands.WRITE)
            .setPosition(0x7D)
            .setPayload(0x0000)
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