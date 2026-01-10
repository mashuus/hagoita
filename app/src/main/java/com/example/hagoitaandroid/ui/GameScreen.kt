package com.example.hagoitaandroid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlin.random.Random

// 和風カラー定義
val ColorWashi = Color(0xFFFBFaf5)
val ColorVermilion = Color(0xFFD3381C)
val ColorGold = Color(0xFFC5A059)
val ColorBlackInk = Color(0xFF2B2B2B)
val ColorTatami = Color(0xFFE0DCB8)

@Composable
fun GamePlayScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel()
) {
    // インポートエラーを防ぐため collectAsState を明示的に使用
    val uiState by gameViewModel.uiState.collectAsState()

    // 乱数座標を保持するState
    var targetX by remember { mutableFloatStateOf(Random.nextFloat() * 0.6f + 0.2f) }
    var targetY by remember { mutableFloatStateOf(Random.nextFloat() * 0.4f + 0.3f) }
    var ballX by remember { mutableFloatStateOf(Random.nextFloat() * 0.6f + 0.2f) }
    var ballY by remember { mutableFloatStateOf(Random.nextFloat() * 0.4f + 0.3f) }

    Box(
        modifier = modifier
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

            // フィールドコンテナ
            GameFieldContainer {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val w = maxWidth
                    val h = maxHeight

                    // 1. 背景フィールド画像
                    Image(
                        painter = painterResource(id = R.drawable.field),
                        contentDescription = "ゲームフィールド",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    // 2. ターゲット
                    TargetItem(x = targetX, y = targetY, containerWidth = w, containerHeight = h)

                    // 3. ボール
                    BallItem(x = ballX, y = ballY, containerWidth = w, containerHeight = h)

                    // 4. 敵
                    CharacterPawn(
                        x = 0.5f,
                        y = 0.25f,
                        imageResId = R.drawable.ic_enemy_char,
                        containerWidth = w,
                        containerHeight = h,
                        label = "敵"
                    )

                    // 5. 自分
                    CharacterPawn(
                        x = 0.5f,
                        y = 0.75f,
                        imageResId = R.drawable.ic_player_char,
                        containerWidth = w,
                        containerHeight = h,
                        label = "自"
                    )
                }
            }

            PlayerScoreArea(score = uiState.playerScore)
        }

        DebugControlPanel(
            modifier = Modifier.align(Alignment.BottomCenter),
            onPlayerScoreChange = {
                gameViewModel.updatePlayerScore(it)
                targetX = Random.nextFloat() * 0.6f + 0.2f
                targetY = Random.nextFloat() * 0.4f + 0.3f
            },
            onOpponentScoreChange = {
                gameViewModel.updateOpponentScore(it)
                ballX = Random.nextFloat() * 0.6f + 0.2f
                ballY = Random.nextFloat() * 0.4f + 0.3f
            }
        )
    }
}

@Composable
fun BallItem(x: Float, y: Float, containerWidth: Dp, containerHeight: Dp) {
    val ballSize = 60.dp
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
fun CharacterPawn(
    x: Float, y: Float,
    imageResId: Int,
    containerWidth: Dp,
    containerHeight: Dp,
    label: String
) {
    val size = 50.dp
    Box(
        modifier = Modifier
            .size(size)
            .offset(
                x = (containerWidth * x) - (size / 2),
                y = (containerHeight * y) - (size / 2)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = label,
            modifier = Modifier
                .fillMaxSize()
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White, CircleShape),
            contentScale = ContentScale.Fit
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(ColorBlackInk, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
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
        modifier = Modifier
            .size(80.dp)
            .shadow(8.dp, CircleShape),
        shape = CircleShape,
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, ColorGold)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = score.toString(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun ColumnScope.GameFieldContainer(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f) // ここが正しく動作するようになります
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

@Composable
fun DebugControlPanel(
    modifier: Modifier = Modifier,
    onPlayerScoreChange: (Int) -> Unit,
    onOpponentScoreChange: (Int) -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 30.dp, start = 16.dp, end = 16.dp),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DebugCounter(label = "相手", onIncrement = { onOpponentScoreChange(1) }, onDecrement = { onOpponentScoreChange(-1) })
            HorizontalDivider(modifier = Modifier
                .height(40.dp)
                .width(1.dp))
            DebugCounter(label = "自分", onIncrement = { onPlayerScoreChange(1) }, onDecrement = { onPlayerScoreChange(-1) })
        }
    }
}

@Composable
fun DebugCounter(
    label: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$label (Debug)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) { Icon(Icons.Default.Remove, contentDescription = "減らす") }
            IconButton(onClick = onIncrement) { Icon(Icons.Default.Add, contentDescription = "増やす") }
        }
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun GamePlayScreenPreview() {
    HagoitaandroidTheme {
        GamePlayScreen()
    }
}
