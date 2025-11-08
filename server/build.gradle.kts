plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
    application
}


kotlin { jvmToolchain(21) }

application {
    mainClass.set("com.example.server.ApplicationKt")
}

dependencies {
    val ktor = "2.3.12" // use a Kotlin-2.x friendly Ktor
    implementation("io.ktor:ktor-server-netty:$ktor")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
    implementation("io.ktor:ktor-server-cors:$ktor")
    implementation("io.ktor:ktor-server-call-logging:$ktor")
    implementation("io.ktor:ktor-server-auth:$ktor")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor")

    implementation("org.jetbrains.exposed:exposed-core:0.54.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.54.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.54.0")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("at.favre.lib:bcrypt:0.10.2")

    implementation("ch.qos.logback:logback-classic:1.5.6")
    testImplementation(kotlin("test"))
}
