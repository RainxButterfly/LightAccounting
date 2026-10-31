/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: UpdateConfig.kt
 * 说明: 更新仓库地址与国内镜像源配置
 */

package com.starleaf.qingzhang.update

object UpdateConfig {

    const val OWNER = "RainxButterfly"
    const val REPO = "LightAccounting"
    const val BRANCH = "main"

    val manifestUrls: List<String> = listOf(
        "https://cdn.jsdelivr.net/gh/$OWNER/$REPO@$BRANCH/update.json",
        "https://fastly.jsdelivr.net/gh/$OWNER/$REPO@$BRANCH/update.json",
        "https://gcore.jsdelivr.net/gh/$OWNER/$REPO@$BRANCH/update.json",
        "https://raw.gitmirror.com/$OWNER/$REPO/$BRANCH/update.json",
        "https://gh-proxy.com/https://raw.githubusercontent.com/$OWNER/$REPO/$BRANCH/update.json",
        "https://raw.githubusercontent.com/$OWNER/$REPO/$BRANCH/update.json"
    )

    val downloadMirrorPrefixes: List<String> = listOf(
        "https://gh-proxy.com/",
        "https://ghfast.top/",
        "https://github.moeyy.xyz/",
        "https://ghproxy.net/"
    )
}
