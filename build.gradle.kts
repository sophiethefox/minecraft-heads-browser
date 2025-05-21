plugins {
    id("gg.essential.defaults")
    id("gg.essential.multi-version")
}

fun Project.dependencyVersion(name: String, friendlyName: String = name, defaultValue: String? = null): String {
    return this.findProperty("dependency.$name.version") as? String
        ?: defaultValue
        ?: error("No $friendlyName version defined for ${platform.mcVersionStr} (${platform.loaderStr})")
}

version = "1.0.0"
group = "cc.sophiethefox"
base.archivesName = "minecraft-heads-browser"

repositories {
	maven("https://maven.wispforest.io/releases/")
}

dependencies {
    val fabricApiVersion = dependencyVersion("fabric-api", "Fabric API")
	modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    val owoVersion = "0.12.21+1.21.5"

    modImplementation("io.wispforest:owo-lib:$owoVersion")
    annotationProcessor("io.wispforest:owo-lib:$owoVersion")
    include("io.wispforest:owo-sentinel:$owoVersion")

}


java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    jar {
        // This gradle script is evaluated for each `./version/{id}`, in order to get the LICENSE file, we need to
        // resolve it from the root project, not the version's root.
        from(rootProject.file("LICENSE")) {
            rename { "minecraft-heads-browser_${it}" }
        }
    }
}
