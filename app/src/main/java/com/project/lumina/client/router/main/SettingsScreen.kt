/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 */

package com.project.lumina.client.router.main

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.lumina.client.util.NetworkOptimizer
import com.project.lumina.client.viewmodel.MainScreenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.Socket

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsState()

    
    val sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

    
    var optimizeNetworkEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("optimizeNetworkEnabled", false))
    }
    var priorityThreadsEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("priorityThreadsEnabled", false))
    }
    var fastDnsEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("fastDnsEnabled", false))
    }
    var injectNekoPackEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("injectNekoPackEnabled", false))
    }
    var skipLoading by remember {
        mutableStateOf(sharedPreferences.getBoolean("skipLoading", false))
    }
    var disableConnectionInfoOverlay by remember {
        mutableStateOf(sharedPreferences.getBoolean("disableConnectionInfoOverlay", false))
    }
    var selectedGUI by remember {
        mutableStateOf(sharedPreferences.getString("selectedGUI", "KitsuGUI") ?: "KitsuGUI")
    }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var showAppSelectionDialog by remember { mutableStateOf(false) }

    var serverIp by remember { mutableStateOf(captureModeModel.serverHostName) }
    var serverPort by remember { mutableStateOf(captureModeModel.serverPort.toString()) }

    var selectedAppPackage by remember {
        mutableStateOf(sharedPreferences.getString("selectedAppPackage", "com.mojang.minecraftpe") ?: "com.mojang.minecraftpe")
    }
    var installedApps by remember { mutableStateOf<List<PackageInfo>>(emptyList()) }

    
    fun saveToggleState(key: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    fun saveGUISelection(value: String) {
        with(sharedPreferences.edit()) {
            putString("selectedGUI", value)
            apply()
        }
    }

    fun saveSelectedApp(packageName: String) {
        with(sharedPreferences.edit()) {
            putString("selectedAppPackage", packageName)
            apply()
        }
        mainScreenViewModel.selectGame(packageName)
    }

    fun getAppName(packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    fun getAppVersion(packageName: String): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    fun getAppIcon(packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    LaunchedEffect(Unit) {
        mainScreenViewModel.selectGame(selectedAppPackage)

        scope.launch(Dispatchers.IO) {
            try {
                val packageManager = context.packageManager
                val packages = packageManager.getInstalledPackages(0)
                    .filter { packageInfo ->
                        val appInfo = packageInfo.applicationInfo
                        appInfo != null &&
                        (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM == 0) &&
                        packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null
                    }
                    .sortedBy { packageInfo ->
                        val appInfo = packageInfo.applicationInfo
                        if (appInfo != null) {
                            packageManager.getApplicationLabel(appInfo).toString()
                        } else {
                            packageInfo.packageName
                        }
                    }
                installedApps = packages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        
                        DropdownMenu(
                            modifier = Modifier.fillMaxWidth(),
                            options = listOf("GraceGUI", "KitsuGUI", "ProtohaxUi", "ClickGUI", "WClientUI"),
                            selectedOption = selectedGUI,
                            onOptionSelected = { newSelection ->
                                selectedGUI = newSelection
                                saveGUISelection(newSelection)
                            }
                        )

                        Divider()

                        
                        SettingToggle(
                            title = "优化网络",
                            description = "初始化网络优化并配置套接字来获得更好的性能",
                            checked = optimizeNetworkEnabled,
                            onCheckedChange = { isEnabled ->
                                if (isEnabled) {
                                    scope.launch(Dispatchers.IO) {
                                        val success = NetworkOptimizer.init(context)
                                        if (success) {
                                            optimizeNetworkEnabled = true
                                            saveToggleState("optimizeNetworkEnabled", true)
                                            val socket = Socket()
                                            NetworkOptimizer.optimizeSocket(socket)
                                        } else {
                                            
                                            scope.launch(Dispatchers.Main) {
                                                showPermissionDialog = true
                                            }
                                        }
                                    }
                                } else {
                                    optimizeNetworkEnabled = false
                                    saveToggleState("optimizeNetworkEnabled", false)
                                }
                            }
                        )

                        Divider()

                        
                        SettingToggle(
                            title = "提高线程优先级",
                            description = "将应用线程设置为前台优先级以提升性能",
                            checked = priorityThreadsEnabled,
                            onCheckedChange = { isEnabled ->
                                priorityThreadsEnabled = isEnabled
                                saveToggleState("priorityThreadsEnabled", isEnabled)
                                scope.launch(Dispatchers.IO) {
                                    if (isEnabled) {
                                        NetworkOptimizer.setThreadPriority()
                                    }
                                }
                            }
                        )

                        Divider()

                        
                        SettingToggle(
                            title = "使用快速 DNS",
                            description = "使用谷歌 DNS 加速域名解析",
                            checked = fastDnsEnabled,
                            onCheckedChange = { isEnabled ->
                                fastDnsEnabled = isEnabled
                                saveToggleState("fastDnsEnabled", isEnabled)
                                scope.launch(Dispatchers.IO) {
                                    if (isEnabled) {
                                        NetworkOptimizer.useFastDNS()
                                    }
                                }
                            }
                        )

                        Divider()


                        SettingToggle(
                            title = "注入 Neko 包",
                            description = "开启之后获得一些功能的强化",
                            checked = injectNekoPackEnabled,
                            onCheckedChange = { isEnabled ->
                                injectNekoPackEnabled = isEnabled
                                saveToggleState("injectNekoPackEnabled", isEnabled)
                            }
                        )

                        Divider()
                        
                        
                        SettingToggle(
                            title = "跳过启动界面",
                            description = "启动时跳过Loading和模式选择界面",
                            checked = skipLoading,
                            onCheckedChange = { isEnabled ->
                                skipLoading = isEnabled
                                saveToggleState("skipLoading", isEnabled)
                            }
                        )

                        Divider()


                        SettingToggle(
                            title = "不显示连接信息 GUI",
                            description = "就是你进游戏之后看到的那个写着ip和端口的窗口，你把这玩意关了他就不显示了",
                            checked = disableConnectionInfoOverlay,
                            onCheckedChange = { isEnabled ->
                                disableConnectionInfoOverlay = isEnabled
                                saveToggleState("disableConnectionInfoOverlay", isEnabled)
                            }
                        )
                    }
                }
            }

            
            Column(
                modifier = Modifier
                    .weight(0.8f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.CenterHorizontally),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "服务器配置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "配置服务器的 IP 和端口",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "IP: $serverIp",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Port: $serverPort",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Button(
                                onClick = { showServerConfigDialog = true },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("配置")
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.CenterHorizontally),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Minecraft 选择器",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "在这里面可以选择其他的 Minecraft",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val appIcon = getAppIcon(selectedAppPackage)
                                if (appIcon != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = appIcon.toBitmap(48, 48).asImageBitmap(),
                                        contentDescription = "App Icon",
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = "Default App Icon",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column {
                                    Text(
                                        text = getAppName(selectedAppPackage),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "你选中的版本: ${getAppVersion(selectedAppPackage)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showAppSelectionDialog = true },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("选择")
                            }
                        }
                    }
                }

            }
        }
    }

    if (showPermissionDialog) {
        PermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onRequestPermission = {
                NetworkOptimizer.openWriteSettingsPermissionPage(context)
                showPermissionDialog = false
            }
        )
    }

    if (showServerConfigDialog) {
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ServerConfigDialog(
                initialIp = serverIp,
                initialPort = serverPort,
                onDismiss = { showServerConfigDialog = false },
                onSave = { ip, port ->
                    serverIp = ip
                    serverPort = port
                    showServerConfigDialog = false

                    try {
                        val portInt = port.toInt()
                        mainScreenViewModel.selectCaptureModeModel(
                            captureModeModel.copy(serverHostName = ip, serverPort = portInt)
                        )
                        Toast.makeText(context, "服务器配置已更新", Toast.LENGTH_SHORT).show()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "密码的不要乱输端口号", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    if (showAppSelectionDialog) {
        AppSelectionDialog(
            installedApps = installedApps,
            selectedAppPackage = selectedAppPackage,
            onDismiss = { showAppSelectionDialog = false },
            onAppSelected = { packageName ->
                selectedAppPackage = packageName
                saveSelectedApp(packageName)
                showAppSelectionDialog = false
                Toast.makeText(context, "你选择了: ${getAppName(packageName)}", Toast.LENGTH_SHORT).show()
            },
            getAppName = ::getAppName,
            getAppVersion = ::getAppVersion,
            getAppIcon = ::getAppIcon
        )
    }
}

@Composable
fun ServerConfigDialog(
    initialIp: String,
    initialPort: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var ip by remember { mutableStateOf(initialIp) }
    var port by remember { mutableStateOf(initialPort) }
    val coroutineScope = rememberCoroutineScope()

    
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isCompactScreen = screenWidth < 600.dp

    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialogScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "dialogAlpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "dialogOffsetY"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (0.4f * alpha.coerceIn(0f, 1f))))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                coroutineScope.launch {
                    isVisible = false
                    delay(300)
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        
        Card(
            modifier = Modifier
                .widthIn(min = 280.dp, max = min(screenWidth * 0.9f, 400.dp))
                .heightIn(max = screenHeight * 0.8f)
                .scale(scale)
                .alpha(alpha)
                .offset(y = offsetY.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume click to prevent propagation */ },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(if (isCompactScreen) 16.dp else 24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "配置服务器",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        label = { Text("IP 地址") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("端口") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    isVisible = false
                                    delay(300)
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel"
                                )
                                Text("取消")
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isVisible = false
                                    delay(300)
                                    onSave(ip, port)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Apply"
                                )
                                Text("完成")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("密码的没权限") },
        text = { Text("网络优化功能需要修改系统设置的权限，如果你要用这玩意，就必须给这个权限") },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text("授予权限")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("算了")
            }
        }
    )
}

