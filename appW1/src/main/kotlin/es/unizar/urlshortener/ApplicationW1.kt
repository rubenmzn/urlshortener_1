package es.unizar.urlshortener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


/**
 * The marker that makes this project a Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling
class ApplicationW1

/**
 * The main entry point.
 */
fun main(args: Array<String>) {
     if (args.isEmpty()) {
        println("Por favor, proporciona el número de puerto como argumento.")
        return
    }

    val port = args[0].toInt()

   @Suppress("SpreadOperator")
    runApplication<ApplicationW1>(*args, "--server.port=$port")

}
