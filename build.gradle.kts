import java.io.ByteArrayOutputStream
import java.net.URL
import java.nio.channels.Channels

plugins {
    java
}

subprojects {
    apply<JavaPlugin>()

    group = "org.terraform"

    repositories {
        //mavenLocal()
        mavenCentral()
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://mvn-repo.arim.space/lesser-gpl3/")
        //maven("https://libraries.minecraft.net/minecraft-server")
        maven("https://papermc.io/repo/repository/maven-public/")
    }
//    Handle this inside each implementation as different minecraft versions support a different max jvm version
//    java {
//        sourceCompatibility = JavaVersion.VERSION_16
//        targetCompatibility = JavaVersion.VERSION_16
//    }
}

fun gitClone(name: String) {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = mutableListOf("git", "clone", name)
        standardOutput = stdout
    }
}