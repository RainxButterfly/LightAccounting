/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-11-01
 * 协议: Apache License 2.0
 *
 * 文件: ChangelogScreen.kt
 * 说明: 应用内更新日志页面
 */

package com.starleaf.qingzhang.ui.changelog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starleaf.qingzhang.ui.components.AppCard
import com.starleaf.qingzhang.ui.components.TopBar
import com.starleaf.qingzhang.ui.theme.Amber
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.AmberSoft
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft

@Composable
fun ChangelogScreen(currentVersion: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "更新日志", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(Changelog.entries, key = { it.version }) { entry ->
                AppCard {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "v${entry.version}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )
                            if (entry.version == currentVersion) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AmberSoft)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "当前版本",
                                        fontSize = 9.sp,
                                        color = AmberDeep,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Text(entry.date, fontSize = 11.sp, color = InkFaint)
                        }
                        Spacer(Modifier.height(12.dp))
                        entry.changes.forEach { change ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    Modifier
                                        .padding(top = 7.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(Amber)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = change,
                                    fontSize = 13.sp,
                                    color = InkSoft,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "更早的版本记录见仓库 CHANGELOG.md",
                    fontSize = 10.sp,
                    color = InkFaint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
