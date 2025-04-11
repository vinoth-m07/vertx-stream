plugins {
    kotlin("jvm") version "2.1.10"
//    id("io.vertx.vertx-plugin") version "1.3.0"
    id("application")
    id("maven-publish")
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    // Vert.x core
    implementation("io.vertx:vertx-core:4.4.5")
    // Vert.x Kotlin support
    implementation("io.vertx:vertx-lang-kotlin:4.4.5")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.4.5")
    implementation("io.vertx:vertx-web:4.5.14")
    // AMPS client (60East's AMPS)
    //implementation("com.crankuptheamps:amps-client:5.3.3.0")
// https://mvnrepository.com/artifact/com.crankuptheamps/amps-client
    implementation("com.crankuptheamps:amps-client:5.3.4.1")
    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.7")
    // Config
    implementation("io.vertx:vertx-config:4.4.5")

    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Better logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
}

application {
    mainClass.set("MainKt")
}

/*vertx {
    mainVerticle.set("com.example.MainVerticle")
}*/

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
    kotlinOptions.freeCompilerArgs += listOf("-Xjsr305=strict")

}
/*publishing {
    publications {
        create<MavenPublication>("mavenJava"){
            from(components["java"])
        }
    }
}*/
kotlin {
    jvmToolchain(21)
}