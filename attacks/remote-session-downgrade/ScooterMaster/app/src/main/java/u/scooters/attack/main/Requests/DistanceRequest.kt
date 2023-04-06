package u.scooters.attack.main.Requests

import u.scooters.attack.main.Statistics.distanceTravelled
import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands
import java.util.concurrent.TimeUnit

class DistanceRequest : IRequest {
    override val requestBit = "B9"
    override val requestType = RequestType.DISTANCE
    private val startTime: Long
    override val delay: Int
        get() = Companion.delay
    override val requestString: String
        get() = LegacyPacketBuilder()
            .setDirection(Commands.MASTER_TO_M365)
            .setRW(Commands.READ)
            .setPosition(0xB9)
            .setPayload(0x02)
            .build()

    override fun handleResponse(request: Array<String?>?): String? {
        val temp = request!![7] + request[6]
        val distance: Int = temp.toInt(16)
        var v = distance.toDouble()
        v = v / 100
        //Log.d("Dist","distance:"+v);
        distanceTravelled = v
        return "$v km"
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