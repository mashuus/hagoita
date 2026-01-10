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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hagoitaandroid.R
import com.example.hagoitaandroid.ui.theme.HagoitaandroidTheme

// 和風カラー定義
val ColorWashi = Color(0xFFFBFaf5) // 和紙っぽい白
val ColorVermilion = Color(0xFFD3381C) // 朱色
val ColorGold = Color(0xFFC5A059) // 金色
val ColorBlackInk = Color(0xFF2B2B2B) // 墨色
val ColorTatami = Color(0xFFE0DCB8) // 畳っぽい色（フィールド背景用）

@Composable
fun GamePlayScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel()
) {
    val uiState by gameViewModel.uiState.collectAsState()

    // 全体の背景（和紙のような質感やグラデーションをイメージ）
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
            // 1. 相手のエリア（スコア表示）
            OpponentScoreArea(score = uiState.opponentScore)

            // 2. ゲームフィールド（中央）
            // 以前の比率ロジックを維持しつつ、見た目を豪華にする枠を追加
            GameFieldContainer {
                Image(
                    painter = painterResource(id = R.drawable.field),
                    contentDescription = "ゲームフィールド",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }

            // 3. 自身のエリア（スコア表示）
            PlayerScoreArea(score = uiState.playerScore)
        }

        // 4. デバッグ用コントロールパネル（画面下部に配置）
        DebugControlPanel(
            modifier = Modifier.align(Alignment.BottomCenter),
            onPlayerScoreChange = { gameViewModel.updatePlayerScore(it) },
            onOpponentScoreChange = { gameViewModel.updateOpponentScore(it) }
        )
    }
}

// ----------------------------------------------------------------
// サブコンポーネント群
// ----------------------------------------------------------------

@Composable
fun OpponentScoreArea(score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "相手",
            style = MaterialTheme.typography.titleMedium,
            color = ColorBlackInk
        )
        ScoreBoard(score = score, containerColor = ColorBlackInk, contentColor = ColorGold)
    }
}

@Composable
fun PlayerScoreArea(score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ScoreBoard(score = score, containerColor = ColorVermilion, contentColor = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "あなた",
            style = MaterialTheme.typography.titleMedium,
            color = ColorBlackInk
        )
    }
}

@Composable
fun ScoreBoard(score: Int, containerColor: Color, contentColor: Color) {
    // 点数を表示する「木札」や「円」のようなデザイン
    Surface(
        modifier = Modifier
            .size(80.dp)
            .shadow(8.dp, CircleShape),
        shape = CircleShape,
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, ColorGold)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = score.toString(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun ColumnScope.GameFieldContainer(
    content: @Composable () -> Unit
) {
    // フィールドを囲む雅な枠
    // weight(1f)を使って、上下のスコア表示以外の空間を埋める
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 16.dp)
            .fillMaxWidth(0.9f) // 横幅の90%を使う
            .shadow(12.dp, RoundedCornerShape(8.dp))
            .background(ColorTatami) // フィールド画像の背景漏れ防止
            .border(4.dp, ColorVermilion, RoundedCornerShape(8.dp)) // 朱色の枠
            .border(8.dp, ColorBlackInk, RoundedCornerShape(8.dp)) // 内側の黒枠
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        // 元の画像のアスペクト比や配置を尊重したい場合はここにロジックを入れる
        // 今回はコンテナいっぱいに広げる形にしています
        content()

        // ネット（中央線）の装飾例（画像に線がない場合用、あるなら不要）
        // Box(
        //    modifier = Modifier
        //        .fillMaxWidth()
        //        .height(2.dp)
        //        .background(Color.White.copy(alpha = 0.5f))
        // )
    }
}

@Composable
fun DebugControlPanel(
    modifier: Modifier = Modifier,
    onPlayerScoreChange: (Int) -> Unit,
    onOpponentScoreChange: (Int) -> Unit
) {
    // デバッグ用の操作パネル（少し透けさせる）
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
            Divider(modifier = Modifier.height(40.dp).width(1.dp))
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
            IconButton(onClick = onDecrement) {
                Icon(Icons.Default.Remove, contentDescription = "減らす")
            }
            IconButton(onClick = onIncrement) {
                Icon(Icons.Default.Add, contentDescription = "増やす")
            }
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