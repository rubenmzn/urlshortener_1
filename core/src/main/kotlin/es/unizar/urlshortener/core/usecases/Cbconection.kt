package es.unizar.urlshortener.core.usecases

import java.sql.Connection
import java.sql.DriverManager

fun ObtenerConexion(): Connection? {
    val jdbcUrl = "jdbc:mysql://localhost:3306/mydatabase"
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
