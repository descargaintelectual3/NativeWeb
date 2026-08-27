package com.example.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL

object GitHubApiAutomation {
    private const val TAG = "GitHubApiAutomation"
    private const val PREFS_NAME = "github_automation_prefs"
    private const val KEY_GH_TOKEN = "github_personal_access_token"
    private const val KEY_GH_OWNER = "github_repo_owner"
    private const val KEY_GH_REPO = "github_repo_name"

    const val DEFAULT_OWNER = "descargaintelectual3"
    const val DEFAULT_REPO = "NativeWeb"

    fun saveGitHubToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GH_TOKEN, token.trim())
            .apply()
    }

    fun getGitHubToken(context: Context): String {
        val t = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GH_TOKEN, "") ?: ""
        return t
    }

    fun saveRepoInfo(context: Context, owner: String, repo: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GH_OWNER, owner.trim())
            .putString(KEY_GH_REPO, repo.trim())
            .apply()
    }

    fun getRepoOwner(context: Context): String {
        val o = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GH_OWNER, "") ?: ""
        return if (o.isNotBlank()) o else DEFAULT_OWNER
    }

    fun getRepoName(context: Context): String {
        val r = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GH_REPO, "") ?: ""
        return if (r.isNotBlank()) r else DEFAULT_REPO
    }

    data class GitHubActionResult(
        val success: Boolean,
        val httpCode: Int,
        val message: String,
        val details: String? = null,
        val releaseId: Long? = null,
        val htmlUrl: String? = null
    )

    /**
     * Verifies the GitHub token against /user endpoint.
     */
    suspend fun verifyToken(token: String): GitHubActionResult = withContext(Dispatchers.IO) {
        if (token.isBlank()) {
            return@withContext GitHubActionResult(false, 0, "Token vacío")
        }
        try {
            val url = URL("https://api.github.com/user")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer ${token.trim()}")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "WebNativePro-Android-Client")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val code = conn.responseCode
            val responseText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""

            if (code in 200..299) {
                val json = JSONObject(responseText)
                val login = json.optString("login", "Usuario")
                val name = json.optString("name", login)
                GitHubActionResult(true, code, "Autenticado como: $name (@$login)", details = responseText)
            } else {
                GitHubActionResult(false, code, "Error de autenticación ($code): $responseText")
            }
        } catch (e: Exception) {
            GitHubActionResult(false, -1, "Excepción de red: ${e.localizedMessage}")
        }
    }

    /**
     * Triggers a GitHub Actions workflow dispatch via REST API.
     */
    suspend fun getLatestWorkflowRunStatus(context: Context, workflowFileName: String = "build-and-release.yml"): GitHubActionResult = withContext(Dispatchers.IO) {
        val token = getGitHubToken(context)
        val owner = getRepoOwner(context)
        val repo = getRepoName(context)
        if (token.isBlank()) return@withContext GitHubActionResult(false, 0, "No token")

        try {
            val endpoint = "https://api.github.com/repos/$owner/$repo/actions/workflows/$workflowFileName/runs?per_page=1"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "WebNativePro-Android-Client")

            val code = conn.responseCode
            val responseText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""

            if (code in 200..299) {
                val json = JSONObject(responseText)
                val runs = json.optJSONArray("workflow_runs")
                if (runs != null && runs.length() > 0) {
                    val latestRun = runs.getJSONObject(0)
                    val status = latestRun.optString("status", "") // "in_progress", "completed", "queued"
                    val conclusion = latestRun.optString("conclusion", "") // "success", "failure"
                    
                    if (status == "completed") {
                        if (conclusion == "success") {
                            GitHubActionResult(true, code, "completed", details = "success")
                        } else {
                            GitHubActionResult(false, code, "completed", details = conclusion)
                        }
                    } else {
                        // Still running
                        GitHubActionResult(true, code, status, details = "running")
                    }
                } else {
                    GitHubActionResult(false, code, "No se encontraron ejecuciones")
                }
            } else {
                GitHubActionResult(false, code, "Error al consultar: $responseText")
            }
        } catch (e: Exception) {
            GitHubActionResult(false, -1, "Error de red: ${e.message}")
        }
    }

    suspend fun getWorkflowRunsList(context: Context, limit: Int = 10): List<com.example.data.model.WorkflowRunInfo> = withContext(Dispatchers.IO) {
        val token = getGitHubToken(context)
        val owner = getRepoOwner(context)
        val repo = getRepoName(context)
        if (token.isBlank()) return@withContext emptyList()

        try {
            val endpoint = "https://api.github.com/repos/$owner/$repo/actions/runs?per_page=$limit"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "WebNativePro-Android-Client")

            val code = conn.responseCode
            val responseText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""

            if (code in 200..299) {
                val json = JSONObject(responseText)
                val runsArray = json.optJSONArray("workflow_runs") ?: return@withContext emptyList()
                val list = mutableListOf<com.example.data.model.WorkflowRunInfo>()

                for (i in 0 until runsArray.length()) {
                    val r = runsArray.getJSONObject(i)
                    val headCommit = r.optJSONObject("head_commit")
                    val authorObj = headCommit?.optJSONObject("author")
                    val authorName = authorObj?.optString("name") ?: r.optJSONObject("actor")?.optString("login") ?: "Dev"

                    list.add(
                        com.example.data.model.WorkflowRunInfo(
                            id = r.optLong("id"),
                            runNumber = r.optInt("run_number"),
                            name = r.optString("name", "CI/CD Build"),
                            displayTitle = r.optString("display_title", headCommit?.optString("message") ?: "Build"),
                            status = r.optString("status", "queued"),
                            conclusion = r.optString("conclusion", "pending"),
                            branch = r.optString("head_branch", "main"),
                            commitSha = r.optString("head_sha", "").take(7),
                            author = authorName,
                            createdAt = r.optString("created_at", ""),
                            updatedAt = r.optString("updated_at", ""),
                            htmlUrl = r.optString("html_url", "")
                        )
                    )
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getRecentArtifactsList(context: Context): List<com.example.data.model.BuildArtifactInfo> = withContext(Dispatchers.IO) {
        val token = getGitHubToken(context)
        val owner = getRepoOwner(context)
        val repo = getRepoName(context)
        if (token.isBlank()) return@withContext emptyList()

        val resultList = mutableListOf<com.example.data.model.BuildArtifactInfo>()

        // 1. Fetch from Actions Artifacts API
        try {
            val endpoint = "https://api.github.com/repos/$owner/$repo/actions/artifacts?per_page=10"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "WebNativePro-Android-Client")

            val code = conn.responseCode
            val responseText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""

            if (code in 200..299) {
                val json = JSONObject(responseText)
                val artifactsArray = json.optJSONArray("artifacts")
                if (artifactsArray != null) {
                    for (i in 0 until artifactsArray.length()) {
                        val a = artifactsArray.getJSONObject(i)
                        resultList.add(
                            com.example.data.model.BuildArtifactInfo(
                                id = a.optLong("id"),
                                name = a.optString("name", "artifact.zip"),
                                sizeInBytes = a.optLong("size_in_bytes", 0L),
                                downloadUrl = a.optString("archive_download_url", ""),
                                createdAt = a.optString("created_at", ""),
                                expired = a.optBoolean("expired", false),
                                source = "GitHub Actions Artifact"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Fetch from Latest Releases Assets
        try {
            val relEndpoint = "https://api.github.com/repos/$owner/$repo/releases?per_page=5"
            val relUrl = URL(relEndpoint)
            val conn = relUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "WebNativePro-Android-Client")

            val code = conn.responseCode
            val responseText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""

            if (code in 200..299) {
                val releasesArray = org.json.JSONArray(responseText)
                for (i in 0 until releasesArray.length()) {
                    val rel = releasesArray.getJSONObject(i)
                    val tagName = rel.optString("tag_name", "")
                    val assets = rel.optJSONArray("assets")
                    if (assets != null) {
                        for (j in 0 until assets.length()) {
                            val asset = assets.getJSONObject(j)
                            val name = asset.optString("name", "app.apk")
                            resultList.add(
                                com.example.data.model.BuildArtifactInfo(
                                    id = asset.optLong("id"),
                                    name = "$name ($tagName)",
                                    sizeInBytes = asset.optLong("size", 0L),
                                    downloadUrl = asset.optString("browser_download_url", ""),
                                    createdAt = asset.optString("created_at", rel.optString("created_at", "")),
                                    expired = false,
                                    source = "GitHub Release Asset"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        resultList
    }

    suspend fun triggerWorkflowDispatch(
        context: Context,
        workflowFileName: String = "build-and-release.yml",
        ref: String = "main"
    ): GitHubActionResult = withContext(Dispatchers.IO) {
        val token = getGitHubToken(context)
        val owner = getRepoOwner(context)
        val repo = getRepoName(context)

        if (token.isBlank()) {
            return@withContext GitHubActionResult(false, 0, "No hay GitHub Token configurado en el sistema.")
        }

        try {
            val endpoint = "https://api.github.com/repos/$owner/$repo/actions/workflows/$workflowFileName/dispatches"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("User-Agent", "WebNativePro-Android-Client")
            conn.doOutput = true

            val body = JSONObject().apply {
                put("ref", ref)
            }

            conn.outputStream.use { os ->
                os.write(body.toString().toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val responseText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""

            if (code == 204 || code in 200..299) {
                GitHubActionResult(
                    success = true,
                    httpCode = code,
                    message = "Workflow '$workflowFileName' disparado con éxito en GitHub Actions.",
                    details = "GitHub Actions está construyendo el APK y creando la release automáticamente."
                )
            } else {
                GitHubActionResult(
                    success = false,
                    httpCode = code,
                    message = "Error al iniciar workflow ($code): $responseText",
                    details = responseText
                )
            }
        } catch (e: Exception) {
            GitHubActionResult(false, -1, "Error: ${e.localizedMessage}")
        }
    }

    /**
     * Creates a GitHub Release directly via REST API.
     */
    suspend fun createRelease(
        context: Context,
        tagName: String = "v4.4.8",
        releaseName: String = "WebNative Pro v4.4.8",
        bodyContent: String = "Lanzamiento automático WebNative Pro v4.4.8 con soporte multicanal y motor Turbo.",
        targetCommitish: String = "main",
        draft: Boolean = false,
        prerelease: Boolean = false
    ): GitHubActionResult = withContext(Dispatchers.IO) {
        val token = getGitHubToken(context)
        val owner = getRepoOwner(context)
        val repo = getRepoName(context)

        if (token.isBlank()) {
            return@withContext GitHubActionResult(false, 0, "No hay GitHub Token configurado.")
        }

        try {
            val endpoint = "https://api.github.com/repos/$owner/$repo/releases"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("User-Agent", "WebNativePro-Android-Client")
            conn.doOutput = true

            val jsonBody = JSONObject().apply {
                put("tag_name", tagName)
                put("target_commitish", targetCommitish)
                put("name", releaseName)
                put("body", bodyContent)
                put("draft", draft)
                put("prerelease", prerelease)
                put("generate_release_notes", true)
            }

            conn.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val responseText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""

            if (code in 200..299) {
                val respJson = JSONObject(responseText)
                val relId = respJson.optLong("id")
                val htmlUrl = respJson.optString("html_url")
                GitHubActionResult(
                    success = true,
                    httpCode = code,
                    message = "Release '$tagName' creada exitosamente en GitHub.",
                    releaseId = relId,
                    htmlUrl = htmlUrl,
                    details = responseText
                )
            } else {
                GitHubActionResult(
                    success = false,
                    httpCode = code,
                    message = "No se pudo crear la release ($code): $responseText",
                    details = responseText
                )
            }
        } catch (e: Exception) {
            GitHubActionResult(false, -1, "Excepción al crear release: ${e.localizedMessage}")
        }
    }

    /**
     * Uploads an APK file directly as an asset to a GitHub Release.
     */
    suspend fun uploadApkAsset(
        context: Context,
        releaseId: Long,
        apkFile: File,
        assetName: String = "app-debug.apk"
    ): GitHubActionResult = withContext(Dispatchers.IO) {
        val token = getGitHubToken(context)
        val owner = getRepoOwner(context)
        val repo = getRepoName(context)

        if (token.isBlank()) {
            return@withContext GitHubActionResult(false, 0, "No hay GitHub Token configurado.")
        }
        if (!apkFile.exists() || apkFile.length() == 0L) {
            return@withContext GitHubActionResult(false, 0, "El archivo APK local no existe o está vacío.")
        }

        try {
            val uploadEndpoint = "https://uploads.github.com/repos/$owner/$repo/releases/$releaseId/assets?name=$assetName"
            val url = URL(uploadEndpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("Content-Type", "application/vnd.android.package-archive")
            conn.setRequestProperty("User-Agent", "WebNativePro-Android-Client")
            conn.setRequestProperty("Content-Length", apkFile.length().toString())
            conn.doOutput = true
            conn.setFixedLengthStreamingMode(apkFile.length())

            FileInputStream(apkFile).use { fis ->
                conn.outputStream.use { os ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (fis.read(buffer).also { read = it } != -1) {
                        os.write(buffer, 0, read)
                    }
                    os.flush()
                }
            }

            val code = conn.responseCode
            val responseText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""

            if (code in 200..299) {
                val respJson = JSONObject(responseText)
                val downloadUrl = respJson.optString("browser_download_url")
                GitHubActionResult(
                    success = true,
                    httpCode = code,
                    message = "APK '$assetName' subido exitosamente a la Release.",
                    htmlUrl = downloadUrl,
                    details = responseText
                )
            } else {
                GitHubActionResult(
                    success = false,
                    httpCode = code,
                    message = "Fallo al subir APK ($code): $responseText",
                    details = responseText
                )
            }
        } catch (e: Exception) {
            GitHubActionResult(false, -1, "Error al subir asset APK: ${e.localizedMessage}")
        }
    }

    /**
     * Fetches jobs and steps for a specific workflow run.
     */
    suspend fun getWorkflowRunJobs(context: Context, runId: Long): List<com.example.data.model.WorkflowJobInfo> = withContext(Dispatchers.IO) {
        val token = getGitHubToken(context)
        val owner = getRepoOwner(context)
        val repo = getRepoName(context)
        if (token.isBlank() || runId <= 0) return@withContext emptyList()

        try {
            val endpoint = "https://api.github.com/repos/$owner/$repo/actions/runs/$runId/jobs"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "WebNativePro-Android-Client")

            val code = conn.responseCode
            val responseText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""

            if (code in 200..299) {
                val json = JSONObject(responseText)
                val jobsArray = json.optJSONArray("jobs") ?: return@withContext emptyList()
                val list = mutableListOf<com.example.data.model.WorkflowJobInfo>()

                for (i in 0 until jobsArray.length()) {
                    val j = jobsArray.getJSONObject(i)
                    val stepsArray = j.optJSONArray("steps")
                    val stepsList = mutableListOf<com.example.data.model.JobStepInfo>()
                    if (stepsArray != null) {
                        for (k in 0 until stepsArray.length()) {
                            val st = stepsArray.getJSONObject(k)
                            stepsList.add(
                                com.example.data.model.JobStepInfo(
                                    name = st.optString("name", "Step"),
                                    status = st.optString("status", "queued"),
                                    conclusion = st.optString("conclusion", "pending"),
                                    number = st.optInt("number", k + 1)
                                )
                            )
                        }
                    }

                    list.add(
                        com.example.data.model.WorkflowJobInfo(
                            id = j.optLong("id"),
                            runId = runId,
                            name = j.optString("name", "Job"),
                            status = j.optString("status", "queued"),
                            conclusion = j.optString("conclusion", "pending"),
                            steps = stepsList,
                            htmlUrl = j.optString("html_url", ""),
                            startedAt = j.optString("started_at", ""),
                            completedAt = j.optString("completed_at", "")
                        )
                    )
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Fetches raw logs for a specific job.
     */
    suspend fun getJobLogsText(context: Context, jobId: Long): String = withContext(Dispatchers.IO) {
        val token = getGitHubToken(context)
        val owner = getRepoOwner(context)
        val repo = getRepoName(context)
        if (token.isBlank() || jobId <= 0) return@withContext "Token no configurado o ID de Job inválido."

        try {
            val endpoint = "https://api.github.com/repos/$owner/$repo/actions/jobs/$jobId/logs"
            var currentUrl = URL(endpoint)
            var redirects = 0
            var conn: HttpURLConnection

            while (redirects < 4) {
                conn = currentUrl.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Accept", "application/vnd.github+json")
                conn.setRequestProperty("User-Agent", "WebNativePro-Android-Client")

                val code = conn.responseCode
                if (code in 300..399) {
                    val location = conn.getHeaderField("Location")
                    if (location != null) {
                        currentUrl = URL(location)
                        redirects++
                        continue
                    }
                }

                return@withContext if (code in 200..299) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    "No se pudieron descargar los logs del job (HTTP $code): $err"
                }
            }
            "Demasiadas redirecciones al descargar logs."
        } catch (e: Exception) {
            "Error al obtener logs: ${e.localizedMessage}"
        }
    }
}
