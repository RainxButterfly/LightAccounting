/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: QingZhangRoot.kt
 * 说明: 主题分发、页面导航与首次引导
 */

package com.starleaf.qingzhang.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.starleaf.qingzhang.ui.assets.AssetsScreen
import com.starleaf.qingzhang.ui.books.BooksScreen
import com.starleaf.qingzhang.ui.categories.CategoriesScreen
import com.starleaf.qingzhang.ui.charts.ChartsScreen
import com.starleaf.qingzhang.ui.changelog.ChangelogScreen
import com.starleaf.qingzhang.ui.components.QianBottomBar
import com.starleaf.qingzhang.ui.home.HomeScreen
import com.starleaf.qingzhang.ui.importer.ImportScreen
import com.starleaf.qingzhang.ui.inbox.InboxScreen
import com.starleaf.qingzhang.ui.onboarding.OnboardingScreen
import com.starleaf.qingzhang.ui.profile.ProfileScreen
import com.starleaf.qingzhang.ui.record.RecordScreen
import com.starleaf.qingzhang.ui.theme.Paper
import com.starleaf.qingzhang.ui.theme.QingZhangTheme
import com.starleaf.qingzhang.ui.theme.ThemeMode
import com.starleaf.qingzhang.ui.update.UpdateDialog

@Composable
fun QingZhangRoot(viewModel: AppViewModel = viewModel()) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    QingZhangTheme(darkTheme = darkTheme) {
        ApplySystemBars(darkTheme)
        val showGuide by viewModel.showGuide.collectAsStateWithLifecycle()
        val updateState by viewModel.updateState.collectAsStateWithLifecycle()

        Box(Modifier.fillMaxSize()) {
            QingZhangMain(viewModel)
            if (showGuide) {
                OnboardingScreen(onFinish = viewModel::dismissGuide)
            } else {
                UpdateDialog(
                    state = updateState,
                    appVersion = viewModel.appVersion,
                    onDownload = viewModel::downloadUpdate,
                    onCancelDownload = viewModel::cancelDownload,
                    onInstall = viewModel::installUpdate,
                    onDismiss = viewModel::dismissUpdate
                )
            }
        }
    }
}

@Composable
private fun ApplySystemBars(darkTheme: Boolean) {
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()
    SideEffect {
        if (activity != null) {
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun QingZhangMain(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: "home"
    val rootRoutes = setOf("home", "charts", "assets", "profile")

    val snackbarHostState = remember { SnackbarHostState() }
    val message by viewModel.message.collectAsStateWithLifecycle()
    LaunchedEffect(message) {
        val text = message
        if (text != null) {
            snackbarHostState.showSnackbar(text)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Paper,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        bottomBar = {
            if (route in rootRoutes) {
                QianBottomBar(
                    selectedRoute = route,
                    onSelect = { target ->
                        if (target != route) {
                            navController.navigate(target) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onAdd = { navController.navigate("record?txnId=-1") }
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .statusBarsPadding()
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (route in rootRoutes) Modifier
                        else Modifier.navigationBarsPadding()
                    )
            ) {
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onEditTxn = { navController.navigate("record?txnId=$it") },
                        onOpenInbox = { navController.navigate("inbox") },
                        onManageBooks = { navController.navigate("books") }
                    )
                }
                composable("charts") { ChartsScreen(viewModel) }
                composable("assets") { AssetsScreen(viewModel) }
                composable("profile") {
                    ProfileScreen(
                        viewModel = viewModel,
                        onOpenBooks = { navController.navigate("books") },
                        onOpenCategories = { navController.navigate("categories") },
                        onOpenImport = { navController.navigate("import") },
                        onOpenInbox = { navController.navigate("inbox") },
                        onOpenChangelog = { navController.navigate("changelog") }
                    )
                }
                composable("changelog") {
                    ChangelogScreen(
                        currentVersion = viewModel.appVersion,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "record?txnId={txnId}",
                    arguments = listOf(
                        navArgument("txnId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) { entry ->
                    val id = entry.arguments?.getLong("txnId")?.takeIf { it > 0 }
                    RecordScreen(
                        viewModel = viewModel,
                        editId = id,
                        onClose = { navController.popBackStack() }
                    )
                }
                composable("books") {
                    BooksScreen(viewModel) { navController.popBackStack() }
                }
                composable("categories") {
                    CategoriesScreen(viewModel) { navController.popBackStack() }
                }
                composable("import") {
                    ImportScreen(viewModel) { navController.popBackStack() }
                }
                composable("inbox") {
                    InboxScreen(
                        viewModel = viewModel,
                        onEdit = { navController.navigate("record?txnId=$it") },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
