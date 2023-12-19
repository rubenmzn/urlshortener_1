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


@Suppress("MagicNumber")
@Component
class Rabbit(
    @Autowired private val template: RabbitTemplate,
    @Autowired private val fanout: FanoutExchange,
) {

    /*
     * Fanout exchange: It routes messages to all of the queues that are bound to it and the routing key is ignored.
     * @param message Message to send
     */
    fun send(message: String) {
        template.convertAndSend(fanout.name, "", message)
    }
}





