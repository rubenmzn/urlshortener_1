package es.unizar.urlshortener

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import QueueWorker

@SpringBootApplication
class Application2 @Autowired constructor(private val worker: QueueWorker) : CommandLineRunner {

    override fun run(vararg args: String?) {
        // Llamar a la tarea en segundo plano
        worker.realizarTarea()

        // Lógica principal de la aplicación
        // ...
    }
}

fun main(args: Array<String>) {
    runApplication<Application2>(*args)
}
