package com.ruchu.player.data.model

import com.google.gson.annotations.SerializedName

data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val assets: List<GitHubAsset>
)

data class GitHubAsset(
    val name: String,
    val size: Long,
    @SerializedName("browser_download_url")
    val downloadUrl: String,
    @SerializedName("content_type")
    val contentType: String
)

data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileSize: Long
)

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    data class Downloading(val progress: Float) : UpdateState()
    data object ReadyToInstall : UpdateState()
    data class Error(val message: String) : UpdateState()
}
