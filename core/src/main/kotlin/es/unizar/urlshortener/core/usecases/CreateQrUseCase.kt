@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*


/**
 * Given an url returns a QR 
 *
 */
interface CreateQrUseCase {
    fun generate(url: String, id: String)
    fun get(id: String): ByteArray
}

/**
 *  Implementation of [CreateQrUseCase].
 */
class CreateQrUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val qrService: QrService,
    private val qrRepository: HashMap<String, ByteArray>,
    private val obtenerUrlInfoServer: UrlService
) : CreateQrUseCase {
    // Create a QR code from a given url
    override fun generate(url: String, id: String) {

        // SI su existe quiero que se genere el QR y se guarde en su.properties.qr

        val su = shortUrlRepository.findByKey(id)

        if( su != null ){
            println("COJO EL IDDD " + id + url)
            //properties=ShortUrlProperties(ip=127.0.0.1, sponsor=null, safe=true, owner=null, country=null, qr=null))
            //shortUrlRepository.save(it.copy(properties = it.properties.copy(qrGenerated = qrGenerated.toString())))
            val qrGenerated = qrService.generateQr(url)
            qrRepository.put(id, qrGenerated)
        } else {
            throw RedirectionNotFound(id)
        } 
    }

    // Get the QR code from a given id
    override fun get(id: String): ByteArray {
        println("COJO EL IDDD gett" + id)
        val su = shortUrlRepository.findByKey(id)
        if (su != null){
                println("QR: " + su.properties)
                if (su.properties.qr == true ) {
                    // se podria cambiar pero la app se va a lanzar en puerto 8080
                    // por ello la urlAcortada sera http://localhost:8080/id
                    val urlAcortada = "http://localhost:8080/" + id
                    val (urlOriginal, alcanzable) = obtenerUrlInfoServer.obtenerUrlInfoPorts(urlAcortada)

                    if (urlOriginal != null && alcanzable == 1) {
                        // if es alcanzable == 1 -> devuelve qr
                        return qrRepository.get(id)!!

                    } else if (urlOriginal != null && alcanzable == 2){
                        // if es alcanzable == 2 -> dev 400 (url no alcanzable)
                        throw ForbiddenRedirection(urlAcortada)

                    } else if (urlOriginal != null && alcanzable == 0){
                        // if es alcanzable == 0 -> dev 400 (retry after 60s)
                        throw PendingRedirection(retryAfterSeconds = 60)

                    } else {
                        // Lanzar una excepción en caso de no encontrar la redirección
                        throw RedirectionNotFound(urlAcortada)
                    }

                } else {
                    // Existe la url pero no tiene QR: (url invalida? error 403?)
                    throw InvalidUrlException(id)
                }
        } else{
            // NO se encontro el ID: 404 not found
            throw RedirectionNotFound(id)
        }

    }
            
    
        
}
