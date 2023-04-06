package u.scooters.attack.main

interface IRequest {
    val delay: Int
    val requestString: String?

    //get RequestBit to identify
    val requestBit: String?

    //expected to update the textviews and the statistic class
    fun handleResponse(request: Array<String?>?): String?
    val requestType: RequestType?
}