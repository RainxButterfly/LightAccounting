/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: ChartsScreen.kt
 * 说明: 图表统计页面
 */

package com.starleaf.qingzhang.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starleaf.qingzhang.data.TxnKind
import com.starleaf.qingzhang.ui.AppViewModel
import com.starleaf.qingzhang.ui.CategorySlice
import com.starleaf.qingzhang.ui.MonthBar
import com.starleaf.qingzhang.ui.components.AppCard
import com.starleaf.qingzhang.ui.components.EmptyState
import com.starleaf.qingzhang.ui.components.MonthSwitcher
import com.starleaf.qingzhang.ui.theme.Amber
import com.starleaf.qingzhang.ui.theme.AmberSoft
import com.starleaf.qingzhang.ui.theme.Card
import com.starleaf.qingzhang.ui.theme.Income
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft
import com.starleaf.qingzhang.ui.theme.Line
import com.starleaf.qingzhang.ui.theme.categoryColor
import com.starleaf.qingzhang.util.Money
import java.time.YearMonth

@Composable
fun ChartsScreen(viewModel: AppViewModel) {
    val month by viewModel.month.collectAsStateWithLifecycle()
    val canNext by viewModel.canGoNextMonth.collectAsStateWithLifecycle()
    val chart by viewModel.chartData.collectAsStateWithLifecycle()
    val sixMonths by viewModel.sixMonths.collectAsStateWithLifecycle()

    var kind by remember { mutableStateOf(TxnKind.EXPENSE) }
    val total = if (kind == TxnKind.EXPENSE) chart.expenseTotal else chart.incomeTotal
    val slices = if (kind == TxnKind.EXPENSE) chart.expense else chart.income
    val label = if (kind == TxnKind.EXPENSE) "支出" else "收入"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp)
    ) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "图表",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        modifier = Modifier.weight(1f)
                    )
                    MonthSwitcher(
                        month = month,
                        onPrevious = viewModel::previousMonth,
                        onNext = viewModel::nextMonth,
                        canNext = canNext
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Card)
                        .padding(4.dp)
                ) {
                    Segment("支出", kind == TxnKind.EXPENSE, Modifier.weight(1f)) {
                        kind = TxnKind.EXPENSE
                    }
                    Segment("收入", kind == TxnKind.INCOME, Modifier.weight(1f)) {
                        kind = TxnKind.INCOME
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(14.dp))
            AppCard(Modifier.padding(horizontal = 20.dp)) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        DonutChart(slices = slices, modifier = Modifier.size(196.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label + "合计", fontSize = 11.sp, color = InkFaint)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "¥" + Money.format(total),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }
                    }
                    if (slices.isEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("这个月还没有$label", fontSize = 12.sp, color = InkFaint)
                    }
                }
            }
        }

        if (slices.isNotEmpty()) {
            item {
                Spacer(Modifier.height(14.dp))
                AppCard(Modifier.padding(horizontal = 20.dp)) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                        slices.forEachIndexed { index, slice ->
                            SliceRow(slice = slice, total = total)
                            if (index != slices.lastIndex) {
                                Spacer(Modifier.height(14.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(14.dp))
            AppCard(Modifier.padding(horizontal = 20.dp)) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "近 6 个月",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink
                        )
                        Spacer(Modifier.weight(1f))
                        LegendDot(Amber, "支出")
                        Spacer(Modifier.width(12.dp))
                        LegendDot(Income, "收入")
                    }
                    Spacer(Modifier.height(18.dp))
                    MonthBars(sixMonths)
                }
            }
        }

        if (slices.isEmpty()) {
            item {
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun Segment(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) AmberSoft else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Ink else InkSoft
        )
    }
}

@Composable
private fun DonutChart(slices: List<CategorySlice>, modifier: Modifier = Modifier) {
    val ringBackground = Line
    Canvas(modifier = modifier) {
        val stroke = 22.dp.toPx()
        val diameter = size.minDimension - stroke
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        drawArc(
            color = ringBackground,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        var start = -90f
        slices.forEach { slice ->
            val sweep = slice.fraction * 360f
            if (sweep > 0.4f) {
                drawArc(
                    color = categoryColor(slice.colorIndex),
                    startAngle = start + 1.4f,
                    sweepAngle = (sweep - 2.8f).coerceAtLeast(0.8f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            start += sweep
        }
    }
}

@Composable
private fun SliceRow(slice: CategorySlice, total: Long) {
    val fraction = if (total > 0) slice.amount.toFloat() / total else 0f
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(categoryColor(slice.colorIndex))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = slice.category?.name ?: "未分类",
                fontSize = 13.sp,
                color = Ink,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${(fraction * 100).toInt()}%",
                fontSize = 11.sp,
                color = InkFaint,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = "¥" + Money.format(slice.amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Ink
            )
        }
        Spacer(Modifier.height(7.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Line)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor(slice.colorIndex))
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 10.sp, color = InkFaint)
    }
}

@Composable
private fun MonthBars(bars: List<MonthBar>) {
    val maxValue = (bars.maxOfOrNull { maxOf(it.expense, it.income) } ?: 0L).coerceAtLeast(1L)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        bars.forEach { bar ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier.height(112.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Bar(
                        heightFraction = bar.expense.toFloat() / maxValue,
                        color = Amber
                    )
                    Bar(
                        heightFraction = bar.income.toFloat() / maxValue,
                        color = Income
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(bar.label, fontSize = 10.sp, color = InkFaint)
            }
        }
    }
}

@Composable
private fun Bar(heightFraction: Float, color: Color) {
    val height = (4f + heightFraction.coerceIn(0f, 1f) * 104f).dp
    Box(
        Modifier
            .width(9.dp)
            .height(height)
            .clip(RoundedCornerShape(5.dp))
            .background(if (heightFraction <= 0f) Line else color.copy(alpha = 0.85f))
    )
}
