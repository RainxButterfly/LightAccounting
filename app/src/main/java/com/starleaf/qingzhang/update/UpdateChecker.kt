/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: UpdateChecker.kt
 * 说明: 多源读取 update.json 并比较版本
 */

package com.starleaf.qingzhang.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val version: String,
    val notes: String,
    val apkUrl: String,
    val mirrors: List<String>,
    val sha256: String?
)

sealed interface UpdateCheckResult {
    data object UpToDate : UpdateCheckResult
    data class Available(val info: UpdateInfo) : UpdateCheckResult
    data class Failed(val message: String) : UpdateCheckResult
}

object UpdateChecker {

    private const val MAX_MANIFEST_BYTES = 512 * 1024
    private const val CONNECT_TIMEOUT = 5_000
    private const val READ_TIMEOUT = 8_000

    suspend fun check(context: Context): UpdateCheckResult = withContext(Dispatchers.IO) {
        val current = installedVersionName(context)
        val results = coroutineScope {
            UpdateConfig.manifestUrls.map { url ->
                async {
                    runCatching { fetchManifest(url) }
                }
            }.awaitAll()
        }

        val manifests = results.mapNotNull { it.getOrNull() }
        if (manifests.isEmpty()) {
            return@withContext UpdateCheckResult.Failed("网络不可用，或更新源暂时无法访问")
        }

        val newest = manifests
            .filter { isNewer(it.version, current) }
            .maxWithOrNull { a, b -> compareVersions(a.version, b.version) }

        if (newest != null) {
            UpdateCheckResult.Available(newest)
        } else {
            UpdateCheckResult.UpToDate
        }
    }

    internal fun installedVersionName(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "0"

    internal fun isNewer(remote: String, current: String): Boolean =
        compareVersions(remote, current) > 0

    internal fun compareVersions(left: String, right: String): Int {
        val leftParts = parseVersion(left)
        val rightParts = parseVersion(right)
        val size = maxOf(leftParts.size, rightParts.size)
        for (index in 0 until size) {
            val a = leftParts.getOrElse(index) { 0 }
            val b = rightParts.getOrElse(index) { 0 }
            if (a != b) return a.compareTo(b)
        }
        return 0
    }

    private fun parseVersion(value: String): List<Int> =
        value.trim()
            .removePrefix("v")
            .removePrefix("V")
            .split('.', '-', '+')
            .map { part -> part.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }

    private fun fetchManifest(url: String): UpdateInfo =
        parseManifest(httpGetText(url))

    internal fun parseManifest(text: String): UpdateInfo {
        val json = JSONObject(text.removePrefix("\uFEFF"))
        val version = json.optString("version").trim()
        require(version.isNotBlank()) { "更新信息缺少版本号" }
        val apk = json.optString("apk").trim()
        require(apk.startsWith("https://")) { "更新信息缺少下载地址" }
        val mirrors = json.optJSONArray("mirrors")?.let { array ->
            (0 until array.length())
                .mapNotNull { array.optString(it).trim().takeIf { item -> item.startsWith("https://") } }
        }.orEmpty()
        return UpdateInfo(
            version = version.removePrefix("v"),
            notes = json.optString("notes").trim(),
            apkUrl = apk,
            mirrors = mirrors,
            sha256 = json.optString("sha256").trim().takeIf { it.isNotBlank() }
        )
    }

    private fun httpGetText(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT
            readTimeout = READ_TIMEOUT
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "QingZhang-Android")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IllegalStateException("更新服务器返回 $code")
            val builder = StringBuilder()
            connection.inputStream.bufferedReader().use { reader ->
                val buffer = CharArray(4096)
                while (true) {
                    val read = reader.read(buffer)
                    if (read < 0) break
                    builder.append(buffer, 0, read)
                    if (builder.length > MAX_MANIFEST_BYTES) {
                        throw IllegalStateException("更新信息过大")
                    }
                }
            }
            return builder.toString()
        } finally {
            connection.disconnect()
        }
    }
}
