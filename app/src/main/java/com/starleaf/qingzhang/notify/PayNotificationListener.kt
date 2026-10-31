/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: PayNotificationListener.kt
 * 说明: 支付通知监听服务
 */

package com.starleaf.qingzhang.notify

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.starleaf.qingzhang.QingZhangApp
import com.starleaf.qingzhang.data.AccountType
import kotlinx.coroutines.launch

class PayNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val type = when (sbn.packageName) {
            WECHAT_PACKAGE -> AccountType.WECHAT
            ALIPAY_PACKAGE -> AccountType.ALIPAY
            else -> return
        }
        if (sbn.isOngoing) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = buildString {
            append(extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty())
            append(' ')
            append(extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty())
            val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            if (lines != null) append(' ').append(lines.joinToString(" "))
        }

        val captured = PayNotificationParser.parse(sbn.packageName, title, text) ?: return
        val app = applicationContext as? QingZhangApp ?: return
        app.container.scope.launch {
            runCatching {
                app.container.repository.captureNotification(
                    type = type,
                    amount = captured.amountCents,
                    kind = captured.kind,
                    rawText = captured.text
                )
            }
        }
    }

    companion object {
        const val WECHAT_PACKAGE = "com.tencent.mm"
        const val ALIPAY_PACKAGE = "com.eg.android.AlipayGphone"
    }
}
