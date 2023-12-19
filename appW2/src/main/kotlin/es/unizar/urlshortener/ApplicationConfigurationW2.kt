package es.unizar.urlshortener

import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCaseImpl
import es.unizar.urlshortener.core.usecases.LogClickUseCaseImpl
import es.unizar.urlshortener.core.usecases.RedirectUseCaseImpl
import es.unizar.urlshortener.core.usecases.CreateQrUseCaseImpl
import es.unizar.urlshortener.core.usecases.ReachableUrlCaseImpl
import es.unizar.urlshortener.core.usecases.BulkShortenUrlUseCase
import es.unizar.urlshortener.infrastructure.delivery.HashServiceImpl
import es.unizar.urlshortener.infrastructure.delivery.QrServiceImpl
import es.unizar.urlshortener.infrastructure.delivery.UrlServiceImpl
//import es.unizar.urlshortener.infrastructure.delivery.AlcanzabilidadReceiver
//import es.unizar.urlshortener.infrastructure.delivery.QrReceiver
//import es.unizar.urlshortener.infrastructure.delivery.RabbitMQServiceImpl
import es.unizar.urlshortener.infrastructure.delivery.ValidatorServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ClickEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.ClickRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlRepositoryServiceImpl
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.AnonymousQueue
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.context.annotation.Profile
import javax.annotation.PostConstruct
// anade import value
import org.springframework.beans.factory.annotation.Value
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.core.DirectExchange


/**
 * Wires use cases with service implementations, and services implementations with repositories.
 *
 * **Note**: Spring Boot is able to discover this [Configuration] without further configuration.
 */
@Suppress("TooManyFunctions")
//@Profile("tut3", "pub-sub", "publish-subscribe")
@Configuration
class ApplicationConfigurationW2(
    @Autowired val shortUrlEntityRepository: ShortUrlEntityRepository,
    @Autowired val clickEntityRepository: ClickEntityRepository,
) {
    @Bean
    fun clickRepositoryService() = ClickRepositoryServiceImpl(clickEntityRepository)

    @Bean
    fun shortUrlRepositoryService() = ShortUrlRepositoryServiceImpl(shortUrlEntityRepository)

    @Bean
    fun validatorService() = ValidatorServiceImpl()

    @Bean
    fun hashService() = HashServiceImpl()

    @Bean
    fun qrService() = QrServiceImpl()

    @Bean 
    fun urlService() = UrlServiceImpl()

    @Bean
    fun redirectUseCase() = RedirectUseCaseImpl(/*shortUrlRepositoryService(), */ urlService())

    @Bean
    fun logClickUseCase() = LogClickUseCaseImpl(clickRepositoryService())

    @Bean
    fun createShortUrlUseCase() =
        CreateShortUrlUseCaseImpl(shortUrlRepositoryService(), validatorService(), hashService())
    
    @Bean
    fun reachableUrlCase() = ReachableUrlCaseImpl(validatorService())

    @Bean
    fun createQrUseCase() =
        CreateQrUseCaseImpl( qrService(), urlService())
    
    @Bean
    fun bulkShortenUrlUseCase() =
        BulkShortenUrlUseCase(shortUrlRepositoryService(), validatorService(), hashService())
    

    @Bean
    fun connectionFactory(): ConnectionFactory {
        // Configuración de la conexión
        return CachingConnectionFactory()
    }


    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        // Configuración adicional si es necesaria
        return template
    }

    @Bean
    fun fanout(): FanoutExchange {
        return FanoutExchange("tut.fanout")
    }


    
    @Bean
    fun autoDeleteQueue2(): Queue {
        return AnonymousQueue()
    }

    @Bean 
    fun binding2(fanout: FanoutExchange, autoDeleteQueue2: Queue): Binding {
        return BindingBuilder.bind(autoDeleteQueue2).to(fanout)
    }

    @Bean  
    fun receiver2(): QrReceiver { 
        return QrReceiver(createQrUseCase())
    }
    

}


