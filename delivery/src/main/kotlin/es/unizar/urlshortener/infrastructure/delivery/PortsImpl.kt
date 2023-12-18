package es.unizar.urlshortener.infrastructure.delivery

import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.QrService
import es.unizar.urlshortener.core.ValidatorService
import es.unizar.urlshortener.core.UrlService
//import es.unizar.urlshortener.core.RabbitMQService
import es.unizar.urlshortener.core.usecases.CreateQrUseCase
import org.apache.commons.validator.routines.UrlValidator
import java.nio.charset.StandardCharsets
import io.github.g0dkar.qrcode.QRCode
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired

import java.io.ByteArrayOutputStream


/**
 * Implementation of the port [ValidatorService].
 */
class ValidatorServiceImpl : ValidatorService {
    override fun isValid(url: String) = urlValidator.isValid(url)

    companion object {
        val urlValidator = UrlValidator(arrayOf("http", "https"))
    }
}

/**
 * Implementation of the port [HashService].
 */
class HashServiceImpl : HashService {
    override fun hasUrl(url: String) = Hashing.murmur3_32_fixed().hashString(url, StandardCharsets.UTF_8).toString()
}

/**
 * Implementation of the port [QrService].
 */
class QrServiceImpl : QrService {
    override fun generateQr(url: String): ByteArray {
        val imageOut = ByteArrayOutputStream()
        QRCode(url).render().writeImage(imageOut)
        return imageOut.toByteArray()
    }     
}

class UrlServiceImpl : UrlService {
    override fun obtenerUrlInfoPorts(urlAcortada: String): Pair<String?, Int?> {
        return obtenerUrlInfo(urlAcortada)
    }
    
}





