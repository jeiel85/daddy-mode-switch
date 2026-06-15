package com.jeiel85.daddymode.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeiel85.daddymode.ui.viewmodel.DadModeViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathScreen(
    viewModel: DadModeViewModel,
    onBack: () -> Unit,
    onBreathCompleted: () -> Unit
) {
    // Read user breathe configuration settings
    val totalMinutes by viewModel.breathDurationMinutes.collectAsStateWithLifecycle()
    val inhaleTypeSeconds by viewModel.inhaleSeconds.collectAsStateWithLifecycle()
    val holdTypeSeconds by viewModel.holdSeconds.collectAsStateWithLifecycle()
    val exhaleTypeSeconds by viewModel.exhaleSeconds.collectAsStateWithLifecycle()

    val totalDurationSeconds = remember(totalMinutes) { totalMinutes * 60 }
    val roundDuration = remember(inhaleTypeSeconds, holdTypeSeconds, exhaleTypeSeconds) {
        inhaleTypeSeconds + holdTypeSeconds + exhaleTypeSeconds
    }

    var isRunning by remember { mutableStateOf(true) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Tick tracker (updates every 50ms for ultra-smooth animations)
    var elapsedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = System.currentTimeMillis() - elapsedMs
            while (isRunning && (elapsedMs / 1000) < totalDurationSeconds) {
                elapsedMs = System.currentTimeMillis() - startTime
                elapsedSeconds = (elapsedMs / 1000).toInt()
                delay(30)
            }
            if ((elapsedMs / 1000) >= totalDurationSeconds) {
                // Completed!
                viewModel.saveBreathSession(true)
                onBreathCompleted()
            }
        }
    }

    val remainingSeconds = maxOf(0, totalDurationSeconds - elapsedSeconds)
    val minutesLeft = remainingSeconds / 60
    val secondsLeft = remainingSeconds % 60

    // Compute exact breath stage state
    val msInRound = elapsedMs % (roundDuration * 1000L)
    val secondsInRoundDouble = msInRound / 1000.0

    val (currentStageText, stageProgress, bubbleScale) = remember(
        secondsInRoundDouble,
        inhaleTypeSeconds,
        holdTypeSeconds,
        exhaleTypeSeconds,
        roundDuration
    ) {
        val inhaleBound = inhaleTypeSeconds.toDouble()
        val holdBound = inhaleBound + holdTypeSeconds.toDouble()

        when {
            secondsInRoundDouble < inhaleBound -> {
                // Inhale (들숨): scale grows from 1.0f to 1.8f
                val fraction = secondsInRoundDouble / inhaleBound
                val scale = 1.0f + (0.8f * fraction.toFloat())
                Triple("들이마시기... (들숨) 👃", fraction, scale)
            }
            secondsInRoundDouble < holdBound -> {
                // Hold (멈춤): scale hangs at 1.8f
                val fraction = (secondsInRoundDouble - inhaleBound) / holdTypeSeconds.toDouble()
                Triple("잠시 멈추기... (정지) ⏱", fraction, 1.8f)
            }
            else -> {
                // Exhale (날숨): scale shrinks from 1.8f to 1.0f
                val fraction = (secondsInRoundDouble - holdBound) / exhaleTypeSeconds.toDouble()
                val scale = 1.8f - (0.8f * fraction.toFloat())
                Triple("길게 내쉬기... (날숨) 🌬", fraction, scale)
            }
        }
    }

    val stageColor = remember(currentStageText) {
        when {
            currentStageText.contains("들숨") -> Color(0xFF4FD1C5) // relaxing cyan-ocean
            currentStageText.contains("정지") -> Color(0xFFECC94B) // comforting bronze-amber
            else -> Color(0xFFED8936) // warm cozy sunset orange
        }
    }

    // Always render customized deep-tranquil midnight dark theme for breathing
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A) // Slate-900 (deep tranquility)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Cancel button at top-left
            IconButton(
                onClick = {
                    isRunning = false
                    showExitDialog = true
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color(0xFF1E293B), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "중단",
                    tint = Color.White
                )
            }

            // Top screen guide
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "2단계: 3분 비우기 호흡",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "화면의 팽창과 수축에 맞춰 숨을 나누어 보세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Main Central Interactive Breathing Bubble
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Breathing Core pulsing canvas circles
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.15f,
                        targetValue = 0.35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val baseRadius = 80.dp.toPx()
                        val animatedRadius = baseRadius * bubbleScale

                        // Outer breathing ring
                        drawCircle(
                            color = stageColor,
                            radius = animatedRadius + 30.dp.toPx(),
                            alpha = pulseAlpha,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Outer glowing circle
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(stageColor.copy(alpha = 0.5f), Color.Transparent),
                                radius = animatedRadius + 20.dp.toPx()
                            ),
                            radius = animatedRadius + 15.dp.toPx()
                        )

                        // Core filled bubble
                        drawCircle(
                            brush = Brush.verticalGradient(
                                colors = listOf(stageColor, stageColor.copy(alpha = 0.7f))
                            ),
                            radius = animatedRadius
                        )
                    }

                    // Numeric seconds in current breath cycle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val roundSecondsLeft = remember(secondsInRoundDouble, currentStageText, inhaleTypeSeconds, holdTypeSeconds, exhaleTypeSeconds) {
                            val doubleInRound = secondsInRoundDouble
                            val inh = inhaleTypeSeconds.toDouble()
                            val hld = holdTypeSeconds.toDouble()
                            when {
                                doubleInRound < inh -> (inh - doubleInRound).toInt() + 1
                                doubleInRound < inh + hld -> ((inh + hld) - doubleInRound).toInt() + 1
                                else -> (roundDuration.toDouble() - doubleInRound).toInt() + 1
                            }
                        }
                        Text(
                            text = if (roundSecondsLeft > 0) roundSecondsLeft.toString() else "1",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "초",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action instruction text
                Text(
                    text = currentStageText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle of patterns
                Text(
                    text = "설정 패턴: 들숨 ${inhaleTypeSeconds}초 - 정지 ${holdTypeSeconds}초 - 날숨 ${exhaleTypeSeconds}초",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            // Bottom session Timer & Controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Remaining overall MM:SS count
                Card(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "남은 시간",
                            tint = Color(0xFFF56565),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "총 남은 시간: %02d:%02d", minutesLeft, secondsLeft),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { elapsedSeconds.toFloat() / totalDurationSeconds.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFF4FD1C5),
                    trackColor = Color(0xFF1E293B)
                )
            }
        }
    }

    // Warning confirmation dialog if they attempt to cancel mid-way
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                isRunning = true
                showExitDialog = false
            },
            title = {
                Text(
                    text = "호흡을 중단하시겠습니까?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "회사 모드에서 완전히 전환되기까지 얼마 남지 않았습니다.\n계속 진행하여 따뜻하고 소중한 아빠모드로 들어가 보세요.",
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        viewModel.saveBreathSession(false) // partially saved as incomplete
                        onBack()
                    }
                ) {
                    Text("네, 중단합니다.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        isRunning = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "계속")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("계속 호흡하기", fontWeight = FontWeight.Bold)
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
