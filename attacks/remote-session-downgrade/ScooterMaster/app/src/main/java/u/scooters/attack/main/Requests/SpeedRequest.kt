package u.scooters.attack.main.Requests

import u.scooters.attack.main.Statistics.setSpeed
import u.scooters.attack.main.Statistics.round
import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands
import java.util.concurrent.TimeUnit

class SpeedRequest : IRequest {
    override val requestBit = "B5"
    override val requestType = RequestType.SPEED
    private val startTime: Long
    override val delay: Int
        get() = Companion.delay
    override val requestString: String
        get() = LegacyPacketBuilder()
            .setDirection(Commands.MASTER_TO_M365)
            .setRW(Commands.READ)
            .setPosition(0xB5)
            .setPayload(0x02)
            .build()

    override fun handleResponse(request: Array<String?>?): String? {
        val temp = request!![7] + request[6]
        val speed: Int = temp.toInt(16)
        var v = speed.toDouble()
        v = v / 1000
        //Log.d("Speed","speed:"+v);
        setSpeed(v)
        v = round(v, 1)
        return v.toString() + ""
        //return textViews;
    }

    fun getDelay(timeUnit: TimeUnit): Long {
        val diff = startTime - System.currentTimeMillis()
        return timeUnit.convert(diff, TimeUnit.MILLISECONDS)
    }

    companion object {
        private const val delay = 500
    }

    init {
        startTime = System.currentTimeMillis() + Companion.delay
    }
}