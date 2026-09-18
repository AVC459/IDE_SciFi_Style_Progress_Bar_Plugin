plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.scifi.progress"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

val localIde = (findProperty("ideaLocalPath") as String?)?.takeIf { it.isNotBlank() }

intellij {
    if (localIde != null) {
        localPath.set(localIde)
    } else {
        version.set("2023.1.5")
        type.set("IC")
    }
    plugins.set(emptyList())
    updateSinceUntilBuild.set(false)
    instrumentCode.set(false)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(11)
}

tasks.withType<Test> {
    useJUnit()
}
