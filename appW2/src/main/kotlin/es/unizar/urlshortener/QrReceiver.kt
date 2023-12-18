package es.unizar.urlshortener

import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.util.StopWatch
import org.springframework.context.annotation.Profile
import java.util.regex.Pattern
import es.unizar.urlshortener.infrastructure.delivery.InsertarUrlAcortada
import es.unizar.urlshortener.infrastructure.delivery.urlExiste
import es.unizar.urlshortener.infrastructure.delivery.obtenerValorAlcanzable
import java.util.concurrent.TimeUnit
import es.unizar.urlshortener.core.usecases.CreateQrUseCaseImpl
import es.unizar.urlshortener.infrastructure.delivery.ValidatorServiceImpl
import es.unizar.urlshortener.infrastructure.delivery.QrServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlEntityRepository







@Suppress("MagicNumber")
@Component
class QrReceiver (
    val qrCase: CreateQrUseCaseImpl,
){ 

    @RabbitListener(queues = ["#{autoDeleteQueue2.name}"])
    fun receiveQrGenerationMessage(message: String) {
        val pattern = Pattern.compile("GENERATE_QR:(\\w+):(.+)")
        val matcher = pattern.matcher(message)

        if (matcher.matches()) {
            val hash = matcher.group(1)
            val url = matcher.group(2) 

            println(" GENERA QR --> Hash: $hash")
            println(" GENERA QR --> URL: $url") 
            qrCase.generate(url, hash)     
        } else {
            println("Received unrecognized message: $message")
        }

    }
}


