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


// En primer lugar se realizao un receiver que se encarga de recibir los mensajes de las colas
// pero se queria intertar hacer que cada cola fuera consumida por un worker diferente, por lo que
// se creo un receiver para cada cola


@Suppress("MagicNumber")
@Component
class AlcanzabilidadReceiver {

    @RabbitListener(queues = ["#{autoDeleteQueue1.name}"])
    fun receiveReachabilityMessage(message: String) {
        // Extraer url y el hash de este mensaje "CHECK_REACHABILITY:${it.hash}:${data.url}" 
        val pattern = Pattern.compile("CHECK_REACHABILITY:(\\w+):(.+):(.+)")
        val matcher = pattern.matcher(message)

        if (matcher.matches()) {
            val hash = matcher.group(1)
            val url = matcher.group(2)
            val qrValue = matcher.group(3)

            // Convertir qrValue a boolean (true o false)
            val qr = qrValue.toBoolean()

            println(" ALCANZABILIDAD --> Hash: $hash")
            println(" ALCANZABILIDAD --> URL: $url") 
            println(" ALCANZABILIDAD --> QR: $qr")

            // mirar si la url ya esta en la base de datos
            if (urlExiste(url)) {
                println("La URL ya existe en la base de datos.")
                // Combrobar en la base datos si es alcanzable
                val valorAlcanzable = obtenerValorAlcanzable(url)
                if (valorAlcanzable == 0) {
                    println("No se ha mirado si es alcanzable")
                    // comprobar alcanzabilidad y update bbdd

                } else if (valorAlcanzable == 1) {
                    println("La url es alcanzable")
                    // Por si acaso update por si el qr ahora es true
                    
                } else if (valorAlcanzable == 2) {
                    println("La url no es alcanzable")
                    // supongo que gestión de errores 

                } else {
                    println("Ha habido un error al comprobar si la url es alcanzable")
                }

            } else {
                println("La URL no existe en la base de datos. Puedes proceder con la inserción.")
                // Como no se encuentra en la base de datos, se inserta
                InsertarUrlAcortada(url, "", qr, "", 1)
                // Esto es como el post, porque aun se tiene porque acortar la url¿?¿ 
                // pero si insertarla para que eso se pueda comproabr
                
            }

            // Llamar a la funcion que comprueba que la url es alcanzable
            // si la url es alcanzable introducirla en la base de datos
            
            // si no es alcanzable gestion de errores

        } else {
            println("Received unrecognized message: $message")
        }
        
    }
}






