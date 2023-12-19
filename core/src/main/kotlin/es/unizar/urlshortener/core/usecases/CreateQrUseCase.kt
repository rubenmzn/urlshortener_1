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
    
    /**
     * Given an url and id generate a QR code and save it in the database
     *
     * @param url Url of the QR code
     * @param id Id of the QR code
     * @return ByteArray of the QR code
     * @throws RedirectionNotFound if the Url is not found
     */

    override fun generate(url: String, id: String) {

        // Ver si existe la url en la bbdd mysql
        if (urlService.obtenerUrlExiste(url)){
            val qrGenerated = qrService.generateQr(url)
            // Insert a la bbdd del qrGenerated que es el bytearray
            urlService.obtenerActualizarQr(url, qrGenerated, id)

        } else {
            throw RedirectionNotFound(id)
        }

    }

    /**
     * Given an id returns a QR code
     *
     * @param id Id of the QR code
     * @return ByteArray of the QR code
     * @throws RedirectionNotFound if the QR code is not found
     * @throws PendingRedirection if the QR code is not generate or is not reachable
     * @throws ForbiddenRedirection if the QR code is not found
     */
     
    override fun get(id: String): ByteArray {

        val urlAcortada = "http://localhost:8080/" + id
        val (urlOriginal, alcanzable) = urlService.obtenerUrlInfoPorts(urlAcortada)

        if (urlOriginal != null && alcanzable == 1) {
            // if es alcanzable == 1 -> devuelve qr
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
