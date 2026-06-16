package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.model.ToolRegistry
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ToolBackground
import com.example.viewmodel.ToolboxViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: ToolboxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(2500) // Beautiful 2.5s starting splash screen
                    showSplash = false
                }

                Box(modifier = Modifier.fillMaxSize().background(ToolBackground)) {
                    AnimatedContent(
                        targetState = showSplash,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(600)) togetherWith
                                    fadeOut(animationSpec = tween(600))
                        },
                        label = "SplashTransition"
                    ) { splashActive ->
                        if (splashActive) {
                            SplashIntroScreen()
                        } else {
                            MainNavigationShell(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // Collect toast alerts from ViewModel
        lifecycleScope.launch {
            viewModel.toastMessage.collectLatest { msg ->
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun SplashIntroScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlow")
    val scaleMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Glowing Logo Circle
            Box(
                modifier = Modifier
                    .size(92.dp * scaleMultiplier)
                    .background(Color(0x19FF2B2B), RoundedCornerShape(50))
                    .border(2.dp, Color(0xFFFF2B2B), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = Color(0xFFFF2B2B),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "D-TOOLBOX",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )

            Text(
                "One Place, Unlimited Tools.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF2B2B),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFFFF2B2B),
                strokeWidth = 2.dp
            )

            Text(
                "Menginisialisasi Swiss-Army Engine...",
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                "wm : Dapp.",
                fontSize = 10.sp,
                color = Color(0xFF555555),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationShell(viewModel: ToolboxViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedToolId by viewModel.selectedToolId.collectAsState()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val context = LocalContext.current

    Scaffold(
        topBar = {
            if (currentScreen != "detail" && currentScreen != "login") {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                "D-TOOLBOX",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                "v1.0.0",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("wm : Dapp.", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen != "detail" && currentScreen != "login") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val navigationItems = listOf(
                        Triple("home", Icons.Default.Dashboard, "Home"),
                        Triple("all_tools", Icons.Default.GridView, "All Tools"),
                        Triple("about", Icons.Default.Info, "About"),
                        Triple("support", Icons.Default.ContactSupport, "Support"),
                        Triple("settings", Icons.Default.Settings, "Settings")
                    )

                    navigationItems.forEach { (route, icon, label) ->
                        NavigationBarItem(
                            selected = currentScreen == route,
                            onClick = { viewModel.navigateTo(route) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.testTag("nav_item_$route")
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        // Slide or Fade screens depending on type
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally(animationSpec = tween(300)) { width -> if (targetState == "detail") width else -width } + fadeIn(animationSpec = tween(300)) togetherWith
                            slideOutHorizontally(animationSpec = tween(300)) { width -> if (initialState == "detail") width else -width } + fadeOut(animationSpec = tween(300))
                },
                label = "ScreenTransitions"
            ) { screen ->
                when (screen) {
                    "login" -> LoginScreenView(viewModel = viewModel)
                    "home" -> MainDashboardView(viewModel = viewModel)
                    "all_tools" -> AllToolsListView(viewModel = viewModel)
                    "about" -> AboutScreenView(viewModel = viewModel)
                    "support" -> SupportScreenView(viewModel = viewModel)
                    "settings" -> SettingsScreenView(viewModel = viewModel)
                    "detail" -> {
                        val activeToolId = selectedToolId ?: ""
                        ToolDetailView(
                            viewModel = viewModel,
                            toolId = activeToolId,
                            onBack = { viewModel.navigateTo("home") }
                        )
                    }
                    else -> LoginScreenView(viewModel = viewModel)
                }
            }
        }
    }
}
