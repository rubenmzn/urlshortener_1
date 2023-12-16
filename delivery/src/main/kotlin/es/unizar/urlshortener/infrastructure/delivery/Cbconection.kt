package es.unizar.urlshortener.infrastructure.delivery

import java.sql.Connection
import java.sql.DriverManager

@Suppress("ALL")
fun ObtenerConexion(): Connection? {
    val jdbcUrl = "jdbc:mysql://localhost:3306/mydatabase"
    //val jdbcUrl = "jdbc:mysql://mysql:3306/mydatabase"

    val username = "myuser"
    val password = "mypassword"

    return try {
        Class.forName("com.mysql.cj.jdbc.Driver")
        DriverManager.getConnection(jdbcUrl, username, password)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Suppress("ALL")
fun InsertarUrlAcortada(url: String, urlAcortada: String, qr: Boolean, qrUrl: String, alcanzable: Int){
    val conexion: Connection? = ObtenerConexion()
    conexion?.let {
        try {
            val statement = it.createStatement()

            val sql = "INSERT INTO urlServices (url, qrurl, urlacortada, qr, alcanzable) VALUES (?, ?, ?, ?, ?)"
            val preparedStatement = it.prepareStatement(sql)

            // Parámetros para la consulta
            preparedStatement.setString(1, url)
            preparedStatement.setBoolean(4, qr)  // Valor para la columna 'qr'
            preparedStatement.setString(3, urlAcortada)       // Valor para la columna 'alcanzable'
            preparedStatement.setString(2, qrUrl)
            preparedStatement.setInt(5, alcanzable)

            // Ejecutar la consulta
            preparedStatement.executeUpdate()

            println("URL insertada correctamente.")
        } catch (e: Exception) {
            println("HA ido mal")
        } finally {
            try {
                it.close()
            } catch (e: Exception) {
                println("HA ido mal")
            }
        }
    }
}
