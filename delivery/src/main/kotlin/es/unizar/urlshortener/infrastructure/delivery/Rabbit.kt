package es.unizar.urlshortener.infrastructure.delivery

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

// UN sender que se encarga de enviar los mensajes al exchange, este lo que hace es enviar 
// el mensaje a todas las colas que esten vinculadas a el
@Suppress("MagicNumber")
@Component
class Tut3Sender(
    @Autowired private val template: RabbitTemplate,
    @Autowired private val fanout: FanoutExchange,
    //@Autowired private val direct: DirectExchange,
) {

    fun send(message: String) {
         template.convertAndSend(fanout.name, "", message)
        println(" [x] Sent '$message' to fanout exchange ")

        // También puedes enviar el mensaje directamente a una cola específica
        // template.convertAndSend(queue.name, message)
    }
}

// En primer lugar se realizao un receiver que se encarga de recibir los mensajes de las colas
// pero se queria intertar hacer que cada cola fuera consumida por un worker diferente, por lo que
// se creo un receiver para cada cola
@Suppress("MagicNumber")
@Component
class AlcanzabilidadReceiver {

    @RabbitListener(queues = ["#{autoDeleteQueue1.name}"])
    fun receiveReachabilityMessage(message: String) {
        // Extraer url y el hash de este mensaje "CHECK_REACHABILITY:${it.hash}:${data.url}" 
        val pattern = Pattern.compile("CHECK_REACHABILITY:(\\w+):(.+)")
        val matcher = pattern.matcher(message)

        if (matcher.matches()) {
            val hash = matcher.group(1)
            val url = matcher.group(2)

            InsertarUrlAcortada("urlString", "urlAcortada", false, "qrUrlString", 1)
            println(" ALCANZABILIDAD --> Hash: $hash")
            println(" ALCANZABILIDAD --> URL: $url") 

            // Llamar a la funcion que comprueba que la url es alcanzable
            // si la url es alcanzable introducirla en la base de datos
            
            // si no es alcanzable gestion de errores

        } else {
            println("Received unrecognized message: $message")
        }
        
    }
}

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

            println("SOY EL RECEIVER 2")
            println(" GENERA QR --> Hash: $hash")
            println(" GENERA QR --> URL: $url") 

            // Quedar comprobandome llamando a la bbdd si la url es alcanzable
            // estaria bien una variable true false de alcanzable
            // si la url es alcanzable generar el QR y guardarlo en la base de datos (o en el hashmap)
            // si no es alcanzable gestion de errores

            
        } else {
            println("Received unrecognized message: $message")
        }

    }
}
