package es.unizar.urlshortener

import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.util.StopWatch
import org.springframework.context.annotation.Profile

@Suppress("MagicNumber")
@Component
class Tut3Sender(
    @Autowired private val template: RabbitTemplate,
    @Autowired private val fanout: FanoutExchange
) {

    private val dots = AtomicInteger(0)

    private val count = AtomicInteger(0)

    @Scheduled(fixedDelay = 1000, initialDelay = 500)
    fun send() {
        val builder = StringBuilder("Hello")
        if (dots.getAndIncrement() == 3) {
            dots.set(1)
        }
        for (i in 0 until dots.get()) {
            println("i: $i")
            builder.append('.')
        }
        builder.append(count.incrementAndGet())
        val message = builder.toString()
        template.convertAndSend(fanout.name, "", message)
        println(" [x] Sent '$message'")
    }
}

@Suppress("MagicNumber")
@Component
class Tut3Receiver1 {

    @RabbitListener(queues = ["#{autoDeleteQueue1.name}"])
    fun receive(inMessage: String) {
        receive(inMessage, 1)
    }


    private fun receive(inMessage: String, receiver: Int) {
        val watch = StopWatch()
        watch.start()
        println("instance $receiver [x] Received '$inMessage'")
        doWork(inMessage)
        watch.stop()
        println("instance $receiver [x] Done in ${watch.totalTimeSeconds}s")
    }

    private fun doWork(inMessage: String) {
        for (ch in inMessage.toCharArray()) {
            if (ch == '.') {
                Thread.sleep(1000)
            }
        }
    }
}

@Suppress("MagicNumber")
@Component
class Tut3Receiver2 {

    @RabbitListener(queues = ["#{autoDeleteQueue2.name}"])
    fun receive(inMessage: String) {
        receive(inMessage, 2)
    }

    private fun receive(inMessage: String, receiver: Int) {
        val watch = StopWatch()
        watch.start()
        println("instance $receiver [x] Received '$inMessage'")
        doWork(inMessage)
        watch.stop()
        println("instance $receiver [x] Done in ${watch.totalTimeSeconds}s")
    }

    private fun doWork(inMessage: String) {
        for (ch in inMessage.toCharArray()) {
            if (ch == '.') {
                Thread.sleep(1000)
            }
        }
    }
}
