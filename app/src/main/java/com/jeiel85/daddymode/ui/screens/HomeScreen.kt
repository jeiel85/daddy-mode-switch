package com.jeiel85.daddymode.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeiel85.daddymode.data.model.MoodCheck
import com.jeiel85.daddymode.data.model.DailyDadMode
import com.jeiel85.daddymode.ui.viewmodel.DadModeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: DadModeViewModel,
    onStartTransition: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val todayDate by viewModel.todayDateString.collectAsStateWithLifecycle()
    val todayMood by viewModel.todayMoodCheck.collectAsStateWithLifecycle()
    val todayBreath by viewModel.todayBreathSession.collectAsStateWithLifecycle()
    val todayDadMode by viewModel.todayDailyDadMode.collectAsStateWithLifecycle()
    val activeActions by viewModel.activeActions.collectAsStateWithLifecycle()
    val allMoodChecks by viewModel.allMoodChecks.collectAsStateWithLifecycle()

    val notificationEnabled by viewModel.notificationEnabled.collectAsStateWithLifecycle()
    val notifHour by viewModel.notificationHour.collectAsStateWithLifecycle()
    val notifMinute by viewModel.notificationMinute.collectAsStateWithLifecycle()

    // Calculate transition progress state
    // Step 1: Mood index (0.33)
    // Step 2: Breath session (0.33)
    // Step 3: Phrase selected (0.34)
    val progress = remember(todayMood, todayBreath, todayDadMode) {
        var p = 0f
        if (todayMood != null) p += 0.33f
        if (todayBreath != null) p += 0.33f
        if (todayDadMode != null && todayDadMode?.selectedPhrase?.isNotEmpty() == true) p += 0.34f
        p
    }

    val stateText = when {
        progress >= 0.99f -> "따뜻한 100% 아빠 완료 🏡"
        progress >= 0.6f -> "아빠모드 거의 완료 (문 앞에서 한숨 고르기)"
        progress >= 0.3f -> "퇴근길 전환 진행 중..."
        else -> "퇴근 준비 중... 회사 아빠 👔"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Core Slogan / Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "퇴근길 아빠모드",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "회사에서의 무거운 스트레스는 퇴근길에 흘려버리고,\n가장 소중한 아내와 아이에게 웃어줄 시간입니다.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                lineHeight = 22.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Today's Conversion Progress bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "오늘의 아빠모드 전환도",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stateText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (progress >= 0.99f) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StepIndicator("1. 스트레스 체크", todayMood != null)
                        StepIndicator("2. 3분 호흡 명상", todayBreath != null)
                        StepIndicator("3. 가족 한마디 약속", todayDadMode != null)
                    }
                }
            }
        }

        // Action CTA: Start 3 minutes transition series
        item {
            Button(
                onClick = onStartTransition,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "전환 시작",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "오늘의 3분 퇴근 전환 시작하기",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Today's Family Action checklist block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "가족을 대하는 오늘 저녁 나의 행동",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (todayDadMode != null) {
                        val matchingAction = activeActions.find { it.id == todayDadMode?.selectedActionId }
                        val actionTitle = matchingAction?.title ?: "아이 꼭 안아주기"
                        val progressChecked = todayDadMode?.isActionDone ?: false

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (progressChecked) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.markTodayActionAsDone(!progressChecked)
                                }
                                .padding(16.dp)
                        ) {
                            Checkbox(
                                checked = progressChecked,
                                onCheckedChange = { viewModel.markTodayActionAsDone(it) },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = actionTitle,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (progressChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (progressChecked) "실천 완료! 오늘 저녁도 멋진 아빠입니다 👏" else "집에 도착해서 실천해 보세요.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (progressChecked) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        if (todayDadMode?.selectedPhrase?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "오늘 저녁 첫 문장 약속",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = todayDadMode?.selectedPhrase ?: "",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Start
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp)
                            )
                        }

                    } else {
                        // Empty action selection
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "아직 저녁 한마디와 행동을 정하지 않았습니다.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = onStartTransition) {
                                    Text("원터치 전환 루틴으로 한마디 고르기", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Commute Alarm reminder widget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSettings() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Alarm,
                            contentDescription = "알림 정보",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "퇴근길 아빠모드 알림",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (notificationEnabled) {
                                String.format(Locale.getDefault(), "매일 오후 %02d시 %02d분 예약 중", notifHour, notifMinute)
                            } else {
                                "알림 꺼짐"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "설정 이동",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }

        // Recent 7 Days Mood status history chart snippet
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToHistory() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "최근 7일 퇴근 기록 흐름",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "상세 기록 정보",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 7-day indicator blocks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val calendar = Calendar.getInstance()
                        val sdf = SimpleDateFormat("E", Locale.KOREA)
                        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                        // Prepopulate last 7 days keys
                        val last7Days = remember(todayDate, allMoodChecks) {
                            (0..6).map { i ->
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DATE, -i)
                                cal
                            }.reversed()
                        }

                        for (cal in last7Days) {
                            val currentDateKey = dateSdf.format(cal.time)
                            val dayLabel = sdf.format(cal.time)
                            val check = allMoodChecks.find { it.date == currentDateKey }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentDateKey == todayDate) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = when {
                                                check != null -> {
                                                    // Stress colors: lower score -> green/calm, higher -> coral/alert
                                                    when (check.stressScore) {
                                                        1 -> Color(0xFF48BB78) // peaceful green
                                                        2 -> Color(0xFF68D391) // soft green
                                                        3 -> Color(0xFFECC94B) // moderate yellow
                                                        4 -> Color(0xFFED8936) // intense orange
                                                        else -> Color(0xFFF56565) // high stress red
                                                    }
                                                }
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (check != null) {
                                        Text(
                                            text = check.stressScore.toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "기록 없음",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(label: String, completed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Icon(
            imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = label,
            tint = if (completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (completed) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                color = if (completed) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
    }
}
