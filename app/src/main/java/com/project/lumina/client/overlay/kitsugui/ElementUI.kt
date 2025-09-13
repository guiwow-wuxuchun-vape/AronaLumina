package com.project.lumina.client.overlay.kitsugui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Shortcut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import com.project.lumina.client.constructors.*
import com.project.lumina.client.overlay.manager.OverlayManager
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ModuleContent(
    cheatCategory: CheatCategory,
    onOpenSettings: ((Element) -> Unit)? = null
) {
    val modules = remember(cheatCategory) {
        GameManager.elements.fastFilter { it.category === cheatCategory && it.name != "ChatListener" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            count = modules.size,
            key = { modules[it].name }          // 复用
        ) {
            ModuleCard(modules[it], onOpenSettings)
        }
    }
}

/* --------------------- 卡片 --------------------- */
@Composable
private fun ModuleCard(
    element: Element,
    onOpenSettings: ((Element) -> Unit)? = null
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val deepBlue = Color(0xFF1E90FF)

    // 1. 动画高度改用 Animatable，避免滚动时反复创建动画
    val accentHeight = remember { Animatable(70f) }
    LaunchedEffect(isExpanded) {
        accentHeight.animateTo(
            targetValue = if (isExpanded) 150f else 70f,
            animationSpec = tween(300, easing = EaseInOutCubic)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = element.isEnabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = { element.isEnabled = it }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(tween(300, easing = EaseInOutCubic)),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = element.displayNameResId?.let { stringResource(it) } ?: element.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF86D7F7)
                        )
                    }
                    if (onOpenSettings != null) {
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        element.overlayShortcutButton?.let { ShortcutContent(element) }
                        element.values.forEach { value ->
                            when (value) {
                                is BoolValue -> BoolValueContent(value)
                                is IntValue -> IntValueContent(value)
                                is FloatValue -> FloatValueContent(value)
                                is ListValue -> ChoiceValueContent(value)
                            }
                        }
                    }
                }
            }

            // 2. 侧边指示条高度直接用 Animatable 的值，无重组
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(8.dp)
                    .height(accentHeight.value.dp)
                    .background(
                        color = if (element.isEnabled && !isExpanded) deepBlue else Color.Transparent,
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    )
            )
        }
    }
}

/* --------------------- 各类值 Composable --------------------- */
@Composable
private fun ShortcutContent(element: Element) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .toggleable(
                value = element.isShortcutDisplayed,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = {
                    element.isShortcutDisplayed = it
                    if (it) OverlayManager.showOverlayWindow(element.overlayShortcutButton)
                    else OverlayManager.dismissOverlayWindow(element.overlayShortcutButton)
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Shortcut,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Shortcut",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF86D7F7)
            )
        }
        Switch(
            checked = element.isShortcutDisplayed,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF86D7F7),
                checkedTrackColor = Color(0xFFFFFFFF),
                uncheckedTrackColor = Color(0xFF4A4A4A)
            )
        )
    }
}

@Composable
private fun ChoiceValueContent(value: ListValue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = if (value.nameResId != 0) stringResource(value.nameResId) else value.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF86D7F7)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            value.listItems.forEach { item ->
                key(item.name) {          // 复用
                    val isSelected = value.value == item
                    FilterChip(
                        selected = isSelected,
                        onClick = { if (!isSelected) value.value = item },
                        label = {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color.Transparent else Color.Gray.copy(alpha = 0.5f)
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected) Color(0xFFFFF8F8) else Color(0xFF3A3A3A),
                            labelColor = if (isSelected) Color.Black else Color(0xFF86D7F7),
                            selectedContainerColor = Color(0xFF232323),
                            selectedLabelColor = Color(0xFF86D7F7)
                        )
                    )
                }
            }
        }
    }
}

/* --------------------- Float / Int --------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatValueContent(value: FloatValue) {
    val sliderPos = remember { Animatable(value.value) }

    // 外部变更时瞬间同步（ snapTo 必须在协程 ）
    LaunchedEffect(value.value) {
        sliderPos.snapTo(value.value)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (value.nameResId != 0) stringResource(value.nameResId) else value.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFF86D7F7)
            )
            val display = remember(value.value) {
                String.format(Locale.US, "%.2f", value.value)
            }
            Text(
                text = display,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF86D7F7)
            )
        }

        val colors = SliderDefaults.colors(
            thumbColor = Color(0xFF86D7F7),
            activeTrackColor = Color(0xFF86D7F7),
            inactiveTrackColor = Color(0xFF4A4A4A)
        )

        // 普通回调里不再调用 suspend 函数
        Slider(
            value = sliderPos.value,
            onValueChange = {
                val new = ((it * 100).roundToInt() / 100f).coerceIn(value.range)
                if (value.value != new) value.value = new
                // 只写 Animatable 的 value，不 snapTo
            },
            valueRange = value.range,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            colors = colors,
            interactionSource = remember { MutableInteractionSource() },
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { MutableInteractionSource() },
                    colors = colors,
                    thumbSize = DpSize(20.dp, 20.dp)
                )
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format(Locale.US, "%.1f", value.range.start),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = String.format(Locale.US, "%.1f", value.range.endInclusive),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntValueContent(value: IntValue) {
    val sliderPos = remember { Animatable(value.value.toFloat()) }

    LaunchedEffect(value.value) {
        sliderPos.snapTo(value.value.toFloat())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (value.nameResId != 0) stringResource(value.nameResId) else value.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFF86D7F7)
            )
            Text(
                text = value.value.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF86D7F7)
            )
        }

        val colors = SliderDefaults.colors(
            thumbColor = Color(0xFF86D7F7),
            activeTrackColor = Color(0xFF86D7F7),
            inactiveTrackColor = Color(0xFF4A4A4A)
        )
        Slider(
            value = sliderPos.value,
            onValueChange = {
                val new = it.roundToInt()
                if (value.value != new) value.value = new
                // 只写值，不 snapTo
            },
            valueRange = value.range.first.toFloat()..value.range.last.toFloat(),
            steps = (value.range.last - value.range.first - 1).coerceAtLeast(0),
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            colors = colors,
            interactionSource = remember { MutableInteractionSource() },
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { MutableInteractionSource() },
                    colors = colors,
                    thumbSize = DpSize(20.dp, 20.dp)
                )
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value.range.first.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = value.range.last.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}


/* --------------------- Bool --------------------- */
@Composable
private fun BoolValueContent(value: BoolValue) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .toggleable(
                value = value.value,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = { value.value = it }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (value.nameResId != 0) stringResource(value.nameResId) else value.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF86D7F7)
        )
        Switch(
            checked = value.value,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF86D7F7),
                uncheckedThumbColor = Color.Gray,
                checkedTrackColor = Color(0xFF9C9C9C),
                uncheckedTrackColor = Color(0xFF4A4A4A)
            )
        )
    }
}