@Composable
fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun DropdownMenu(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    
    
    val elevationState by animateFloatAsState(
        targetValue = if (expanded) 8f else 2f,
        label = "elevation"
    )

    Column(modifier = modifier) {
        Text(
            text = "功能 GUI",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = elevationState.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when (selectedOption) {
                                        "GraceGUI" -> MaterialTheme.colorScheme.primary
                                        "KitsuGUI" -> MaterialTheme.colorScheme.tertiary
                                        "ProtohaxUi" -> MaterialTheme.colorScheme.error
                                        "ClickGUI" -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                        )
                        
                        Text(
                            text = selectedOption,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .rotate(rotationState)
                            .size(20.dp)
                    )
                }
            }

            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(220.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    
                    androidx.compose.material3.DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(
                                            when (option) {
                                                "GraceGUI" -> MaterialTheme.colorScheme.primary
                                                "KitsuGUI" -> MaterialTheme.colorScheme.tertiary
                                                "ProtohaxUi" -> MaterialTheme.colorScheme.error
                                                "ClickGUI" -> MaterialTheme.colorScheme.secondary
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                )
                                
                                Text(
                                    text = option,
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    style = MaterialTheme.typography.bodyMedium
                                ) 
                            }
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .height(36.dp)
                            .background(
                                if (isSelected) 
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
                                else 
                                    Color.Transparent
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun AppSelectionDialog(
    installedApps: List<PackageInfo>,
    selectedAppPackage: String,
    onDismiss: () -> Unit,
    onAppSelected: (String) -> Unit,
    getAppName: (String) -> String,
    getAppVersion: (String) -> String,
    getAppIcon: (String) -> Drawable?
) {
    var isVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialogScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "dialogAlpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "dialogOffsetY"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (0.4f * alpha.coerceIn(0f, 1f))))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                coroutineScope.launch {
                    isVisible = false
                    delay(300)
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 320.dp, max = min(screenWidth * 0.9f, 500.dp))
                .heightIn(max = screenHeight * 0.8f)
                .scale(scale)
                .alpha(alpha)
                .offset(y = offsetY.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume click to prevent propagation */ },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "选择 App",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "你在主菜单点了开始之后，这个app就会自动启动",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(installedApps) { packageInfo ->
                        val packageName = packageInfo.packageName
                        val isSelected = packageName == selectedAppPackage

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        isVisible = false
                                        delay(300)
                                        onAppSelected(packageName)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 4.dp else 2.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val appIcon = getAppIcon(packageName)
                                if (appIcon != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = appIcon.toBitmap(48, 48).asImageBitmap(),
                                        contentDescription = "App Icon",
                                        modifier = Modifier.size(40.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = "Default App Icon",
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = getAppName(packageName),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "v${getAppVersion(packageName)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isVisible = false
                                delay(300)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel"
                            )
                            Text("算了")
                        }
                    }
                }
            }
        }
    }
}