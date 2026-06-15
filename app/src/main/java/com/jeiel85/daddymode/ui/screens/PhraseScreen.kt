package com.jeiel85.daddymode.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeiel85.daddymode.data.model.DadAction
import com.jeiel85.daddymode.ui.viewmodel.DadModeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhraseScreen(
    viewModel: DadModeViewModel,
    onBackToHome: () -> Unit
) {
    val phrases by viewModel.phrases.collectAsStateWithLifecycle()
    val activeActions by viewModel.activeActions.collectAsStateWithLifecycle()

    var selectedPhrase by remember { mutableStateOf("") }
    var selectedActionId by remember { mutableIntStateOf(-1) }

    // Auto-select first elements if available
    LaunchedEffect(phrases, activeActions) {
        if (selectedPhrase.isEmpty() && phrases.isNotEmpty()) {
            selectedPhrase = phrases.first()
        }
        if (selectedActionId == -1 && activeActions.isNotEmpty()) {
            selectedActionId = activeActions.first().id
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("3단계: 오늘 저녁 사랑 한마디 & 행동 약속", fontWeight = FontWeight.Bold) }
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
            // Screen Guide Header
            item {
                Text(
                    text = "집 문을 열고 들어가기 바로 전, 오늘 저녁 아내와 아이에게 부드럽게 던질 한마디와 작은 행동을 약속해 주세요.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 24.sp
                )
            }

            // Step A: Choose warm phrase
            item {
                Text(
                    text = "A. 문 열며 건넬 첫마디 선택",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(phrases) { phrase ->
                val isSelected = selectedPhrase == phrase
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPhrase = phrase },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = borderForSelection(isSelected)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedPhrase = phrase }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = phrase,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            }

            // Step B: Choose micro action
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "B. 오늘 실천할 아빠행동 선택",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (activeActions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("사용 가능한 실천 행동이 없습니다. 설정에서 등록하세요.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(activeActions) { action ->
                    val isSelected = selectedActionId == action.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedActionId = action.id },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = borderForSelection(isSelected)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedActionId = action.id },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.secondary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 17.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Full transition Save button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (selectedPhrase.isNotEmpty() && selectedActionId != -1) {
                            viewModel.saveTodayPhraseAndAction(selectedPhrase, selectedActionId)
                        }
                        onBackToHome()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    enabled = selectedPhrase.isNotEmpty() && selectedActionId != -1
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "전환 완료",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "아빠모드 활성화 완료! 집으로 가기 🏡",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun borderForSelection(selected: Boolean) = if (selected) {
    borderSelection(MaterialTheme.colorScheme.primary)
} else {
    borderSelection(Color.Transparent)
}

@Composable
fun borderSelection(color: Color) = if (color != Color.Transparent) {
    androidx.compose.foundation.BorderStroke(2.dp, color)
} else {
    null
}
