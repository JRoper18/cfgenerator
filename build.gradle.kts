import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    idea
    antlr
    application
}

group = "me.jroper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val generatedSourcesPath = file("build/classes/java")

java.sourceSets["main"].java.srcDir(generatedSourcesPath)

idea {
    module {
        sourceDirs.add(generatedSourcesPath)
        generatedSourceDirs.add(generatedSourcesPath)
    }
}
dependencies {
    antlr("org.antlr:antlr4:4.7.1")
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.3")
    implementation("org.antlr:antlr4:4.7.1")
    implementation("org.antlr:antlr4-runtime:4.7.1")
    implementation(files("./build/classes/java/main"))

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<AntlrTask>() {
    
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
    dependsOn.add("generateGrammarSource")
    dependsOn.add("generateTestGrammarSource")

}

application {
    mainClass.set("MainKt")
}