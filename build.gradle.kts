import com.google.protobuf.gradle.*

plugins {
    java
    kotlin("jvm") version "1.3.72"
    scala
    id("com.google.protobuf") version "0.8.12"
    idea
}

val protobuf_version = "3.11.1"
val grpc_version = "1.28.1"

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    jcenter()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.protobuf:protobuf-java:$protobuf_version")
    implementation("com.google.protobuf:protobuf-java-util:$protobuf_version")
    implementation("io.grpc:grpc-netty-shaded:$grpc_version")
    implementation("io.grpc:grpc-protobuf:$grpc_version")
    implementation("io.grpc:grpc-stub:$grpc_version")
    compileOnly("javax.annotation:javax.annotation-api:1.2")
    implementation("org.scala-lang:scala-library:2.11.12")
    implementation("io.reactivex:rxscala_2.11:0.27.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:$protobuf_version" }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpc_version"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
        }
    }
}
