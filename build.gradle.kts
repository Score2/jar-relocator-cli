plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

group = "me.scorez.utils.jarrelocatorcli"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.lucko:jar-relocator:1.7")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes(
            "Main-Class" to "me.scorez.utils.jarrelocatorcli.Main"
        )
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}