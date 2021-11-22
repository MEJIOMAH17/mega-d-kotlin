plugins {
    kotlin("jvm") version "1.5.10"
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:1.6.3")
    implementation("io.ktor:ktor-client-okhttp:1.6.3")
    implementation("io.ktor:ktor-client-cio:1.6.3")
    implementation("io.ktor:ktor-client-java:1.6.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.10")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.slf4j:slf4j-api:1.7.31")
    implementation("io.github.microutils:kotlin-logging:1.12.5")

    testImplementation ("io.kotest:kotest-assertions-core:4.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
}


tasks.withType<Test> {
    useJUnitPlatform()
}

publishing{
    publications {
        create<MavenPublication>("test"){
            from(components["kotlin"])
        }
    }
}


group = "com.github.mejiomah17"
version = "0.1"
description = "mega-d-kotlin"
