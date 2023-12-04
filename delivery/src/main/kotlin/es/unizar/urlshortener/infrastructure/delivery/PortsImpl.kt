package es.unizar.urlshortener.infrastructure.delivery

import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.QrService
import es.unizar.urlshortener.core.ValidatorService
import es.unizar.urlshortener.core.RabbitMQService
import es.unizar.urlshortener.core.usecases.CreateQrUseCase
import org.apache.commons.validator.routines.UrlValidator
import java.nio.charset.StandardCharsets
import io.github.g0dkar.qrcode.QRCode
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener


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

/**
 * Implementation of the port [RabbitMQService].
 */
class RabbitMQServiceImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val createQrUseCase: CreateQrUseCase
) : RabbitMQService {
    override fun sendMessage(queueName: String, message: String) {
        rabbitTemplate.convertAndSend("", queueName, message)
    }

    @RabbitListener(queues = ["myQueue"])
    override fun receiveFromRabbitmq(message: String) {
        println("Received message: $message")
        val parts = message.split(":")
        if (parts.size == 2) {
            val action = parts[0]
            val id = parts[1]

            when (action) {
                "GENERATE_QR" -> {
                    // Realizar la tarea de generación de QR
                    createQrUseCase.generate("url_placeholder", id)
                }
                else -> {
                    println("Acción no reconocida: $action")
                }
            }
        }
    }
}


