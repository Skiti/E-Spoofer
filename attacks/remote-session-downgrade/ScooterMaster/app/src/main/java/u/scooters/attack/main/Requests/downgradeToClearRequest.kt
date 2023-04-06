package u.scooters.attack.main.Requests

import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import java.util.*

class downgradeToClearRequest : IRequest {
    override val requestBit = "00"
    override val requestType = RequestType.DOWNGRADETOCLEAR

    override val delay: Int
        get() = Companion.delay

    override val requestString: String
        get() = "55AA0621F00000000000E8FE"

    override fun handleResponse(request: Array<String?>?): String? {
        return Arrays.toString(request)
    }

    companion object {
        private const val delay = 1000
    }

}