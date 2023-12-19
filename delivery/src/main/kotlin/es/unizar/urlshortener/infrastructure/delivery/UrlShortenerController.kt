package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import es.unizar.urlshortener.core.usecases.CreateQrUseCase
import es.unizar.urlshortener.core.usecases.ReachableUrlCase
import es.unizar.urlshortener.core.InvalidUrlException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import java.net.URI
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileWriter
import java.io.IOException
import org.springframework.core.io.FileSystemResource
import java.io.InputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import com.opencsv.CSVReaderBuilder
import java.io.FileInputStream
import org.springframework.beans.factory.annotation.Autowired


/**
 * The specification of the controller.
 */
interface UrlShortenerController {

    /**
     * Redirects and logs a short url identified by its [id].
     *
     * **Note**: Delivery of use cases [RedirectUseCase] and [LogClickUseCase].
     */
    fun redirectTo(id: String, request: HttpServletRequest): ResponseEntity<Unit>

    /**
     * Creates a short url from details provided in [data].
     *
     * **Note**: Delivery of use case [CreateShortUrlUseCase].
     */
    fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>

    /**
     * Generates a qr code for the short url identified by its [id].
     */
    fun qr(id: String, request: HttpServletRequest): ResponseEntity<ByteArrayResource>

     /**
     * Generates a qr code for the short url identified by its [id].
     */
    fun bulkShortenUrl(@RequestParam("file") file: MultipartFile, request: HttpServletRequest): ResponseEntity<Resource>
}

/**
 * Data required to create a short url.
 */
data class ShortUrlDataIn(
    val url: String,
    val sponsor: String? = null,
    val qr: Boolean
)

/**
 * Data returned after the creation of a short url.
 */
data class ShortUrlDataOut(
    val url: URI? = null,
    val properties: Map<String, Any?> = emptyMap()
)

/**
 * The implementation of the controller.
 *
 * **Note**: Spring Boot is able to discover this [RestController] without further configuration.
 */
