package com.jeiel85.daddymode.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeiel85.daddymode.data.model.MoodCheck
import com.jeiel85.daddymode.data.model.BreathSession
import com.jeiel85.daddymode.data.model.DailyDadMode
import com.jeiel85.daddymode.ui.viewmodel.DadModeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayLog(
    val date: String,
    val moodCheck: MoodCheck? = null,
    val breathSession: BreathSession? = null,
    val dailyDadMode: DailyDadMode? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(viewModel: DadModeViewModel) {
    val allMoodChecks by viewModel.allMoodChecks.collectAsStateWithLifecycle()
    val allBreathSessions by viewModel.allBreathSessions.collectAsStateWithLifecycle()
    val allDailyDadModes by viewModel.allDailyDadModes.collectAsStateWithLifecycle()
    val activeActions by viewModel.activeActions.collectAsStateWithLifecycle()

    // Consolidate tables grouped by date
    val combinedLogs = remember(allMoodChecks, allBreathSessions, allDailyDadModes) {
        val map = mutableMapOf<String, DayLog>()
        for (m in allMoodChecks) {
            map[m.date] = (map[m.date] ?: DayLog(m.date)).copy(moodCheck = m)
        }
        for (b in allBreathSessions) {
            map[b.date] = (map[b.date] ?: DayLog(b.date)).copy(breathSession = b)
        }
        for (d in allDailyDadModes) {
            map[d.date] = (map[d.date] ?: DayLog(d.date)).copy(dailyDadMode = d)
        }
        map.values.sortedByDescending { it.date }
    }

    // Weekly Summary calculations
    val weeklyStats = remember(combinedLogs) {
        val last7Days = combinedLogs.take(7)
        if (last7Days.isEmpty()) return@remember null

        val avgStress = last7Days.mapNotNull { it.moodCheck?.stressScore }.let { scores ->
            if (scores.isEmpty()) 0f else scores.average().toFloat()
        }
        val compBreathsCount = last7Days.count { it.breathSession?.isCompleted == true }
        val actionsCount = last7Days.count { it.dailyDadMode != null }
        val completedActionsCount = last7Days.count { it.dailyDadMode?.isActionDone == true }
        val actionRate = if (actionsCount == 0) 0f else (completedActionsCount.toFloat() / actionsCount) * 100

        Triple(avgStress, compBreathsCount, actionRate)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("아빠 전환 기록첩 📙", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Dashboard Header
            item {
                Text(
                    text = "주간 요약 보고서 (최근 7일 기록)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (weeklyStats == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "수집된 데이터가 부족합니다.\n퇴근기록을 체크하여 주간 요약을 확인해보세요.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val (avgStress, breaths, actionRate) = weeklyStats
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Average Stress
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("평균 스트레스", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f / 5", avgStress),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Divider(modifier = Modifier
                                    .height(48.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                                // Completed Breaths
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("호흡 완료 횟수", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${breaths}회 완료",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Divider(modifier = Modifier
                                    .height(48.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                                // Completed Actions rate
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("행동 실천율", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f%% 상태", actionRate),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Daily History List Section
            item {
                Text(
                    text = "경과된 날짜별 상세 기록",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (combinedLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "빈 기록",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "오늘 첫 퇴근 체크를 진행하여\n따뜻한 기록첩의 역사를 만들어보세요.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                items(combinedLogs) { log ->
                    DayLogCard(log, activeActions)
                }
            }
        }
    }
}

@Composable
fun DayLogCard(log: DayLog, activeActions: List<com.jeiel85.daddymode.data.model.DadAction>) {
    // Format date string (e.g. 2026-06-14 -> 6월 14일 일요일)
    val formattedDate = remember(log.date) {
        try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdfInput.parse(log.date)
            if (date != null) {
                val sdfOutput = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
                sdfOutput.format(date)
            } else {
                log.date
            }
        } catch (e: Exception) {
            log.date
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Date & Share indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (log.dailyDadMode?.isActionDone == true) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = "실천 100% 💖",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info Grid Row
            Row(modifier = Modifier.fillMaxWidth()) {
                // Stress Column
                if (log.moodCheck != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("스트레스", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val dotColor = when(log.moodCheck.stressScore) {
                                1 -> Color(0xFF48BB78)
                                2 -> Color(0xFF68D391)
                                3 -> Color(0xFFECC94B)
                                4 -> Color(0xFFED8936)
                                else -> Color(0xFFF56565)
                            }
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${log.moodCheck.stressScore}점 / 5", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Condition (Body + Mind)
                if (log.moodCheck != null) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("컨디션", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            "몸: ${log.moodCheck.bodyState} / 마음: ${log.moodCheck.mindState}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Breathing session Done check
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("3분 호흡", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isComp = log.breathSession?.isCompleted ?: false
                        Icon(
                            imageVector = if (isComp) Icons.Default.CheckCircle else Icons.Default.HelpOutline,
                            contentDescription = "호흡 여부",
                            tint = if (isComp) Color(0xFF48BB78) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isComp) "완료" else "없음",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isComp) Color(0xFF48BB78) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }

            // Memo note if exists
            if (log.moodCheck != null && log.moodCheck.memo.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "직장일지 메모: ${log.moodCheck.memo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            // Phrase and Actions checklist details
            if (log.dailyDadMode != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                // Log chosen phrase
                if (log.dailyDadMode.selectedPhrase.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = "약속 문구",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = log.dailyDadMode.selectedPhrase,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }

                // Log action title
                val matchingAction = activeActions.find { it.id == log.dailyDadMode.selectedActionId }
                val actTitle = matchingAction?.title ?: "아이 꼬옥 안아주기"
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (log.dailyDadMode.isActionDone) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                        contentDescription = "실천 상태",
                        tint = if (log.dailyDadMode.isActionDone) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "실천행동: $actTitle",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (log.dailyDadMode.isActionDone) FontWeight.Bold else FontWeight.Normal,
                            color = if (log.dailyDadMode.isActionDone) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}
