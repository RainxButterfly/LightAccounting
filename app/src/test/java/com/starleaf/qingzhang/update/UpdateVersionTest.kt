/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: UpdateVersionTest.kt
 * 说明: 版本比较单元测试
 */

package com.starleaf.qingzhang.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateVersionTest {

    @Test
    fun `newer patch version is detected`() {
        assertTrue(UpdateChecker.isNewer("1.0.1", "1.0.0"))
        assertTrue(UpdateChecker.isNewer("1.1.0", "1.0.9"))
        assertTrue(UpdateChecker.isNewer("2.0", "1.9.9"))
    }

    @Test
    fun `same version with prefix is not newer`() {
        assertFalse(UpdateChecker.isNewer("v1.0.0", "1.0.0"))
        assertFalse(UpdateChecker.isNewer("1.0", "1.0.0"))
        assertFalse(UpdateChecker.isNewer("1.0.0", "1.0.1"))
    }

    @Test
    fun `numeric segments compare correctly`() {
        assertEquals(1, UpdateChecker.compareVersions("1.0.10", "1.0.9"))
        assertEquals(-1, UpdateChecker.compareVersions("1.0.0", "1.0.1"))
        assertEquals(0, UpdateChecker.compareVersions("1.2.3", "v1.2.3"))
    }

    @Test
    fun `manifest is parsed with mirrors and checksum`() {
        val info = UpdateChecker.parseManifest(
            """
            {
              "version": "v1.2.0",
              "notes": "修复若干问题",
              "apk": "https://github.com/StarLeaf-Roxy/BookKeeping/releases/download/v1.2.0/qingzhang-v1.2.0.apk",
              "mirrors": [
                "https://gh-proxy.com/https://github.com/StarLeaf-Roxy/BookKeeping/releases/download/v1.2.0/qingzhang-v1.2.0.apk",
                "http://insecure.example.com/app.apk"
              ],
              "sha256": "abc123"
            }
            """.trimIndent()
        )

        assertEquals("1.2.0", info.version)
        assertEquals("修复若干问题", info.notes)
        assertEquals(1, info.mirrors.size)
        assertEquals("abc123", info.sha256)
    }
}
