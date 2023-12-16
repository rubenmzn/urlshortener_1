

import org.springframework.amqp.rabbit.annotation.RabbitListener

public class QueueWorker {

    @RabbitListener(queues = "#(autoDeleteQueueAlcanzable)")

    @RabbitListener(queues = "#(autoDeleteQueueQR)")
    fun 

}
