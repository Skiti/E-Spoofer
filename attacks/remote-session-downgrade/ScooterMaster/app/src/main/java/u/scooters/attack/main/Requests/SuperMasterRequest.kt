package u.scooters.attack.main.Requests

import u.scooters.attack.main.Statistics.setSpeed
import u.scooters.attack.main.Statistics.round
import u.scooters.attack.main.Statistics.distanceTravelled
import u.scooters.attack.main.Statistics.motorTemperature
import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.util.LegacyPacketBuilder
import u.scooters.attack.util.Commands

class SuperMasterRequest : IRequest {
    override val requestBit = "B0"
    override val requestType = RequestType.SUPERMASTER
    override val delay: Int
        get() = 0
    override val requestString: String
        get() = LegacyPacketBuilder()
            .setDirection(Commands.MASTER_TO_M365)
            .setRW(Commands.READ)
            .setPosition(0xb0)
            .setPayload(0x20)
            .build()

    override fun handleResponse(request: Array<String?>?): String? {
        var temp = request!![17] + request[16]
        val speed: Int = temp.toInt(16)
        var v = speed.toDouble()
        v = v / 1000
        //Log.d("Speed","speed:"+v);
        setSpeed(v)
        v = round(v, 1)
        temp = request[25] + request[24]
        val distance: Int = temp.toInt(16)
        var dist = distance.toDouble()
        dist = dist / 100
        distanceTravelled = dist
        temp = request[29] + request[28]
        val temperature: Int = temp.toInt(16)
        var temperature1 = temperature.toDouble()
        temperature1 = temperature1 / 10
        motorTemperature = temperature1
        //Log.d("SuperDistance","Distance:"+dist);
        return v.toString() + ""
    }
}