package com.example.hagoitaandroid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hagoitaandroid.R
import com.example.hagoitaandroid.ui.theme.HagoitaandroidTheme


@Composable
fun HomeScreen(
    gameViewModel: GameViewModel, // ViewModelを受け取る
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    // この画面が表示されたら、有無を言わさずリセットを実行する
    LaunchedEffect(Unit) {
        gameViewModel.resetGame()
    }

    // 難易度選択を表示するかどうかのフラグ
    var showDifficultySelect by remember { mutableStateOf(false) }

    // この画面（ホーム）が表示された瞬間に、強制的に音とゲーム状態をリセットする
    androidx.compose.runtime.DisposableEffect(Unit) {
        gameViewModel.resetGame() // 音を止め、ラリー状態を解除する
        onDispose { }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 背景画像 (以前の配置を維持)
        Image(
            painter = painterResource(id = R.drawable.backgrounds1),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 0.dp, y = maxHeight * 0.107f)
                .width(maxWidth)
                .height(maxHeight * 0.677f),
            contentScale = ContentScale.FillBounds
        )

        if (!showDifficultySelect) {
            // 通常のスタートボタン
            Button(
                onClick = { showDifficultySelect = true }, // 押すと難易度選択へ
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Game Start")
            }
        } else {
            // 難易度選択ボタン群
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ラベル
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                Text(
                    text = "難易度を選択してください",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
                DifficultyButton("簡単 ", 0.5f, Color.Green.copy(alpha = 0.5f)) { accuracy ->
                    gameViewModel.setDifficulty(accuracy)
                    gameViewModel.resetGame()
                    onStartClick()
                }
                DifficultyButton("普通 ", 0.7f, Color.Blue.copy(alpha = 0.5f)) { accuracy ->
                    gameViewModel.setDifficulty(accuracy)
                    gameViewModel.resetGame()
                    onStartClick()
                }
                DifficultyButton("難しい ", 0.9f, Color.Magenta.copy(alpha = 0.5f)) { accuracy ->
                    gameViewModel.setDifficulty(accuracy)
                    gameViewModel.resetGame()
                    onStartClick()
                }
                DifficultyButton("神 ", 0.99f, Color.Red.copy(alpha = 0.5f)) { accuracy ->
                    gameViewModel.setDifficulty(accuracy)
                    gameViewModel.resetGame()
                    onStartClick()
                }

                TextButton(onClick = { showDifficultySelect = false }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("戻る")
                }
            }
        }
    }
}

@Composable
fun DifficultyButton(label: String, accuracy: Float, color: Color, onClick: (Float) -> Unit) {
    Button(
        onClick = { onClick(accuracy) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, color = Color.White)
    }
}


