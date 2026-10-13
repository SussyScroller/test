group = "app.sussyscroller"

patches {
    about {
        name = "RedGifs Audio Patches"
        description = "Standalone \"Enable RedGifs audio\" patch for Reddit, ported from DarkGooner/morphe-patches. For use alongside the official Morphe patches."
        source = "git@github.com:SussyScroller/test.git"
        author = "DarkGooner (patch), SussyScroller (standalone port)"
        contact = "na"
        website = "https://github.com/SussyScroller/test"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("app.sussyscroller.util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
