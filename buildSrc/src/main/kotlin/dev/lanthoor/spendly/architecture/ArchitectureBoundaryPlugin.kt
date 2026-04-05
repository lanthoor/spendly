package dev.lanthoor.spendly.architecture

import org.gradle.api.Plugin
import org.gradle.api.Project

class ArchitectureBoundaryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project != project.rootProject) {
            return
        }

        val baselineLocation = project.layout.projectDirectory.file("config/architecture/boundary-baseline.txt")

        project.tasks.register(
            "generateArchitectureBoundaryBaseline",
            GenerateArchitectureBoundaryBaselineTask::class.java,
        ) {
            group = "verification"
            description = "Generates architecture boundary baseline entries from current source state"

            sourceRoot.set("app/src/main/java")
            packagePrefix.set("dev.lanthoor.spendly")
            featureRootPackage.set("dev.lanthoor.spendly.ui.screens")
            baselineFile.set(baselineLocation)
        }

        val checkTask = project.tasks.register(
            "checkArchitectureBoundaries",
            CheckArchitectureBoundariesTask::class.java,
        ) {
            group = "verification"
            description = "Checks architecture boundaries against baseline and fails on new violations"

            sourceRoot.set("app/src/main/java")
            packagePrefix.set("dev.lanthoor.spendly")
            featureRootPackage.set("dev.lanthoor.spendly.ui.screens")
            baselineFile.set(baselineLocation)
        }

        project.tasks.matching { it.name == "check" }.configureEach {
            dependsOn(checkTask)
        }
    }
}
