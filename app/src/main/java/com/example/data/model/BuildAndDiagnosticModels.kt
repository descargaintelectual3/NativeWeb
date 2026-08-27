package com.example.data.model

data class WorkflowRunInfo(
    val id: Long,
    val runNumber: Int,
    val name: String,
    val displayTitle: String,
    val status: String, // "queued", "in_progress", "completed"
    val conclusion: String, // "success", "failure", "cancelled", etc.
    val branch: String,
    val commitSha: String,
    val author: String,
    val createdAt: String,
    val updatedAt: String,
    val htmlUrl: String
)

data class BuildArtifactInfo(
    val id: Long,
    val name: String,
    val sizeInBytes: Long,
    val downloadUrl: String,
    val createdAt: String,
    val expired: Boolean,
    val source: String // "GitHub Artifact" or "GitHub Release Asset"
)

data class JobStepInfo(
    val name: String,
    val status: String,
    val conclusion: String,
    val number: Int
)

data class WorkflowJobInfo(
    val id: Long,
    val runId: Long,
    val name: String,
    val status: String,
    val conclusion: String,
    val steps: List<JobStepInfo>,
    val htmlUrl: String,
    val startedAt: String = "",
    val completedAt: String = ""
)

enum class ActivityCategory {
    ALL,
    CICD,
    ADB_HARDWARE,
    CORE_ENGINE,
    TESTING,
    FIXES
}

data class ActivityLogEntry(
    val id: String,
    val timestamp: Long,
    val dateString: String,
    val title: String,
    val description: String,
    val category: ActivityCategory,
    val agentTag: String,
    val affectedFiles: List<String>,
    val rollbackInstruction: String
)

data class TestCaseResult(
    val name: String,
    val suite: String,
    val passed: Boolean,
    val message: String,
    val durationMs: Long,
    val details: String = ""
)

data class TestSuiteSummary(
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val durationMs: Long,
    val results: List<TestCaseResult>,
    val timestamp: Long = System.currentTimeMillis()
)
