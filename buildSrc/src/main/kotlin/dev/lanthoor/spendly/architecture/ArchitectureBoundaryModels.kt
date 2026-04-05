package dev.lanthoor.spendly.architecture

data class ArchitectureViolation(
    val ruleId: String,
    val filePath: String,
    val lineNumber: Int,
    val message: String,
) {
    fun baselineEntry(): String = "$ruleId|$filePath|$lineNumber|$message"
}

data class ArchitectureRuleConfig(
    val sourceRoot: String,
    val baselineFile: String,
    val packagePrefix: String,
    val featureRootPackage: String,
)
