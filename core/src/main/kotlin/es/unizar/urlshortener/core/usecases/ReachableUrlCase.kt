@file:Suppress("WildcardImport")
package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import java.net.HttpURLConnection
import java.net.URL
import java.io.IOException

interface ReachableUrlCase {
    fun checkReachable(url: String): Boolean
}

class ReachableUrlCaseImpl(
    private val validatorService: ValidatorService,
) : ReachableUrlCase {
    override fun checkReachable(url: String): Boolean {
        if(validatorService.isValid(url)){
            return try{
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                val responseCode = connection.responseCode
                responseCode in 200..299
            } catch (e: IOException){
                false
            }
        } else {
            return false
        }
    }
}
