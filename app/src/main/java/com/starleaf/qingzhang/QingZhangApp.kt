/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: QingZhangApp.kt
 * 说明: 应用入口与依赖容器
 */

package com.starleaf.qingzhang

import android.app.Application
import android.content.Context
import com.starleaf.qingzhang.data.AppDatabase
import com.starleaf.qingzhang.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.build(context)
    val repository: AppRepository = AppRepository(database)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

class QingZhangApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.scope.launch {
            container.repository.seedIfNeeded()
        }
    }
}
