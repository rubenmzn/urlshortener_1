@file:Suppress("WildcardImport")
package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import java.net.HttpURLConnection
import java.net.URL
import java.io.IOException

interface ReachableUrlCase {
    fun checkReachable(url: String): Boolean
}

private const val SUCCESSFUL_RESPONSE_CODE_MIN = 200
private const val SUCCESSFUL_RESPONSE_CODE_MAX = 299
/**
 * Implementation of [ReachableUrlCase]
 */
class ReachableUrlCaseImpl(
    private val validatorService: ValidatorService,
) : ReachableUrlCase {
    override fun checkReachable(url: String): Boolean {
        if(validatorService.isValid(url)){
            return try{
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                val responseCode = connection.responseCode
                responseCode in SUCCESSFUL_RESPONSE_CODE_MIN..SUCCESSFUL_RESPONSE_CODE_MAX
            } catch (e: IOException){
                // false
                // antes ponia false pero me daba error el detekt, de esta forma 
                // no da error pero se puede poner otra cosa
                throw UrlReachabilityException("Error while checking URL reachability", e)
            }
        } else {
            return false
        }
    }
}
