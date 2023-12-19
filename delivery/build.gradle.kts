plugins {
    id("urlshortener.spring-library-conventions")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("commons-validator:commons-validator:${Version.COMMONS_VALIDATOR}")
    implementation("com.google.guava:guava:${Version.GUAVA}")
    implementation("io.github.g0dkar:qrcode-kotlin-jvm:3.3.0")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation ("com.opencsv:opencsv:5.6")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Version.MOCKITO}")
}
