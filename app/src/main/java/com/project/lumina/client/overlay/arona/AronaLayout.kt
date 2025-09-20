package com.project.lumina.client.overlay.arona

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.lumina.client.constructors.CheatCategory

/* -------------- 唯一对外入口 -------------- */
@Composable
internal fun AronaLayout(onClose: () -> Unit) {
    var selected by remember { mutableStateOf(CheatCategory.Home) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x70000000))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            Modifier.size(400.dp, 300.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Row(Modifier.fillMaxSize()) {
                /* 左侧：分类列表（占坑） */
                Column(
                    Modifier
                        .width(190.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF2A2A2A))
                ) {
                    Text("分类", color = Color.White, modifier = Modifier.padding(8.dp))
                }

                /* 中间：空区域 */
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFF18181B))
                )

                /* 右侧：模块列表（占坑） */
                Column(
                    Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF2A2A2A))
                ) {
                    Text("模块", color = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
