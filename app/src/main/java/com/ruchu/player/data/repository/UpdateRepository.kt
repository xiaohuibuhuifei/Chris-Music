package com.ruchu.player.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.ruchu.player.data.model.GitHubRelease
import com.ruchu.player.data.model.UpdateInfo
import com.ruchu.player.data.model.UpdateState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class UpdateRepository(private val context: Context) {

    companion object {
        private const val GITHUB_OWNER = "xiaohuibuhuifei"
        private const val GITHUB_REPO = "Chris-Music"
        private const val API_URL =
            "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

        // GitHub 下载镜像列表，依次尝试
        private val MIRRORS = listOf(
            "https://ghps.cc/",
            "https://gh.idayer.com/",
            "https://gh.con.sh/",
            "https://ghproxy.cn/",
            "" // 兜底：直连 GitHub
        )
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    suspend fun checkForUpdate(currentVersionCode: Int) {
        _updateState.value = UpdateState.Checking
        try {
            val release = fetchLatestRelease()
            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }

            if (apkAsset == null) {
                _updateState.value = UpdateState.Idle
                return
            }

            val remoteVersionCode = parseVersionCode(release.tag_name)
            if (remoteVersionCode > currentVersionCode) {
                _updateState.value = UpdateState.Available(
                    info = UpdateInfo(
                        versionName = release.tag_name,
                        versionCode = remoteVersionCode,
                        releaseNotes = release.body,
                        downloadUrl = apkAsset.downloadUrl,
                        fileSize = apkAsset.size
                    )
                )
            } else {
                _updateState.value = UpdateState.Idle
            }
        } catch (e: NoSuchElementException) {
            _updateState.value = UpdateState.Idle
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(e.message ?: "检查更新失败")
        }
    }

    suspend fun downloadAndInstall(updateInfo: UpdateInfo) {
        _updateState.value = UpdateState.Downloading(0f)
        try {
            val apkFile = downloadApkWithMirrors(updateInfo)
            _updateState.value = UpdateState.ReadyToInstall
            installApk(apkFile)
            _updateState.value = UpdateState.Idle
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(e.message ?: "下载失败")
        }
    }

    fun dismiss() {
        _updateState.value = UpdateState.Idle
    }

    private suspend fun fetchLatestRelease(): GitHubRelease = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(API_URL)
            .header("Accept", "application/vnd.github+json")
            .build()

        val response = client.newCall(request).execute()
        if (response.code == 404) {
            throw NoSuchElementException()
        }
        if (!response.isSuccessful) {
            throw Exception("GitHub API 返回 ${response.code}")
        }

        val body = response.body?.string()
            ?: throw Exception("空响应")

        gson.fromJson(body, GitHubRelease::class.java)
    }

    private suspend fun downloadApkWithMirrors(updateInfo: UpdateInfo): File = withContext(Dispatchers.IO) {
        val baseUrl = updateInfo.downloadUrl
        val urls = MIRRORS.map { mirror -> "${mirror}${baseUrl}" }

        var lastError: Exception? = null
        for (url in urls) {
            try {
                return@withContext downloadApk(url)
            } catch (e: Exception) {
                lastError = e
                continue
            }
        }
        throw lastError ?: Exception("下载失败")
    }

    private suspend fun downloadApk(url: String): File = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) updatesDir.mkdirs()
        updatesDir.listFiles()?.forEach { it.delete() }

        val apkFile = File(updatesDir, "update.apk")
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("下载失败: ${response.code}")

        val body = response.body ?: throw Exception("下载内容为空")
        val contentLength = body.contentLength()

        body.byteStream().use { input ->
            apkFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Long = 0
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    bytesRead += read
                    if (contentLength > 0) {
                        val progress = bytesRead.toFloat() / contentLength
                        _updateState.value = UpdateState.Downloading(progress)
                    }
                }
                output.flush()
            }
        }

        apkFile
    }

    private fun installApk(apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun parseVersionCode(tagName: String): Int {
        val digits = tagName.filter { it.isDigit() }
        return digits.toIntOrNull() ?: 0
    }
}
