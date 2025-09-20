package com.project.lumina.client.overlay.arona

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.constructors.*
import com.project.lumina.client.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun AronaLayout(onClose: () -> Unit) {
    var selectedCategory by remember { mutableStateOf(CheatCategory.Home) }
    var selectedModule by remember { mutableStateOf<Element?>(null) }

    val dismiss = remember {
        val scope = rememberCoroutineScope()
        val alpha  = remember { Animatable(1f) }
        val transY = remember { Animatable(0f)  }
        {
            scope.launch {
                launch { alpha.animateTo(0f,  tween(200)) }
                launch { transY.animateTo(100f, tween(200)) }
                delay(200)
                onClose()
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0x70000000), Color(0x90000000))
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (selectedModule == null) dismiss() },
        contentAlignment = Alignment.Center
    ) {

        /* 主卡片 */
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AronaSurface),
            modifier = Modifier
                .widthIn(max = 660.dp)
                .height(500.dp)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .enterScale(),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Row(Modifier.fillMaxSize()) {
                /* 左侧 */
                Column(
                    Modifier
                        .width(190.dp)
                        .fillMaxHeight()
                        .padding(5.dp)
                ) {
                    LogoBar()
                    Spacer(Modifier.height(5.dp))
                    CategoryList(selectedCategory) { selectedCategory = it }
                }

                /* 中间 */
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 5.dp)
                ) {
                    CenterArea()
                }

                /* 右侧 */
                Column(
                    Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                        .padding(5.dp)
                ) {
                    ModuleList(selectedCategory) { selectedModule = it }
                    Spacer(Modifier.height(5.dp))
                    BottomButtons(dismiss)
                }
            }
        }

        /* 设置弹层 */
        selectedModule?.let {
            KitsuSettingsOverlay(
                element = it,
                onDismiss = { selectedModule = null }
            )
        }
    }
}

/* --------------------------------------------------
 * 以下纯 UI
 * -------------------------------------------------- */

@Composable
private fun LogoBar() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(AronaIcons.logo),
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                RainbowText("LUMINA")
            }
        }
    }
}

@Composable
private fun RainbowText(text: String) {
    val infinite = rememberInfiniteTransition(label = "rainbow")
    val hue by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatSpec(tween(3000, easing = LinearEasing)),
        label = "hue"
    )
    Text(
        text = text,
        fontSize = 19.sp,
        fontFamily = modernFont,
        fontWeight = FontWeight.Bold,
        color = Color.hsv(hue, 0.8f, 1f)
    )
}

@Composable
private fun CategoryList(
    selected: CheatCategory,
    onSelect: (CheatCategory) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(CheatCategory.entries) { cat ->
                CategoryItem(cat, selected == cat, onSelect)
            }
        }
    }
}

