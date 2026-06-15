package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.DadModeViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MoodCheckScreen(
    viewModel: DadModeViewModel,
    onBack: () -> Unit,
    onMoodSaved: () -> Unit
) {
    var stressScore by remember { mutableIntStateOf(3) }
    var bodyState by remember { mutableStateOf("괜찮음") }
    var mindState by remember { mutableStateOf("평온") }
    var memo by remember { mutableStateOf("") }

    val bodyStates = listOf("괜찮음", "피곤함", "지침", "예민함")
    val mindStates = listOf("평온", "답답함", "화남", "걱정", "무기력")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("1단계: 오늘 하루 정리", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "이전")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Screen Guide Text
            item {
                Text(
                    text = "집에 들어가기 전에 회사 일과 기분을 흘려보내 볼까요?\n오늘 있었던 상태를 솔직하게 선택해 주세요.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 24.sp
                )
            }

            // Stress Score (1-5)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "회사 스트레스 점수",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "숫자가 높을수록 회사 상황이 복잡하고 힘들었음을 말합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { score ->
                                val isSelected = stressScore == score
                                val circleColor = when(score) {
                                    1 -> Color(0xFF48BB78)
                                    2 -> Color(0xFF68D391)
                                    3 -> Color(0xFFECC94B)
                                    4 -> Color(0xFFED8936)
                                    else -> Color(0xFFF56565)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) circleColor else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { stressScore = score },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = score.toString(),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 18.sp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val stressPrompt = when(stressScore) {
                            1 -> "평화로운 만점 하루! 퇴근길이 가볍습니다."
                            2 -> "무난한 직장 일상이었습니다. 괜찮은 편이에요."
                            3 -> "평범하게 고단했던 하루였습니다. 피로를 불어넣어 버리세요."
                            4 -> "상당한 회사 고난이 있었습니다. 집 앞 가기 전 마음 환기가 시급합니다."
                            else -> "매우 힘들고 지친 하루였습니다. 3분 호흡 명상 때 모두 내쉬어 보세요."
                        }
                        Text(
                            text = stressPrompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Body Condition (몸 상태)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "지금 나의 몸 상태",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            bodyStates.forEach { state ->
                                val isSelected = bodyState == state
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { bodyState = state },
                                    label = {
                                        Text(
                                            text = state,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 16.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Mind Condition (마음 상태)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "지금 나의 마음 상태",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            mindStates.forEach { state ->
                                val isSelected = mindState == state
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { mindState = state },
                                    label = {
                                        Text(
                                            text = state,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 16.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Memo / Self note
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "하루 한마디 메모 (선택)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "오늘 힘들었거나 머리 아팠던 상사 지시, 일 등을 적어 비워 보세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            placeholder = { Text("오늘의 짧은 메모를 남겨 회사 일을 비우세요.") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Submit / Save and Go Next Step Button
            item {
                Button(
                    onClick = {
                        viewModel.saveMoodCheck(
                            stressScore = stressScore,
                            bodyState = bodyState,
                            mindState = mindState,
                            memo = memo
                        )
                        onMoodSaved()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("오늘 기분 저장 및 비우기 호흡 단계로", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "다음 단계")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
