package dev.lanthoor.spendly.architecture

import java.io.File

class ArchitectureBoundaryScanner(
    private val config: ArchitectureRuleConfig,
) {
    private val importRegex = Regex("""^\s*import\s+([a-zA-Z0-9_.]+)""")
    private val packageRegex = Regex("""^\s*package\s+([a-zA-Z0-9_.]+)""")
    private val dataClassRegex = Regex("""\bdata\s+class\s+[A-Z][A-Za-z0-9_]*""")
    private val enumClassRegex = Regex("""\benum\s+class\s+[A-Z][A-Za-z0-9_]*""")
    private val featureSegmentRegex = Regex("""\.ui\.screens\.([a-z][a-z0-9_]*)\.""")
    private val legacyUtilsOwnershipImports = setOf(
        "${config.packagePrefix}.utils.IncomeSource",
        "${config.packagePrefix}.utils.RecurringFrequency",
        "${config.packagePrefix}.utils.TransactionType",
        "${config.packagePrefix}.utils.AccountType",
        "${config.packagePrefix}.utils.AppTheme",
        "${config.packagePrefix}.utils.AppLanguage",
        "${config.packagePrefix}.utils.YearType",
        "${config.packagePrefix}.utils.TimePeriod",
        "${config.packagePrefix}.utils.LockTimeout",
        "${config.packagePrefix}.utils.toDisplayString",
        "${config.packagePrefix}.utils.toDisplayName",
        "${config.packagePrefix}.utils.getDefaultIcon",
        "${config.packagePrefix}.utils.getDisplayRange",
        "${config.packagePrefix}.utils.getDateRange",
        "${config.packagePrefix}.utils.displayNameRes",
    )

    fun scan(baseDir: File): List<ArchitectureViolation> {
        val sourceRootDir = baseDir.resolve(config.sourceRoot)
        if (!sourceRootDir.exists()) return emptyList()

        return sourceRootDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { scanFile(baseDir, it).asSequence() }
            .toList()
            .sortedWith(compareBy({ it.ruleId }, { it.filePath }, { it.lineNumber }, { it.message }))
    }

    private fun scanFile(baseDir: File, file: File): List<ArchitectureViolation> {
        val relativePath = baseDir.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/')
        val lines = file.readLines()
        val packageName = lines
            .firstNotNullOfOrNull { line -> packageRegex.find(line)?.groupValues?.get(1) }
            .orEmpty()

        val violations = mutableListOf<ArchitectureViolation>()
        val currentFeature = extractFeatureFromPackage(packageName)

        lines.forEachIndexed { index, line ->
            val lineNumber = index + 1
            val importTarget = importRegex.find(line)?.groupValues?.get(1)
            if (importTarget != null) {
                if (isDomainPackage(packageName) && importTarget.startsWith("${config.packagePrefix}.ui.")) {
                    violations += ArchitectureViolation(
                        ruleId = "DOMAIN_TO_UI_IMPORT",
                        filePath = relativePath,
                        lineNumber = lineNumber,
                        message = "Domain package must not import UI package: $importTarget"
                    )
                }

                val importedFeature = extractFeatureFromImport(importTarget)
                if (currentFeature != null && importedFeature != null && currentFeature != importedFeature) {
                    violations += ArchitectureViolation(
                        ruleId = "CROSS_FEATURE_INTERNAL_IMPORT",
                        filePath = relativePath,
                        lineNumber = lineNumber,
                        message = "Feature '$currentFeature' must not import feature '$importedFeature' internals: $importTarget"
                    )
                }

                if (importTarget in legacyUtilsOwnershipImports) {
                    violations += ArchitectureViolation(
                        ruleId = "LEGACY_UTILS_OWNERSHIP_IMPORT",
                        filePath = relativePath,
                        lineNumber = lineNumber,
                        message = "Import moved ownership type from explicit core package instead of utils: $importTarget"
                    )
                }
            }

            if (isUtilsPath(relativePath) && (dataClassRegex.containsMatchIn(line) || enumClassRegex.containsMatchIn(line))) {
                violations += ArchitectureViolation(
                    ruleId = "UTILS_BUSINESS_DECLARATION",
                    filePath = relativePath,
                    lineNumber = lineNumber,
                    message = "Do not add new data or enum declarations under utils without explicit ownership"
                )
            }
        }

        return violations
    }

    private fun extractFeatureFromPackage(packageName: String): String? {
        val prefix = "${config.featureRootPackage}."
        if (!packageName.startsWith(prefix)) return null
        val rest = packageName.removePrefix(prefix)
        return rest.substringBefore('.')
    }

    private fun extractFeatureFromImport(importTarget: String): String? {
        val match = featureSegmentRegex.find(importTarget) ?: return null
        return match.groupValues[1]
    }

    private fun isDomainPackage(packageName: String): Boolean =
        packageName.contains(".domain.") || packageName.endsWith(".domain")

    private fun isUtilsPath(relativePath: String): Boolean =
        relativePath.contains("/utils/") || relativePath.endsWith("/utils")
}