@Suppress("LongParameterList")
@RestController
class UrlShortenerControllerImpl(
    val redirectUseCase: RedirectUseCase,
    val logClickUseCase: LogClickUseCase,
    val createShortUrlUseCase: CreateShortUrlUseCase,
    val reachableUrlCase: ReachableUrlCase,
    val createQrUseCase: CreateQrUseCase,
    @Autowired val sender: Rabbit,
) : UrlShortenerController {

    @Operation(
        summary = "Redirige a la URL especificada",
        description = "Redirige a la URL correspondiente al ID proporcionado.",
        responses = [
            ApiResponse(responseCode = "302", description = "Redirección exitosa",
                content = [Content()]),
            ApiResponse(responseCode = "404", description = "ID no encontrado",
                content = [Content()])
        ]
    )
    @GetMapping("/{id:(?!api|index).*}")
    override fun redirectTo(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<Unit> =
        redirectUseCase.redirectTo(id).let {
            
            logClickUseCase.logClick(id, ClickProperties(ip = request.remoteAddr))
            val h = HttpHeaders()
            h.location = URI.create(it.target)
            println("status: " + it.mode)
            ResponseEntity<Unit>(h, HttpStatus.valueOf(it.mode))
        }

    @Operation(
        summary = "Acorta una URL y genera un código QR opcionalmente",
        description = "Crea una URL corta para la URL proporcionada, con la opción de generar un código QR.",
        responses = [
            ApiResponse(responseCode = "201", description = "URL corta creada exitosamente",
                        content = [Content(mediaType = "application/json")]),
            ApiResponse(responseCode = "400", description = "Error en la solicitud o datos inválidos",
                        content = [Content(mediaType = "application/json")])
        ]
    )
    @PostMapping("/api/link", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    override fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =
      //  if (reachableUrlCase.checkReachable(data.url)){
            createShortUrlUseCase.create(
                url = data.url,
                data = ShortUrlProperties(
                    ip = request.remoteAddr,
                    sponsor = data.sponsor,
                    qr = data.qr
                )
            ).let {
                val h = HttpHeaders()
                val url = linkTo<UrlShortenerControllerImpl> { redirectTo(it.hash, request) }.toUri()
                h.location = url

                // AHORA ANTES DE CREAR EL QR LO METO A LA COLA DE MENSAJES PARA CREARLO CUANDO SE RECIBA EL MENSAJE
                // Enviar mensaje a la cola para comprobar la alcanzabilidad
                sender.send("CHECK_REACHABILITY;${it.hash};${data.url};${data.qr};${url}")
                val qr: Any? = if (data.qr) {
                    // Enviar mensaje a la cola para generar el código QR
                    sender.send("GENERATE_QR:${it.hash}:${data.url}")
                    linkTo<UrlShortenerControllerImpl> { qr(it.hash, request) }.toUri()
                } else {
                    null
                }

                val response = ShortUrlDataOut(
                    url = url,
                    properties = mapOf(
                        "safe" to it.properties.safe,
                        "qr" to qr
                    )   
                )


                ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
            }

    @Operation(
        summary = "Obtiene el código QR para una URI específica",
        description = "Obtiene el código QR correspondiente al ID proporcionado.",
        responses = [
            ApiResponse(responseCode = "200", description = "Éxito en la obtención del código QR",
                        content = [Content(mediaType = "image/png")]),
            ApiResponse(responseCode = "404", description = "ID no encontrado o código QR no habilitado",
                        content = [Content()]),
            ApiResponse(responseCode = "400", description = "URI de destino no validada todavía",
                        content = [Content()])
        ]
    )
    @GetMapping("/{id:(?!api|index).*}/qr")
    override fun qr(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<ByteArrayResource> {
        // Devolver el QR 
        val h = HttpHeaders()
        h.set(HttpHeaders.CONTENT_TYPE, IMAGE_PNG_VALUE)
        return ResponseEntity<ByteArrayResource>(ByteArrayResource(createQrUseCase.get(id) , 
        IMAGE_PNG_VALUE), h, HttpStatus.OK)
    }

    @Operation(
        summary = "Procesa un archivo CSV para crear múltiples URL cortas",
        description = "Procesa un archivo CSV que contiene URIs para crear múltiples URL cortas, y opcionalmente QRs.",
        responses = [
            ApiResponse(responseCode = "201", description = "URLs cortas creadas exitosamente",
                        content = [Content(mediaType = "text/csv")]),
            ApiResponse(responseCode = "400", description = "Error en la solicitud o datos inválidos",
                        content = [Content(mediaType = "application/json")]),
            ApiResponse(responseCode = "200", description = "El fichero está vacío")
        ]
    )
    @Suppress("ALL")
    @PostMapping("/api/bulk", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    override fun bulkShortenUrl(@RequestParam("file") file: MultipartFile, request: HttpServletRequest): ResponseEntity<Resource>{ 
        if (file.isEmpty) {
              return ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, "attachment; filename=${file.originalFilename}")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(InputStreamResource(file.inputStream))
        }
        val csvData: InputStream = file.inputStream
        val csvReader = CSVReaderBuilder(InputStreamReader(csvData)).build()
        val header = csvReader.readNext()
        var hayQR = false
        if (!header[0].contains("URI", ignoreCase = true)) {
             return ResponseEntity.badRequest().body(null)
        }
        if (header[0].contains("QR", ignoreCase = true)) {
             hayQR = true
        }
        val lines = csvReader.readAll()
        if (lines.isEmpty()) {
            csvReader.close()
            return ResponseEntity.badRequest().body(null)
        }
        csvReader.close()
        val responses = mutableListOf<String>()

        for (urlData in lines) {
            val shortUrlData = ShortUrlDataIn(
                url = urlData[0],
                qr = hayQR
            )
            responses.add(urlData[0])

            try {
                val response = shortener(shortUrlData, request)

                if (response.statusCodeValue == 200 || response.statusCodeValue == 201) {
                    // Obtener la URL acortada de la respuesta
                    val shortUrl = response.body?.url.toString()
                    responses.add(shortUrl)
                    if(hayQR){
                        responses.add(response.body?.properties?.get("qr").toString())
                    }
                } else {
                    responses.add("ERROR")
                }
            } catch (e: Exception) {
                responses.add("ERROR: ${e.message}")
                if(hayQR){
                    responses.add("ERROR: ${e.message}")
                }
            }
        }
         val csvFile = createCsvFile(responses, hayQR)

        csvData.close()
        return ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, "attachment; filename=${csvFile.name}")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(InputStreamResource(FileInputStream(csvFile)))
        
    }

}

@Suppress("ALL")
private fun createCsvFile(shortenedUrls: List<String>, hayQR: Boolean): File {
    val csvFile = File.createTempFile("shortened_urls_", ".csv")

    try {
        val writer = FileWriter(csvFile)
        writer.append("Original;Acortada;QR\n")

        if(hayQR){
            for (i in 0 until shortenedUrls.size step 3) {
                val originalUrl = shortenedUrls.getOrNull(i) ?: ""
                val shortenedUrl = shortenedUrls.getOrNull(i + 1) ?: ""
                val shortenedQrUrl = shortenedUrls.getOrNull(i + 2) ?: ""
                writer.append("$originalUrl;$shortenedUrl;$shortenedQrUrl\n")
            }
        }else{
            for (i in 0 until shortenedUrls.size step 2) {
                val originalUrl = shortenedUrls.getOrNull(i) ?: ""
                val shortenedUrl = shortenedUrls.getOrNull(i + 1) ?: ""
                writer.append("$originalUrl;$shortenedUrl\n")
            }
        }
        writer.close()
    } catch (e: IOException) {
        println("Error al leer el archivo CSV: ${e.message}")
    }

    return csvFile
}
