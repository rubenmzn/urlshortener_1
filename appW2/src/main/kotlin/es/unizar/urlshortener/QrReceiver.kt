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



@Suppress("MagicNumber")
@Component
class QrReceiver { 

    @RabbitListener(queues = ["#{autoDeleteQueue2.name}"])
    fun receiveQrGenerationMessage(message: String) {
        val pattern = Pattern.compile("GENERATE_QR:(\\w+):(.+)")
        val matcher = pattern.matcher(message)

        if (matcher.matches()) {
            val hash = matcher.group(1)
            val url = matcher.group(2) 

            println(" GENERA QR --> Hash: $hash")
            println(" GENERA QR --> URL: $url") 

            // Quedar comprobandome llamando a la bbdd si la url es alcanzable
            var valorAlcanzable = obtenerValorAlcanzable(url)
            // Realizar la comprobación hasta que alcanzable sea 1 o 2
            while (valorAlcanzable != 1 && valorAlcanzable != 2) {
                println("La URL no es alcanzable. Esperando antes de la siguiente comprobación...")
                TimeUnit.SECONDS.sleep(5) // Esperar 5 segundos antes de la siguiente comprobación

                // Volver a obtener el valor de alcanzable
                valorAlcanzable = obtenerValorAlcanzable(url)
            }

            // Si alcanzable es 1, generar el QR
            if (valorAlcanzable == 1) {
                println("QR generado correctamente.")
            } else {
                println("La URL no es alcanzable, no se generará el QR.")
            }
            
        } else {
            println("Received unrecognized message: $message")
        }

    }
}


