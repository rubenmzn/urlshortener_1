package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.UrlService
import es.unizar.urlshortener.core.ForbiddenRedirection
import es.unizar.urlshortener.core.PendingRedirection

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
    private val obtenerUrlInfoServer: UrlService
) : RedirectUseCase {

    /**
     * Given a key returns a [Redirection] that contains a [URI target][Redirection.target]
     * and an [HTTP redirection mode][Redirection.mode].
     *
     * @param key Key of the redirection.
     * @return [Redirection] that contains a [URI target][Redirection.target]
     * and an [HTTP redirection mode][Redirection.mode].
     * @throws RedirectionNotFound if the redirection is not found.
     */

    override fun redirectTo(key: String): Redirection {


        val urlAcortada = "http://localhost:8080/" + key
        val (urlOriginal, alcanzable) = obtenerUrlInfoServer.obtenerUrlInfoPorts(urlAcortada)

        if (urlOriginal != null && alcanzable == 1) {
            // Construir y devolver el objeto Redirection
            return Redirection(target = urlOriginal.toString(), mode = 307)
        } else if (urlOriginal != null && alcanzable == 2){
            // URL original encontrada, pero no es alcanzable
            throw ForbiddenRedirection(key)

        } else if (urlOriginal != null && alcanzable == 0){
            // URL original encontrada, pero no se ha confirmado la alcanzabilidad
            throw PendingRedirection(retryAfterSeconds = 60)

        } else {
            // Lanzar una excepción en caso de no encontrar la redirección
            throw RedirectionNotFound(key)
        }
    }
}



