plugins {
    id("java")
    id("org.springframework.boot") version ("3.2.2")
    id("io.spring.dependency-management") version ("1.1.4")
    id("io.freefair.lombok") version "6.6.3"
}

group = "org.cbrevdev.knightpath"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("software.amazon.awssdk:dynamodb-enhanced:2.25.64")
    implementation("software.amazon.awssdk:url-connection-client:2.25.64")
    implementation("software.amazon.awssdk:sqs:2.25.64")

    implementation("com.amazonaws.serverless:aws-serverless-java-container-springboot3:2.0.2")
}

tasks {
    bootRun {
        environment("spring_profiles_active", "local")
    }

    test {
        useJUnitPlatform()
    }

    register<Zip>("buildZip") {
        from(compileJava)
        from(processResources)
        from(configurations.compileClasspath) {
            include("org/cbrevdev/**")
        }
        into("lib") {
            from(configurations.compileClasspath) {
                exclude("org/cbrevdev/**")
                exclude("tomcat-embed-*")
            }
        }
    }
}
