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




@Suppress("MagicNumber")
@Component
class AlcanzabilidadReceiver(
    val reachableUrlCase: ReachableUrlCaseImpl,
){
    /**
     * Método que se ejecuta cuando se recibe un mensaje de alcanzabilidad.
     * Analiza el mensaje, extrae la información necesaria y realiza las operaciones correspondientes.
     * Este es el método que ejecua el worker de alcanzabilidad.
     *
     * @param message Mensaje recibido de RabbitMQ.
     */

    @RabbitListener(queues = ["#{autoDeleteQueue1.name}"])
    fun receiveReachabilityMessage(message: String) {
        // Extraer url y el hash de este mensaje "CHECK_REACHABILITY:${it.hash}:${data.url}:${data.qr}:${url}" 
        val pattern = Pattern.compile("CHECK_REACHABILITY;(\\w+);(.+);(.+);(.+)")
        val matcher = pattern.matcher(message)

        if (matcher.matches()) {
            val url = matcher.group(2)
            val qrValue = matcher.group(3)
            val urlAcortada = matcher.group(4)

            // Convertir qrValue a boolean (true o false)
            val qr = qrValue.toBoolean()

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






