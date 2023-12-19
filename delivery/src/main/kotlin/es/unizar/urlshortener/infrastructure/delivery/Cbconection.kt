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
fun ActualizarUrlAcortada(url: String, urlAcortada: String, qr: Boolean, qrUrl: String, alcanzable: Int) {
    val conexion: Connection? = ObtenerConexion()
    conexion?.let {
        try {
            val statement = it.createStatement()

            val sql = "UPDATE urlServices SET qrurl=?, urlacortada=?, qr=?, alcanzable=? WHERE url=?"
            val preparedStatement = it.prepareStatement(sql)

            // Parámetros para la actualización
            preparedStatement.setString(5, url)
            preparedStatement.setString(1, qrUrl)
            preparedStatement.setString(2, urlAcortada)
            preparedStatement.setBoolean(3, qr)
            preparedStatement.setInt(4, alcanzable)

            // Ejecutar la actualización
            preparedStatement.executeUpdate()

            println("URL actualizada correctamente.")
        } catch (e: Exception) {
            println("Ha ocurrido un error al actualizar la URL.")
            e.printStackTrace()
        } finally {
            try {
                it.close()
            } catch (e: Exception) {
                println("Ha ocurrido un error al cerrar la conexión.")
            }
        }
    }
}

@Suppress("ALL")
fun actualizarQR(url: String, qr: ByteArray, id: String) {
    val conexion: Connection? = ObtenerConexion()
    conexion?.let {
        try {
            val statement = it.createStatement()

            val sql = "UPDATE urlServices SET qrCode=?, idQr=? WHERE url=?"
            val preparedStatement = it.prepareStatement(sql)

            // Parámetros para la actualización
            preparedStatement.setBytes(1, qr)
            preparedStatement.setString(2, id)
            preparedStatement.setString(3, url)

            // Ejecutar la actualización
            preparedStatement.executeUpdate()

            println("QR actualizada correctamente.")
        } catch (e: Exception) {
            println("Ha ocurrido un error al actualizar el QR.")
            e.printStackTrace()
        } finally {
            try {
                it.close()
            } catch (e: Exception) {
                println("Ha ocurrido un error al cerrar la conexión.")
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
@Suppress("ALL")
fun obtenerUrlInfo(urlAcortada: String): Pair<String?, Int?> {
    val conexion: Connection? = ObtenerConexion()
    conexion?.let {
        try {
            val statement = it.createStatement()

            // Consulta para obtener la URL original y el valor de 'alcanzable' correspondiente a la URL acortada
            val sql = "SELECT url, alcanzable FROM urlServices WHERE urlacortada = ?"
            val preparedStatement = it.prepareStatement(sql)

            // Parámetro para la consulta
            preparedStatement.setString(1, urlAcortada)

            // Ejecutar la consulta y obtener el resultado
            val resultSet = preparedStatement.executeQuery()

            // Si hay resultados, obtener la URL original y el valor de 'alcanzable'
            return if (resultSet.next()) {
                val urlOriginal = resultSet.getString("url")
                val alcanzable = resultSet.getInt("alcanzable")
                Pair(urlOriginal, alcanzable)
            } else {
                Pair(null, null) // La URL acortada no se encontró en la base de datos
            }
        } catch (e: Exception) {
            println("Hubo un error al obtener la información para la URL acortada.")
            return Pair(null, null)
        } finally {
            try {
                it.close()
            } catch (e: Exception) {
                println("Hubo un error al cerrar la conexión.")
            }
        }
    }
    return Pair(null, null)
}

@Suppress("ALL")
fun obtenerValorQr(id: String): ByteArray? {
    val conexion: Connection? = ObtenerConexion()
    conexion?.let {
        try {
            val statement = it.createStatement()

            // Consulta para obtener el valor de la columna 'alcanzable' para la URL específica
            val sql = "SELECT qrCode FROM urlServices WHERE idQr = ?"
            val preparedStatement = it.prepareStatement(sql)

            // Parámetro para la consulta
            preparedStatement.setString(1, id)

            // Ejecutar la consulta y obtener el resultado
            val resultSet = preparedStatement.executeQuery()

            // Si hay resultados, obtener el valor de la columna 'alcanzable'
            return if (resultSet.next()) {
                resultSet.getBytes("qrCode")
            } else {
                null // La URL no se encontró en la base de datos
            }
        } catch (e: Exception) {
            println("Hubo un error al obtener el valor del codigo qr.")
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

