@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import es.unizar.urlshortener.core.usecases.CreateQrUseCase
import es.unizar.urlshortener.core.usecases.ReachableUrlCase
import es.unizar.urlshortener.core.usecases.BulkShortenUrlUseCase
//import es.unizar.urlshortener.core.RabbitMQService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import com.opencsv.CSVReaderBuilder
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.ByteArrayInputStream

@WebMvcTest
@ContextConfiguration(
    classes = [
        UrlShortenerControllerImpl::class,
        RestResponseEntityExceptionHandler::class
    ]
)
class UrlShortenerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var redirectUseCase: RedirectUseCase

    @MockBean
    private lateinit var logClickUseCase: LogClickUseCase

    @MockBean
    private lateinit var createShortUrlUseCase: CreateShortUrlUseCase

    @MockBean
    private lateinit var createQrUseCase: CreateQrUseCase

    @Suppress("UnusedPrivateProperty")
    @MockBean
    private lateinit var reachableUrlCase: ReachableUrlCase

    @Suppress("UnusedPrivateProperty")
    @MockBean
    private lateinit var bulkShortenUrlUseCase: BulkShortenUrlUseCase

    /*@Suppress("UnusedPrivateProperty")
    @MockBean
    private lateinit var rabbitMQService: RabbitMQService*/


    @Test
    fun `redirectTo returns a redirect when the key exists`() {
        given(redirectUseCase.redirectTo("key")).willReturn(Redirection("http://example.com/"))

        mockMvc.perform(get("/{id}", "key"))
            .andExpect(status().isTemporaryRedirect)
            .andExpect(redirectedUrl("http://example.com/"))

        verify(logClickUseCase).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        given(redirectUseCase.redirectTo("key"))
            .willAnswer { throw RedirectionNotFound("key") }

        mockMvc.perform(get("/{id}", "key"))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.statusCode").value(404))

        verify(logClickUseCase, never()).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {
        given(
            createShortUrlUseCase.create(
                url = "http://example.com/",
                data = ShortUrlProperties(ip = "127.0.0.1")
            )
        ).willReturn(ShortUrl("f684a3c4", Redirection("http://example.com/")))

        mockMvc.perform(
            post("/api/link")
                .param("url", "http://example.com/")
                .param("qr", "false")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(redirectedUrl("http://localhost/f684a3c4"))
            .andExpect(jsonPath("$.url").value("http://localhost/f684a3c4"))
    }

    @Test
    fun `creates returns bad request if it can compute a hash`() {
        given(
            createShortUrlUseCase.create(
                url = "ftp://example.com/",
                data = ShortUrlProperties(ip = "127.0.0.1")
            )
        ).willAnswer { throw InvalidUrlException("ftp://example.com/") }

        mockMvc.perform(
            post("/api/link")
                .param("url", "ftp://example.com/")
                .param("qr", "false")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.statusCode").value(400))
    }  

    @Test
    fun `get returns a valid qr image if the hash exists`() {
        given(
            createShortUrlUseCase.create(
                url = "http://www.example.com/",
                data = ShortUrlProperties(
                    ip = "127.0.0.1",
                    qr = true
                )
            )
        ).willReturn(ShortUrl("f684a3c4", Redirection("http://www.example.com/")))

        given(
            createQrUseCase.get(
                id = "f684a3c4"
            )
        ).willReturn("http://example.com/".toByteArray())

        mockMvc.perform(
            post("/api/link")
                .param("url", "http://www.example.com/")
                .param("qr", "true")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(redirectedUrl("http://localhost/f684a3c4"))
            .andExpect(jsonPath("$.url").value("http://localhost/f684a3c4"))

        mockMvc.perform(
            get("http://localhost/{id}/qr", "f684a3c4")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
    }

    @Test
    fun `get returns bad request if the qr can't be found`() {
        given(
            createQrUseCase.get(
                id = "f684a3c4"
            )
        ).willAnswer { throw RedirectionNotFound("http://localhost/f684a3c4/qr") }

        mockMvc.perform(
            get("http://localhost/{id}/qr", "f684a3c4")
        )
            .andDo(print())
            .andExpect(status().isNotFound)
    }

  
    @Test
    fun `bulkShortenUrl returns OK and empty CSV when provided CSV file is empty`() {
        val csvFile = "".byteInputStream()

        mockMvc.perform(
            fileUpload("/api/bulk")
                .file(MockMultipartFile("file", "test.csv", MediaType.MULTIPART_FORM_DATA_VALUE, csvFile))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.parseMediaType("text/csv")))
            .andExpect(content().string(""))
            .andExpect(header().string("Content-Disposition", "attachment; filename=test.csv"))
    }

    @Test
    fun `bulkShortenUrl returns BadRequest when CSV file has incorrect header`() {
        val csvFile = "Invalid Header\nhttp://example.com\n".byteInputStream()

        mockMvc.perform(
            fileUpload("/api/bulk")
                .file(MockMultipartFile("file", "test.csv", MediaType.MULTIPART_FORM_DATA_VALUE, csvFile))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `bulkShortenUrl returns OK and shortened URLs in CSV file`() {
        // Create a temporary CSV file with sample data
        val csvData = """
            URI
            http://example.com
            http://example.org
        """.trimIndent().byteInputStream()

        val tempCsvFile = Files.createTempFile("test", ".csv")
        Files.copy(csvData, tempCsvFile, StandardCopyOption.REPLACE_EXISTING)

        mockMvc.perform(
            fileUpload("/api/bulk")
                .file(MockMultipartFile("file", "test.csv", MediaType.MULTIPART_FORM_DATA_VALUE,
                 Files.newInputStream(tempCsvFile)))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.parseMediaType("text/csv")))
            .andExpect(header().string("Content-Disposition", "attachment; filename=test.csv"))
            .andExpect(content()
            .string("http://example.com;http://localhost/f684a3c4\nhttp://example.org;http://localhost/anotherHash"))

        // Clean up temporary file
        Files.deleteIfExists(tempCsvFile)
    }

    
    @Test
    fun `bulkShortenUrl returns OK with errors for invalid URLs`() {
        val csvData = """
            URI
            http://valid-url.com
            invalid-url
        """.trimIndent().byteInputStream()

        mockMvc.perform(
            fileUpload("/api/bulk")
                .file(MockMultipartFile("file", "test.csv", MediaType.MULTIPART_FORM_DATA_VALUE, csvData))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.parseMediaType("text/csv")))
            .andExpect(header().string("Content-Disposition", "attachment; filename=test.csv"))
            .andExpect(content())
    }



}

