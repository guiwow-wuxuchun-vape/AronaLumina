package com.project.lumina.client.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background // 添加这个导入
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.project.lumina.client.ui.theme.LuminaClientTheme

private const val TOTAL_PAGES = 4
private const val QQ_GROUP_URL = "https://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=7XXYAD_O7dGg0vSWxpQlc3QVaPcGMzh6&authKey=nXgqANOzsWiKBF1OydnlRcmZN0cFlZb2EQ+PUat7ymg2LsddmVCCU43yylyikDwJ&noverify=0&group_code=915442376"
private const val GITHUB_URL = "https://github.com/guiwow-wuxuchun-vape/AronaLumina"

class HelpActivity : ComponentActivity() {

    private var currentPage by mutableStateOf(0)

    private val storageLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    private val specialLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LuminaClientTheme { 
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF404040)
                ) {
                    GuideScreen(
                        currentPage = currentPage,
                        onPageChange = { currentPage = it },
                        onFinish = {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        },
                        onRequestStorage = { requestStorage() },
                        onRequestOverlay = { requestOverlay() }
                    )
                }
            }
        }
    }

 /*   override fun onResume() {
        super.onResume()
        when (currentPage) {
            2 -> if (storageGranted()) currentPage++
            3 -> if (overlayGranted()) currentPage++
        }
    }*/

    private fun requestStorage() {
        if (storageGranted()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            specialLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        } else {
            val arr = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
            if (arr.isNotEmpty()) storageLauncher.launch(arr)
        }
    }

    private fun requestOverlay() {
        if (overlayGranted()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            specialLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }

    private fun storageGranted() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Environment.isExternalStorageManager()
        else
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

    private fun overlayGranted() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)
}

/* ------------------------------------------------------------------ */
/* -------------------------  Compose 区域  --------------------------- */
/* ------------------------------------------------------------------ */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun GuideScreen(
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    onFinish: () -> Unit,
    onRequestStorage: () -> Unit,
    onRequestOverlay: () -> Unit
) {
    val context = LocalContext.current

    val pages: List<@Composable () -> Unit> = listOf(
        {
            GenericPage(
                title = "欢迎使用AronaLumina",
                desc = "针对基岩版 Minecraft 的 MITM 作弊工具"
            )
        },
        {
            GenericPage(
                title = "如何使用",
                desc = "在应用主界面点击\"账户\"，然后登录你的微软账号\n如果你没有微软账号，请去微软官网注册，不要在本应用内注册"
            )
        },
        {
            GenericPage(
                title = "如何使用",
                desc = "账户添加完成之后，选中账户，再点击\"服务器\"回到服务器主界面，选中服务器之后点击\"启动\"，然后在minecraft的游戏界面中点击带有\"Lumina\"字样的多人游戏，如果没有，请查看游戏启动时的连接仓库，并在服务器界面自行添加"
            )
        },
        {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text("获取帮助", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(16.dp))
                Text("遇到问题？欢迎加入我们的社区。")
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(QQ_GROUP_URL)) // 使用常量
                    )
                }) { Text("加入 QQ 群聊") }
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)) // 使用常量
                    )
                }) { Text("访问 GitHub") }
            }
        }
    )

    Scaffold(
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onPageChange(currentPage - 1) },
                    enabled = currentPage > 0
                ) { Text("上一页") }

                Box(Modifier.weight(1f), Alignment.Center) {
                    PageIndicator(total = TOTAL_PAGES, index = currentPage)
                }

                TextButton(
                    onClick = {
                        if (currentPage == pages.lastIndex) onFinish()
                        else onPageChange(currentPage + 1)
                    }
                ) {
                    Text(if (currentPage == pages.lastIndex) "完成" else "下一页")
                }
            }
        }
    ) { pv ->
        Box(Modifier.padding(pv)) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    slideInHorizontally { it } with slideOutHorizontally { -it }
                }
            ) { idx -> pages[idx]() }
        }
    }
}

@Composable
private fun GenericPage(title: String, desc: String) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = Color(0xFF86D7F7))
        Spacer(Modifier.height(16.dp))
        Text(desc, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF86D7F7))
    }
}

@Composable
private fun PermissionPage(title: String, desc: String, onGrant: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = Color(0xFF86D7F7))
        Spacer(Modifier.height(16.dp))
        Text(desc, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF86D7F7))
        Spacer(Modifier.height(32.dp))
        Button(onClick = onGrant) { Text("授予权限") }
    }
}

@Composable
private fun PageIndicator(total: Int, index: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            val color by animateColorAsState(
                if (i == index) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            val width by animateDpAsState(if (i == index) 24.dp else 8.dp)
            Box(
                Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color) // 这里使用了background，现在应该可以正常编译了
            )
        }
    }
}