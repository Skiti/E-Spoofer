package u.scooters.attack.main.Requests

import u.scooters.attack.main.IRequest
import u.scooters.attack.main.RequestType
import java.util.*

class downgradeTo5AA5Request : IRequest {
    override val requestBit = "00"
    override val requestType = RequestType.DOWNGRADETO5AA5

    override val delay: Int
        get() = Companion.delay

    override val requestString: String
        get() = "55AB000000000000000000000000FFFF"

    override fun handleResponse(request: Array<String?>?): String? {
        return Arrays.toString(request)
    }

    companion object {
        private const val delay = 1000
    }

}