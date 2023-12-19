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
    private val qrService: QrService,
    //private val qrRepository: HashMap<String, ByteArray>,
    private val urlService: UrlService
) : CreateQrUseCase {
    // Create a QR code from a given url
    override fun generate(url: String, id: String) {

        println("ESTOY EN GENERATE" + id)
        // Ver si existe la url en la bbdd mysql
        if (urlService.obtenerUrlExiste(url)){
            println("ESTOY EN obtenerurlsexiste" + id)
            val qrGenerated = qrService.generateQr(url)
            println("qrgenerated" + id)
            // qrRepository.put(id, qrGenerated)
            // Insert a la bbdd del qrGenerated que es el bytearray
            urlService.obtenerActualizarQr(url, qrGenerated, id)
            println("actualizadooo" + id)

        } else {
            throw RedirectionNotFound(id)
        }

    }

    // Get the QR code from a given id
    override fun get(id: String): ByteArray {
        println("COJO EL IDDD gett" + id)

        // if el qrCOde se ha generado -> devuelve qr

        // else error 400 retry after 60s

        // se podria cambiar pero la app se va a lanzar en puerto 8080
        // por ello la urlAcortada sera http://localhost:8080/id
        val urlAcortada = "http://localhost:8080/" + id
        val (urlOriginal, alcanzable) = urlService.obtenerUrlInfoPorts(urlAcortada)

        println("urlOriginal: " + urlOriginal)
        println("alcanzable: " + alcanzable)

        if (urlOriginal != null && alcanzable == 1) {
            // if es alcanzable == 1 -> devuelve qr
            // CAMBIAR ESTOOOOO A UNA BBDDD MYSQL
            if ( urlService.obtenerQrCOde(id) == null){
                throw PendingRedirection(retryAfterSeconds = 60)
            } else {
                return urlService.obtenerQrCOde(id)!!
            }
            
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


    }
            
    
        
}
