// https://github.com/EssentialGG/essential-gradle-toolkit
plugins {
//    id("gg.essential.loom") version "1.7.28"
    id("gg.essential.loom") version "1.9.31" apply false
    id("gg.essential.multi-version.root")


}

// https://github.com/detekt/detekt/issues/630
System.setProperty("idea.use.native.fs.for.win", "false")

preprocess {
    val fabric12105 = createNode("1.21.5-fabric", 12105, "yarn")
    val fabric12100 = createNode("1.21.0-fabric", 12100, "yarn")

    fabric12105.link(fabric12100)    // Fabric 1.21.5   ->  Fabric 1.21.0
}