plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("signing")
    id("maven-publish")
    id("net.kyori.indra") version "3.1.3"
    id("net.kyori.indra.publishing") version "3.1.3"
    `java-gradle-plugin`
}

group = "dev.onelitefeather"
version = "1.5.1"

java {
    withJavadocJar()
    withSourcesJar()
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

println("Starting build of $name, Version: $version")

repositories {
    mavenCentral()
    maven(url = "https://maven.covers1624.net/")
}

sourceSets {
    create("gradle")
    main
}

configurations {
    shadow
    implementation.get().extendsFrom(shadow.get())
}

dependencies {
    val gradleImplementation = configurations.getByName("gradleImplementation")
    val gradleCompileOnly = configurations.getByName("gradleCompileOnly")

    implementation("net.covers1624:Quack:0.4.7.72")
    implementation("it.unimi.dsi:fastutil:8.3.1")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("org.apache.commons:commons-compress:1.18")
    implementation("org.tukaani:xz:1.8")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    compileOnly("org.jetbrains:annotations:23.1.0")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    gradleImplementation(sourceSets.main.get().output)
    gradleCompileOnly(gradleApi())

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks {
    test {
        useJUnitPlatform()
    }
    jar {
        manifest {
            attributes("Main-Class" to "codechicken.diffpatch.DiffPatch")
        }
    }
    shadowJar {
        minimize()
        manifest {
            attributes("Main-Class" to "codechicken.diffpatch.DiffPatch")
        }
        configurations = listOf(project.configurations.getByName("shadow"))

        from(file("LICENSE.txt"))
        from(sourceSets["gradle"].output)
        exclude("META-INF/maven/**")
        exclude("module-info.class")

        relocate("net.covers1624.quack", "codechicken.repack.net.covers1624.quack")
        relocate("it.unimi", "codechicken.repack.it.unimi")
        relocate("org.apache", "codechicken.repack.org.apache")
        relocate("org.tukaani", "codechicken.repack.org.tukaani")
        relocate("joptsimple", "codechicken.repack.joptsimple")
    }
}

indra {
    includeJavaSoftwareComponentInPublications(true)
    publishReleasesTo("eldo", "https://eldonexus.de/repository/maven-releases/")
    publishSnapshotsTo("eldo", "https://eldonexus.de/repository/maven-snapshots/")
    javaVersions {
        target(17)
        testWith(17)
    }

    github("OneLiteFeatherNET", "DiffPatch") {
        ci(true)
        publishing(false)
    }
    mitLicense()
    signWithKeyFromPrefixedProperties("onelitefeather")
    configurePublications {
        pom {
            developers {
                developer {
                    id.set("themeinerlp")
                    name.set("Phillipp Glanz")
                    email.set("p.glanz@madfix.me")
                }
                developer {
                    id.set("covers1624")
                    name.set("covers1624")
                }
                developer {
                    id.set("ChickenBones")
                    name.set("Chicken Bones")
                }
            }
        }
    }
}
signing {
    val signingKey = findProperty("onelitefeatherSigningKey") as String?
    val signingPassword = findProperty("onelitefeatherSigningPassword") as String?
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}