@Composable
private fun CategoryItem(
    cat: CheatCategory,
    selected: Boolean,
    onSelect: (CheatCategory) -> Unit
) {
    val bg = animateColorAsState(
        if (selected) AronaSelected.copy(0.8f) else Color.Transparent,
        label = "catBg"
    )
    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg.value)
            .clickable { onSelect(cat) }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(cat.iconResId),
            contentDescription = null,
            tint = if (selected) Color.White else AronaOnSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            getCategoryTitle(cat),
            color = if (selected) Color.White else AronaOnSurfaceVariant,
            fontFamily = modernFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun CenterArea() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AutoFlipText()
            Column(
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(AronaIcons.logo),
                    contentDescription = null,
                    modifier = Modifier.size(90.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "LUMINA CLIENT",
                    fontFamily = modernFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ready - ${GameManager.netBound?.getCurrentPlayers()?.size ?: 0} online",
                    color = Color(0xFF86D7F7),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AutoFlipText() {
    val list = remember {
        listOf(
            "哔哩哔哩关注稽静小白，谢谢捏😋",
            "sensei，你想要九蓝一金吗😈"
        )
    }
    var index by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4580)
            index = (index + 1) % list.size
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 50.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            list[index],
            color = Color.White,
            fontSize = 15.sp,
            fontFamily = modernFont,
            modifier = Modifier
                .background(Color(0xFF2A2A2A), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ModuleList(
    category: CheatCategory,
    onSettings: (Element) -> Unit
) {
    val modules = remember(category) {
        GameManager.elements.fastFilter {
            it.category === category && it.name != "ChatListener"
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(modules, key = { it.name }) { mod ->
                ModuleCard(mod, onSettings)
            }
        }
    }
}

@Composable
private fun ModuleCard(
    element: Element,
    onSettings: (Element) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        border = BorderStroke(1.dp, if (element.isEnabled) AronaPrimary else Color.Gray)
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    element.displayNameResId
                        ?.let { stringResource(it) } ?: element.name,
                    color = Color(0xFF86D7F7),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onSettings(element) }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    element.values.forEach { v ->
                        when (v) {
                            is BoolValue -> BoolRow(v)
                            is IntValue -> IntRow(v)
                            is FloatValue -> FloatRow(v)
                            is ListValue -> ListRow(v)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoolRow(v: BoolValue) = Row(
    Modifier
        .fillMaxWidth()
        .toggleable(v.value) { v.value = it },
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        if (v.nameResId != 0) stringResource(v.nameResId) else v.name,
        color = Color(0xFF86D7F7),
        fontSize = 13.sp,
        modifier = Modifier.weight(1f)
    )
    Switch(checked = v.value, onCheckedChange = null)
}

@Composable
private fun IntRow(v: IntValue) = Column {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(
            if (v.nameResId != 0) stringResource(v.nameResId) else v.name,
            color = Color(0xFF86D7F7),
            fontSize = 13.sp
        )
        Text(v.value.toString(), color = Color.White, fontSize = 13.sp)
    }
    Slider(
        value = v.value.toFloat(),
        onValueChange = { v.value = it.roundToInt() },
        valueRange = v.range.first.toFloat()..v.range.last.toFloat(),
        modifier = Modifier.height(24.dp),
        colors = sliderColors()
    )
}

@Composable
private fun FloatRow(v: FloatValue) = Column {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(
            if (v.nameResId != 0) stringResource(v.nameResId) else v.name,
            color = Color(0xFF86D7F7),
            fontSize = 13.sp
        )
        Text(
            String.format(java.util.Locale.US, "%.2f", v.value),
            color = Color.White,
            fontSize = 13.sp
        )
    }
    Slider(
        value = v.value,
        onValueChange = { v.value = it },
        valueRange = v.range,
        modifier = Modifier.height(24.dp),
        colors = sliderColors()
    )
}

@Composable
private fun ListRow(v: ListValue) = Column {
    Text(
        if (v.nameResId != 0) stringResource(v.nameResId) else v.name,
        color = Color(0xFF86D7F7),
        fontSize = 13.sp
    )
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        v.listItems.forEach { item ->
            FilterChip(
                selected = v.value == item,
                onClick = { v.value = item },
                label = { Text(item.name, fontSize = 12.sp) },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AronaPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF3A3A3A),
                    labelColor = Color(0xFF86D7F7)
                )
            )
        }
    }
}

@Composable
private fun BottomButtons(onClose: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { openUrl("https://discord.gg/6kz3dcndrN") }) {
                Icon(
                    painterResource(AronaIcons.discord),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { openUrl("https://projectlumina.netlify.app/") }) {
                Icon(
                    painterResource(AronaIcons.web),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    painterResource(AronaIcons.close),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/* ---------- 工具 ---------- */
private fun openUrl(url: String) {
    val context = LocalContext.current
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

private fun sliderColors() = SliderDefaults.colors(
    thumbColor = Color(0xFF86D7F7),
    activeTrackColor = Color(0xFF86D7F7),
    inactiveTrackColor = Color(0xFF4A4A4A)
)

private fun Modifier.enterScale(): Modifier = composed {
    val scale = remember { Animatable(0.95f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
        launch { alpha.animateTo(1f, tween(350, easing = LinearOutSlowInEasing)) }
    }
    graphicsLayer {
        this.scaleX = scale.value
        this.scaleY = scale.value
        this.alpha = alpha.value
    }
}

/* 与旧包同名函数，方便 copy */
private fun getCategoryTitle(cat: CheatCategory) = when (cat) {
    CheatCategory.Combat -> "蘸豆😡"
    CheatCategory.Motion -> "移动😋"
    CheatCategory.World -> "世界🤔"
    CheatCategory.Visual -> "渲染🤓"
    CheatCategory.Misc -> "杂项🧐"
    CheatCategory.Config -> "配置💾"
    CheatCategory.Home -> "信息📋"
    else -> "Modules"
}
