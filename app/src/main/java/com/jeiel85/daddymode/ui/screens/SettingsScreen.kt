package com.jeiel85.daddymode.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeiel85.daddymode.ui.viewmodel.DadModeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(viewModel: DadModeViewModel) {
    val context = LocalContext.current

    val notificationEnabled by viewModel.notificationEnabled.collectAsStateWithLifecycle()
    val notifHour by viewModel.notificationHour.collectAsStateWithLifecycle()
    val notifMinute by viewModel.notificationMinute.collectAsStateWithLifecycle()

    val breathMinutes by viewModel.breathDurationMinutes.collectAsStateWithLifecycle()
    val inhaleSeconds by viewModel.inhaleSeconds.collectAsStateWithLifecycle()
    val holdSeconds by viewModel.holdSeconds.collectAsStateWithLifecycle()
    val exhaleSeconds by viewModel.exhaleSeconds.collectAsStateWithLifecycle()

    val phrases by viewModel.phrases.collectAsStateWithLifecycle()

    var newPhraseText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("아빠모드 설정 ⚙️", fontWeight = FontWeight.Bold) }
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
            // Section 1: Commute notification Alarm configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "퇴근 전환 시간 알림 수신",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "매일 설정된 퇴근시간에 리마인더 발송",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Switch(
                                checked = notificationEnabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.updateNotificationSettings(isChecked, notifHour, notifMinute)
                                    Toast.makeText(
                                        context,
                                        if (isChecked) "퇴근 전환 시간 리마인더가 설정되었습니다." else "리마인더가 중지되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }

                        if (notificationEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "퇴근 시간 미세 조정 (쉬운 터치 카운터)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Hour Picker
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val nextH = if (notifHour == 0) 23 else notifHour - 1
                                            viewModel.updateNotificationSettings(true, nextH, notifMinute)
                                        },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "1시간 마이너스", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d시", notifHour),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(
                                        onClick = {
                                            val nextH = if (notifHour == 23) 0 else notifHour + 1
                                            viewModel.updateNotificationSettings(true, nextH, notifMinute)
                                        },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "1시간 플러스", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }

                                // Minute Picker
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val nextM = if (notifMinute < 10) 50 else notifMinute - 10
                                            viewModel.updateNotificationSettings(true, notifHour, nextM)
                                        },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "10분 마이너스", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d분", notifMinute),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(
                                        onClick = {
                                            val nextM = if (notifMinute >= 50) 0 else notifMinute + 10
                                            viewModel.updateNotificationSettings(true, notifHour, nextM)
                                        },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "10분 플러스", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Breathing settings configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "호흡 시간 & 패턴 설정",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "원하는 총 호흡 시간과 들숨, 정지, 날숨 시간을 조절해 보세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Duration selector row tabs
                        Text(
                            text = "총 호흡 목표 시간",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(1, 3, 5).forEach { min ->
                                val isSelected = min == breathMinutes
                                Button(
                                    onClick = {
                                        viewModel.updateBreathSettings(min, inhaleSeconds, holdSeconds, exhaleSeconds)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text("${min}분", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Inhale, Hold, Exhale counters
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            // Inhale (들숨)
                            BreathPatternMetricPicker(
                                label = "들숨 (Inhale)",
                                value = inhaleSeconds,
                                onValueUpdate = { nextValue ->
                                    viewModel.updateBreathSettings(breathMinutes, nextValue, holdSeconds, exhaleSeconds)
                                }
                            )

                            // Hold (정지)
                            BreathPatternMetricPicker(
                                label = "참기 (Hold)",
                                value = holdSeconds,
                                onValueUpdate = { nextValue ->
                                    viewModel.updateBreathSettings(breathMinutes, inhaleSeconds, nextValue, exhaleSeconds)
                                }
                            )

                            // Exhale (날숨)
                            BreathPatternMetricPicker(
                                label = "날숨 (Exhale)",
                                value = exhaleSeconds,
                                onValueUpdate = { nextValue ->
                                    viewModel.updateBreathSettings(breathMinutes, inhaleSeconds, holdSeconds, nextValue)
                                }
                            )
                        }
                    }
                }
            }

            // Section 3: Manage Greeting recommended Phrases list
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "집 대문용 따뜻한 한마디 목록 관리",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "퇴근하고 집에 들어설 때 아내와 아이를 웃게 할 나만의 커스텀 한마디를 추가하세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom adds slot
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newPhraseText,
                                onValueChange = { newPhraseText = it },
                                placeholder = { Text("예) '여보, 오늘도 수고 많았어!'") },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newPhraseText.isNotBlank()) {
                                        viewModel.addPhrase(newPhraseText)
                                        newPhraseText = ""
                                        Toast.makeText(context, "새 문장이 약속 목록에 추가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Text("추가", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // List of phrases with quick remove slot
                        phrases.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        viewModel.deletePhrase(item)
                                        Toast.makeText(context, "문장이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "문장 삭제",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            // Section 4: Local app data wipe out
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "데이터 조작 위험 영역",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "지금까지 수집된 모든 퇴근 기분 체크, 호흡 명상 결과, 그리고 추가하신 커스텀 문구들이 영구 삭제되고 설정이 공장 출하값으로 원복됩니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "경고")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("모든 로컬 데이터 초기화", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("정말 초기화하시겠습니까?", fontWeight = FontWeight.Bold) },
            text = { Text("이 작업은 취소할 수 없으며 휴대폰 로컬에 보관된 모든 아빠 일지 정보가 깨끗하게 지워집니다.", fontSize = 16.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllData()
                        showResetDialog = false
                        Toast.makeText(context, "초기화 작업 완료! 모든 데이터가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("네, 초기화합니다.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showResetDialog = false }
                ) {
                    Text("취소", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun BreathPatternMetricPicker(
    label: String,
    value: Int,
    onValueUpdate: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (value > 1) onValueUpdate(value - 1) },
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "빼기", modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${value}초",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { if (value < 15) onValueUpdate(value + 1) },
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "더하기", modifier = Modifier.size(16.dp))
            }
        }
    }
}
