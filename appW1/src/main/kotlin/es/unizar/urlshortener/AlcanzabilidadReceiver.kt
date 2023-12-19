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
import es.unizar.urlshortener.infrastructure.delivery.ActualizarUrlAcortada
import es.unizar.urlshortener.infrastructure.delivery.urlExiste
import es.unizar.urlshortener.infrastructure.delivery.obtenerValorAlcanzable
import es.unizar.urlshortener.core.usecases.ReachableUrlCase
import es.unizar.urlshortener.core.usecases.ReachableUrlCaseImpl
import es.unizar.urlshortener.infrastructure.delivery.ValidatorServiceImpl


// En primer lugar se realizao un receiver que se encarga de recibir los mensajes de las colas
// pero se queria intertar hacer que cada cola fuera consumida por un worker diferente, por lo que
// se creo un receiver para cada cola


@Suppress("MagicNumber")
@Component
class AlcanzabilidadReceiver(
    val reachableUrlCase: ReachableUrlCaseImpl,
){
    @RabbitListener(queues = ["#{autoDeleteQueue1.name}"])
    fun receiveReachabilityMessage(message: String) {
        // Extraer url y el hash de este mensaje "CHECK_REACHABILITY:${it.hash}:${data.url}:${data.qr}:${url}" 
        val pattern = Pattern.compile("CHECK_REACHABILITY;(\\w+);(.+);(.+);(.+)")
        val matcher = pattern.matcher(message)

        if (matcher.matches()) {
            val hash = matcher.group(1)
            val url = matcher.group(2)
            val qrValue = matcher.group(3)
            val urlAcortada = matcher.group(4)

            // Convertir qrValue a boolean (true o false)
            val qr = qrValue.toBoolean()

            println(" ALCANZABILIDAD --> Hash: $hash")
            println(" ALCANZABILIDAD --> URL: $url") 
            println(" ALCANZABILIDAD --> QR: $qr")
            println(" ALCANZABILIDAD --> URL ACORTADA: $urlAcortada")

            // mirar si la url ya esta en la base de datos
            if (!urlExiste(url)) {
                InsertarUrlAcortada(url, urlAcortada, qr, "", 0)
            }
            var isReachable = reachableUrlCase.checkReachable(url)
            if (isReachable){
                ActualizarUrlAcortada(url, urlAcortada, qr, "", 1)
            } else{
                ActualizarUrlAcortada(url, urlAcortada, qr, "", 2)
            }


        } else {
            println("Received unrecognized message: $message")
        }
        
    }
}






