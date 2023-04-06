package u.scooters.attack.main.Requests

import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import u.scooters.attack.main.Statistics
import u.scooters.attack.util.Commands
import u.scooters.attack.util.HexString
import u.scooters.attack.util.LegacyPacketBuilder
import java.util.*
import java.util.concurrent.TimeUnit

class ScooterNameRequest : IRequest {
    override val requestBit = "00"
    override val requestType = RequestType.SCOOTERNAME

    override val delay: Int
        get() = ScooterNameRequest.delay
    override val requestString: String
        get() = HexString.bytesToHex(LegacyPacketBuilder.encryptionNinebot.encrypt(
            HexString.hexToBytes("5AA5003E210100")))

    override fun handleResponse(request: Array<String?>?): String? {
        return Arrays.toString(request)
    }


    companion object {
        private const val delay = 100
    }

}