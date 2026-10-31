/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: UpdateInstaller.kt
 * 说明: 多镜像下载安装包并调起系统安装
 */

package com.starleaf.qingzhang.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

enum class InstallResult { STARTED, NEED_PERMISSION, FAILED }

object UpdateInstaller {

    private const val MIME_APK = "application/vnd.android.package-archive"
    private const val CONNECT_TIMEOUT = 8_000
    private const val READ_TIMEOUT = 30_000

    suspend fun download(
        context: Context,
        info: UpdateInfo,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "updates")
        dir.mkdirs()
        dir.listFiles()?.forEach { file -> if (file.isFile) file.delete() }

        val target = File(dir, "qingzhang-${info.version}.apk")
        val part = File(target.path + ".part")
        val candidates = buildList {
            if (info.apkUrl.startsWith("https://github.com/")) {
                UpdateConfig.downloadMirrorPrefixes.forEach { add(it + info.apkUrl) }
            }
            addAll(info.mirrors)
            add(info.apkUrl)
        }.filter { it.startsWith("https://") }.distinct()

        var lastError: Exception? = null
        for (url in candidates) {
            try {
                onProgress(-1)
                downloadTo(url, part, info.sha256, onProgress)
                if (target.exists()) target.delete()
                if (!part.renameTo(target)) {
                    part.copyTo(target, overwrite = true)
                    part.delete()
                }
                onProgress(100)
                return@withContext target
            } catch (e: CancellationException) {
                part.delete()
                throw e
            } catch (e: Exception) {
                part.delete()
                lastError = e
            }
        }
        throw IOException(lastError?.message ?: "下载更新失败，请稍后重试")
    }

    fun install(context: Context, file: File): InstallResult {
        if (!file.exists()) return InstallResult.FAILED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(intent) }
            return InstallResult.NEED_PERMISSION
        }

        return runCatching {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, MIME_APK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            InstallResult.STARTED
        }.getOrDefault(InstallResult.FAILED)
    }

    private fun downloadTo(
        url: String,
        part: File,
        expectedSha256: String?,
        onProgress: (Int) -> Unit
    ) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT
            readTimeout = READ_TIMEOUT
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "QingZhang-Android")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("下载地址返回 $code")
            val total = connection.contentLengthLong
            val digest = MessageDigest.getInstance("SHA-256")
            var readTotal = 0L
            var lastPercent = -1

            FileOutputStream(part).use { output ->
                connection.inputStream.use { input ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        digest.update(buffer, 0, read)
                        readTotal += read
                        if (total > 0) {
                            val percent = ((readTotal * 100) / total).toInt().coerceIn(0, 99)
                            if (percent != lastPercent) {
                                lastPercent = percent
                                onProgress(percent)
                            }
                        }
                    }
                }
            }

            if (total > 0 && readTotal != total) throw IOException("安装包下载不完整")
            if (!expectedSha256.isNullOrBlank()) {
                val actual = digest.digest().joinToString("") { "%02x".format(it) }
                if (!actual.equals(expectedSha256, ignoreCase = true)) {
                    throw IOException("安装包校验失败")
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
