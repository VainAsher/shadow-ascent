import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

plugins {
    `java`
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.shadowascent.core.HubMissionDemo")
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

fun Project.mainSourceSet() = extensions.getByType(SourceSetContainer::class.java).getByName("main")

task<JavaExec>("runRegressionTests") {
    group = "verification"
    description = "Run regression tests for stability validation"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.shadowascent.core.RegressionTest")
}

task<JavaExec>("runDataContractDiagnostics") {
    group = "verification"
    description = "Validate and report narrative/world data contracts"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.shadowascent.core.DataContractDiagnostics")
}

task<JavaExec>("runWorldgenDiagnostics") {
    group = "verification"
    description = "Validate and report migrated worldgen section templates"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.shadowascent.core.WorldgenDiagnostics")
}

task<JavaExec>("runWorldSimulationDiagnostics") {
    group = "verification"
    description = "Validate and report M5 world/faction/settlement simulation contracts"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.shadowascent.core.WorldSimulationDiagnostics")
}

task<JavaExec>("runRegionalStreamingDiagnostics") {
    group = "verification"
    description = "Validate authored region fragments and run M6 constraint checks"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.shadowascent.core.RegionalStreamingDiagnostics")
}

task<JavaExec>("runPlayableClient") {
    group = "application"
    description = "Launch interactive human-playtest client (MVP)."
    dependsOn(":client:classes")
    classpath = project(":client").mainSourceSet().runtimeClasspath
    mainClass.set("com.shadowascent.client.PlaytestClient")
}

task<JavaExec>("packSprites") {
    group = "build"
    description = "Generate placeholder sprite sheet and atlas for the P3 asset pipeline."
    dependsOn(":client:classes")
    classpath = project(":client").mainSourceSet().runtimeClasspath
    mainClass.set("com.shadowascent.client.tools.SpritePackerTool")
    args("${projectDir}/assets/sprites/packed")
    outputs.dir("${projectDir}/assets/sprites/packed")
}

task<JavaExec>("runGame") {
    group = "application"
    description = "Launch LibGDX game client (Phase 1 rendering stack)."
    dependsOn("packSprites")
    classpath = project(":client").mainSourceSet().runtimeClasspath
    mainClass.set("com.shadowascent.client.DesktopLauncher")
}

dependencies {
    implementation(project(":core"))
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Main-Class" to "com.shadowascent.core.HubMissionDemo")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(project(":core").mainSourceSet().output)
}

tasks.register<Jar>("releaseCandidate") {
    group = "build"
    description = "Assemble a release candidate JAR for the clean Shadow Ascent build."
    archiveBaseName.set("shadow-ascent-release-candidate")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes("Main-Class" to "com.shadowascent.core.HubMissionDemo")
    }
    from(project(":core").mainSourceSet().output)
    dependsOn(":core:classes")
}

project(":core") {
    dependencies {
        implementation("org.apache.commons:commons-lang3:3.13.0")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    }
}

project(":client") {
    dependencies {
        implementation(project(":core"))
        implementation("com.badlogicgames.gdx:gdx:1.12.1")
        implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.12.1")
        runtimeOnly("com.badlogicgames.gdx:gdx-platform:1.12.1:natives-desktop")
    }
}

project(":server") {
    dependencies {
        implementation(project(":core"))
    }
}
