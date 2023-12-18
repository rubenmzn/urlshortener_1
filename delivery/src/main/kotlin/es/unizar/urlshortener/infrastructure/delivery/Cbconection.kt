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

@Suppress("ALL")
fun urlExiste(url: String): Boolean {
    val conexion: Connection? = ObtenerConexion()
    conexion?.let {
        try {
            val statement = it.createStatement()

            // Consulta para verificar si la URL ya existe en la base de datos
            val sql = "SELECT COUNT(*) FROM urlServices WHERE url = ?"
            val preparedStatement = it.prepareStatement(sql)

            // Parámetro para la consulta
            preparedStatement.setString(1, url)

            // Ejecutar la consulta y obtener el resultado
            val resultSet = preparedStatement.executeQuery()
            resultSet.next()
            val count = resultSet.getInt(1)

            // Si el conteo es mayor que 0, significa que la URL ya existe
            return count > 0
        } catch (e: Exception) {
            println("Hubo un error al verificar la existencia de la URL.")
            return false
        } finally {
            try {
                it.close()
            } catch (e: Exception) {
                println("Hubo un error al cerrar la conexión.")
            }
        }
    }
    return false
}

@Suppress("ALL")
fun obtenerValorAlcanzable(url: String): Int? {
    val conexion: Connection? = ObtenerConexion()
    conexion?.let {
        try {
            val statement = it.createStatement()

            // Consulta para obtener el valor de la columna 'alcanzable' para la URL específica
            val sql = "SELECT alcanzable FROM urlServices WHERE url = ?"
            val preparedStatement = it.prepareStatement(sql)

            // Parámetro para la consulta
            preparedStatement.setString(1, url)

            // Ejecutar la consulta y obtener el resultado
            val resultSet = preparedStatement.executeQuery()

            // Si hay resultados, obtener el valor de la columna 'alcanzable'
            return if (resultSet.next()) {
                resultSet.getInt("alcanzable")
            } else {
                null // La URL no se encontró en la base de datos
            }
        } catch (e: Exception) {
            println("Hubo un error al obtener el valor de alcanzable para la URL.")
            return null
        } finally {
            try {
                it.close()
            } catch (e: Exception) {
                println("Hubo un error al cerrar la conexión.")
            }
        }
    }
    return null
}

