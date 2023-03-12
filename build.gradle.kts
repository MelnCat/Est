import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
	java
	kotlin("jvm") version "1.6.20"
	id("com.github.johnrengelman.shadow") version "7.0.0"
	id("io.papermc.paperweight.userdev") version "1.3.3"
	kotlin("plugin.serialization") version "1.6.10"
}

group = "dev.melncat.est"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
	mavenLocal()
	maven("https://repo.purpurmc.org/snapshots")
	maven("https://jitpack.io")
	maven("https://repo.codemc.org/repository/maven-public/")
	maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
	compileOnly(kotlin("stdlib"))
	compileOnly(kotlin("reflect"))
	paperweightDevBundle("org.purpurmc.purpur", "1.19.3-R0.1-SNAPSHOT")
	compileOnly("org.purpurmc.purpur:purpur-api:1.19.3-R0.1-SNAPSHOT")

	compileOnly("dev.melncat.furcation:Furcation:0.0.2")
	compileOnly("com.github.MilkBowl:VaultAPI:1.7")

	compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
	compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
	compileOnly("org.tukaani:xz:1.9")
	compileOnly("org.reflections:reflections:0.10.2")
	compileOnly("org.graalvm.truffle:truffle-api:22.2.0")
	compileOnly("cloud.commandframework:cloud-paper:1.8.0")
	compileOnly("cloud.commandframework:cloud-kotlin-extensions:1.8.0")
	compileOnly("cloud.commandframework:cloud-kotlin-coroutines:1.8.0")
	compileOnly("xyz.xenondevs.nova:nova:0.12.11")
	compileOnly(files("./lib/GSit-1.2.7.jar"))

	implementation("com.charleskorn.kaml:kaml:0.49.0")

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
		fun rel(pattern: String) = relocate(pattern, "dev.melncat.est.shaded.$pattern")
		listOf(
			"com.charleskorn.kaml",
			"org.graalvm.js"
		).forEach(::rel)
		fun fshaded(pkg: String) = relocate(pkg, "dev.melncat.furcation.shaded.$pkg")
		listOf(
			"kotlinx",
			"org.reflections",
			"org.tukaani.xz",
			"cloud.commandframework",
			"io.leangen.geantyref",
			"com.github.stefvanschie.inventoryframework"
		).forEach(::fshaded)
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