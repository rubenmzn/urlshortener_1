package es.unizar.urlshortener.core

/**
 * [ClickRepositoryService] is the port to the repository that provides persistence to [Clicks][Click].
 */
interface ClickRepositoryService {
    fun save(cl: Click): Click
}

/**
 * [ShortUrlRepositoryService] is the port to the repository that provides management to [ShortUrl][ShortUrl].
 */
interface ShortUrlRepositoryService {
    fun findByKey(id: String): ShortUrl?
    fun save(su: ShortUrl): ShortUrl
}

/**
 * [ValidatorService] is the port to the service that validates if an url can be shortened.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core .
 */
interface ValidatorService {
    fun isValid(url: String): Boolean
}

/**
 * [HashService] is the port to the service that creates a hash from a URL.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core .
 */
interface HashService {
    fun hasUrl(url: String): String
}

/**
 * [QrService] is the port to the service that creates a QR code from a URL.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core .
 */
interface QrService {
    fun generateQr(url: String): ByteArray
}

/**
 * [UrlService] is the port to the service that manages the conexion with the database.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core .
 */
interface UrlService {
    fun obtenerUrlInfoPorts(urlAcortada: String): Pair<String?, Int?>
    fun obtenerUrlExiste(url: String): Boolean
    fun obtenerActualizarQr(url: String, qr: ByteArray, id: String)
    fun obtenerQrCOde(id: String): ByteArray?
    fun obtenerInsertar(url: String, urlAcortada: String, qr: Boolean, qrUrl: String, alcanzable: Int)
}


