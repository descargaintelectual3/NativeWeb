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

    const val DEFAULT_OWNER = "PabloArboledai"
    const val DEFAULT_REPO = "Civer-Cloud-Manager-IDE"

    fun saveGitHubToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GH_TOKEN, token.trim())
            .apply()
    }

    fun getGitHubToken(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GH_TOKEN, "") ?: ""
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
}
