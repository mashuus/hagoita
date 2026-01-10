package com.example.hagoitaandroid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hagoitaandroid.R
import com.example.hagoitaandroid.ui.theme.HagoitaandroidTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// カラー定義
val ColorWashi = Color(0xFFFBFaf5)
val ColorVermilion = Color(0xFFD3381C)
val ColorGold = Color(0xFFC5A059)
val ColorBlackInk = Color(0xFF2B2B2B)
val ColorTatami = Color(0xFFE0DCB8)

@Composable
fun GamePlayScreen(
    onNavigateBack: () -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    val uiState by gameViewModel.uiState.collectAsState()

    var showScoreEffect by remember { mutableStateOf(false) }
    var effectText by remember { mutableStateOf("") }     // 表示する文字
    var effectColor by remember { mutableStateOf(ColorVermilion) } // 文字の色

    // スコアの変更を検知するために、直前のスコアを覚えておく
    var prevPlayerScore by remember { mutableIntStateOf(uiState.playerScore) }
    var prevOpponentScore by remember { mutableIntStateOf(uiState.opponentScore) }

    // 得点/失点時にエフェクトを起動
    LaunchedEffect(uiState.playerScore, uiState.opponentScore) {
        // ゲームオーバー時はダイアログが出るので、それ以外の時に判定
        if (!uiState.isGameOver) {
            if (uiState.playerScore > prevPlayerScore) {
                // 自分のスコアが増えた場合
                effectText = "一本！"
                effectColor = ColorVermilion // 情熱の赤
                showScoreEffect = true
            } else if (uiState.opponentScore > prevOpponentScore) {
                // 相手のスコアが増えた場合（＝自分の失点）
                effectText = "失点..."
                effectColor = Color.Gray      // 哀愁のグレー
                showScoreEffect = true
            }

            if (showScoreEffect) {
                kotlinx.coroutines.delay(1200) // 1.2秒間表示
                showScoreEffect = false
            }
        }
        // 次の判定のためにスコアを更新
        prevPlayerScore = uiState.playerScore
        prevOpponentScore = uiState.opponentScore
    }

    if (uiState.isGameOver) {
        // 勝利ダイアログ
        AlertDialog(
            onDismissRequest = { },
            title = { Text("試合終了") },
            text = { Text(uiState.winnerLabel) },
            confirmButton = {
                Button(onClick = onNavigateBack) { Text("ホームへ戻る") }
            }
        )
    }

    // 背景レイアウト
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ColorWashi, Color(0xFFEDE4CD))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            OpponentScoreArea(score = uiState.opponentScore)

            GameFieldContainer {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val w = maxWidth
                    val h = maxHeight

                    // 1. 背景フィールド画像と中央線
                    Image(
                        painter = painterResource(id = R.drawable.field),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                                drawLine(
                                    color = Color.White.copy(alpha = 0.5f),
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    strokeWidth = 3.dp.toPx(),
                                    pathEffect = pathEffect
                                )
                            },
                        contentScale = ContentScale.FillBounds
                    )

                    // キャラクター・ボール等の描画
                    // ターゲット: ラリー中のみ表示
                    if (uiState.isRallyActive) {
                        uiState.targetPos?.let { pos ->
                            TargetItem(x = pos.x, y = pos.y, containerWidth = w, containerHeight = h)
                        }
                    }

                    // ボール: ラリー中のみ表示
                    if (uiState.isRallyActive) {
                        BallItem(
                            x = uiState.ballPos.x,
                            y = uiState.ballPos.y,
                            containerWidth = w,
                            containerHeight = h
                        )
                    }
                    CharacterPawn(uiState.enemyPos.x, uiState.enemyPos.y, R.drawable.ic_enemy_char, w, h, "敵")
                    CharacterPawn(uiState.playerPos.x, uiState.playerPos.y, R.drawable.ic_player_char, w, h, "自")

                    ScoreEffectOverlay(
                        visible = showScoreEffect,
                        text = effectText,
                        color = effectColor
                    )
                }
            }

            PlayerScoreArea(score = uiState.playerScore)

            Button(
                onClick = { gameViewModel.onPlayerAction() },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("スイング！", fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun BoxScope.ScoreEffectOverlay(visible: Boolean, text: String, color: Color) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f),
        exit = fadeOut(animationSpec = tween(500)),
        modifier = Modifier.align(Alignment.Center)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text, // 動的にテキストを切り替え
                style = MaterialTheme.typography.displayMedium,
                color = color, // 動的に色を切り替え
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun BallItem(x: Float, y: Float, containerWidth: Dp, containerHeight: Dp) {
    val ballSize = 30.dp
    Box(
        modifier = Modifier
            .size(ballSize)
            .offset(
                x = (containerWidth * x) - (ballSize / 2),
                y = (containerHeight * y) - (ballSize / 2)
            )
            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            .clip(CircleShape)
    )
}

@Composable
fun TargetItem(x: Float, y: Float, containerWidth: Dp, containerHeight: Dp) {
    val targetSize = 140.dp
    Box(
        modifier = Modifier
            .size(targetSize)
            .offset(
                x = (containerWidth * x) - (targetSize / 2),
                y = (containerHeight * y) - (targetSize / 2)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = ColorGold.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.White, CircleShape)
                .border(3.dp, ColorVermilion, CircleShape)
        )
    }
}

@Composable
fun CharacterPawn(x: Float, y: Float, imageResId: Int, containerWidth: Dp, containerHeight: Dp, label: String) {
    val size = 50.dp
    Box(
        modifier = Modifier
            .size(size)
            .offset(x = (containerWidth * x) - (size / 2), y = (containerHeight * y) - (size / 2)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = label,
            modifier = Modifier.fillMaxSize().shadow(4.dp, CircleShape).clip(CircleShape).background(Color.White, CircleShape),
            contentScale = ContentScale.Fit
        )
        Box(
            modifier = Modifier.align(Alignment.BottomEnd).background(ColorBlackInk, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(text = label, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OpponentScoreArea(score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "相手", style = MaterialTheme.typography.titleMedium, color = ColorBlackInk)
        ScoreBoard(score = score, containerColor = ColorBlackInk, contentColor = ColorGold)
    }
}

@Composable
fun PlayerScoreArea(score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ScoreBoard(score = score, containerColor = ColorVermilion, contentColor = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "あなた", style = MaterialTheme.typography.titleMedium, color = ColorBlackInk)
    }
}

@Composable
fun ScoreBoard(score: Int, containerColor: Color, contentColor: Color) {
    Surface(
        modifier = Modifier.size(80.dp).shadow(8.dp, CircleShape),
        shape = CircleShape, color = containerColor, border = androidx.compose.foundation.BorderStroke(2.dp, ColorGold)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = score.toString(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun ColumnScope.GameFieldContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 16.dp)
            .fillMaxWidth(0.9f)
            .shadow(12.dp, RoundedCornerShape(8.dp))
            .background(ColorTatami)
            .border(4.dp, ColorVermilion, RoundedCornerShape(8.dp))
            .border(8.dp, ColorBlackInk, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun GamePlayScreenPreview() {
    HagoitaandroidTheme {
        GamePlayScreen(onNavigateBack = {})
    }
}
