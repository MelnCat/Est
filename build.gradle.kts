plugins {
	java
	kotlin("jvm") version "1.6.10"
	id("com.github.johnrengelman.shadow") version "7.0.0"
	id("io.papermc.paperweight.userdev") version "1.3.3"
	kotlin("plugin.serialization") version "1.6.10"
}

group = "cf.melncat.est"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
	mavenLocal()
	maven("https://repo.purpurmc.org/snapshots")
	maven("https://repo.dmulloy2.net/repository/public/")
	maven("https://repo.mineinabyss.com/releases")
	maven("https://jitpack.io")
	maven("https://nexus.devsrsouza.com.br/repository/maven-public/")
}

dependencies {
	compileOnly(kotlin("stdlib"))
	compileOnly(kotlin("reflect"))
	compileOnly("org.purpurmc.purpur", "purpur-api", "1.18.1-R0.1-SNAPSHOT")
	compileOnly("com.comphenix.protocol", "ProtocolLib", "4.7.0")
	compileOnly("de.tr7zw:item-nbt-api-plugin:2.6.0")
	compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
	implementation("org.reflections:reflections:0.10.2")
	implementation("com.charleskorn.kaml:kaml:0.38.0")
	paperDevBundle("1.18.1-R0.1-SNAPSHOT")
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
	shadowJar {
		dependencies {
			exclude {
				it.moduleName.startsWith("kotlin")
			}
		}
		fun rel(pattern: String) = relocate(pattern, "cf.melncat.est.shaded.$pattern")
		rel("com.charleskorn.kaml")
		rel("org.reflections")
	}

	assemble {
		dependsOn(reobfJar)
	}

	compileJava {
		options.encoding = Charsets.UTF_8.name()
		options.release.set(17)
	}

	compileKotlin {
		kotlinOptions {
			jvmTarget = "17"
			targetCompatibility = "17"
		}
	}
}