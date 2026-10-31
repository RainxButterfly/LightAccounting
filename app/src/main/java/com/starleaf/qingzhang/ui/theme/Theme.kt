/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: Theme.kt
 * 说明: 主题配色、深浅色方案与排版
 */

package com.starleaf.qingzhang.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppPalette(
    val amber: Color,
    val amberDeep: Color,
    val amberSoft: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val paper: Color,
    val card: Color,
    val line: Color,
    val income: Color,
    val danger: Color
)

val CategoryColors = listOf(
    Color(0xFFF0786A),
    Color(0xFFF2B33D),
    Color(0xFF6C9FF2),
    Color(0xFF4FB286),
    Color(0xFF9C88F0),
    Color(0xFFEE8FB7),
    Color(0xFF48BCC0),
    Color(0xFFA78BFA),
    Color(0xFFF0A05A),
    Color(0xFFE56B8D),
    Color(0xFF7CB3A5),
    Color(0xFF5B8DEE),
    Color(0xFFD9A441),
    Color(0xFF8C9BB0),
    Color(0xFF9AA0A6),
    Color(0xFF6BC5A8),
    Color(0xFFE3A94F),
    Color(0xFF6D9EEB),
    Color(0xFF58B8A4),
    Color(0xFFD97A6C),
    Color(0xFF8E8CE8),
    Color(0xFFA0A4A8)
)

fun categoryColor(index: Int): Color =
    CategoryColors[Math.floorMod(index, CategoryColors.size)]

private val LightPalette = AppPalette(
    amber = Color(0xFFF2B33D),
    amberDeep = Color(0xFFDE9720),
    amberSoft = Color(0xFFFFF3DC),
    ink = Color(0xFF202327),
    inkSoft = Color(0xFF7C828B),
    inkFaint = Color(0xFFAAAFB6),
    paper = Color(0xFFF7F5F1),
    card = Color(0xFFFFFFFF),
    line = Color(0xFFEDEAE4),
    income = Color(0xFF2E9E68),
    danger = Color(0xFFDD5E5E)
)

private val DarkPalette = AppPalette(
    amber = Color(0xFFF2B33D),
    amberDeep = Color(0xFFF0B34A),
    amberSoft = Color(0xFF33280F),
    ink = Color(0xFFE9E8E6),
    inkSoft = Color(0xFFA3A6AB),
    inkFaint = Color(0xFF73777D),
    paper = Color(0xFF121212),
    card = Color(0xFF1C1C1C),
    line = Color(0xFF2C2C2C),
    income = Color(0xFF4CC38A),
    danger = Color(0xFFF27A7A)
)

private val LocalAppPalette = staticCompositionLocalOf { LightPalette }

val Amber: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.amber

val AmberDeep: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.amberDeep

val AmberSoft: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.amberSoft

val Ink: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.ink

val InkSoft: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.inkSoft

val InkFaint: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.inkFaint

val Paper: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.paper

val Card: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.card

val Line: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.line

val Income: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.income

val Danger: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current.danger

private val LightScheme = lightColorScheme(
    primary = LightPalette.amber,
    onPrimary = LightPalette.ink,
    primaryContainer = LightPalette.amberSoft,
    onPrimaryContainer = LightPalette.ink,
    secondary = LightPalette.income,
    onSecondary = Color.White,
    background = LightPalette.paper,
    onBackground = LightPalette.ink,
    surface = LightPalette.card,
    onSurface = LightPalette.ink,
    surfaceVariant = LightPalette.paper,
    onSurfaceVariant = LightPalette.inkSoft,
    outline = LightPalette.line,
    outlineVariant = LightPalette.line,
    error = LightPalette.danger,
    onError = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = DarkPalette.amber,
    onPrimary = Color(0xFF2A1E08),
    primaryContainer = DarkPalette.amberSoft,
    onPrimaryContainer = DarkPalette.ink,
    secondary = DarkPalette.income,
    onSecondary = Color(0xFF06130C),
    background = DarkPalette.paper,
    onBackground = DarkPalette.ink,
    surface = DarkPalette.card,
    onSurface = DarkPalette.ink,
    surfaceVariant = DarkPalette.paper,
    onSurfaceVariant = DarkPalette.inkSoft,
    outline = DarkPalette.line,
    outlineVariant = DarkPalette.line,
    error = DarkPalette.danger,
    onError = Color(0xFF2A0B0B)
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 19.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
    )
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun QingZhangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
