# Imagen base de Java
FROM openjdk:17
# Establece el directorio de trabajo en /app
WORKDIR /app
# Copia el archivo JAR de tu aplicación al contenedor 
COPY app/build/libs/app-0.2023.1-SNAPSHOT.jar app.jar
# Comando para ejecutar la aplicación cuando se inicie el contenedor
CMD ["java", "-jar", "app.jar"]
