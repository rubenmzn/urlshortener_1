package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.UrlService

/**
 * Given a key returns a [Redirection] that contains a [URI target][Redirection.target]
 * and an [HTTP redirection mode][Redirection.mode].
 *
 * **Note**: This is an example of functionality.
 */
interface RedirectUseCase {
    fun redirectTo(key: String): Redirection
}

/**
 * Implementation of [RedirectUseCase].
 */
class RedirectUseCaseImpl(
    // private val shortUrlRepository: ShortUrlRepositoryService,
    private val obtenerUrlInfoServer: UrlService
) : RedirectUseCase {
    override fun redirectTo(key: String): Redirection {
        val urlAcortada = key
        val (urlOriginal, alcanzable) = obtenerUrlInfoServer.obtenerUrlInfoPorts(urlAcortada)

        if (urlOriginal != null && alcanzable != null) {
            println("La URL original correspondiente a $urlAcortada es: $urlOriginal")
            println("El valor de 'alcanzable' es: $alcanzable")

            // Construir y devolver el objeto Redirection
            return Redirection(target = urlOriginal.toString(), mode = 307)
        } else {
            // Lanzar una excepción en caso de no encontrar la redirección
            throw RedirectionNotFound(key)
        }
    }
}


