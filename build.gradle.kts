plugins {
    id("java-library")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.by1337.space/repository/maven-releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit")
    }

    compileOnly(files("libs/BMenu-2.6.jar"))
    compileOnly("dev.by1337.edsl:EventDSL:1.5.2")
    compileOnly("dev.by1337.yaml:common:1.6")
    compileOnly("dev.by1337.core:BLibV2:1.7.8")

    compileOnly("com.zaxxer:HikariCP:7.0.2")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.3")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.5.2")
    runtimeOnly("org.xerial:sqlite-jdbc:3.53.2.1")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
