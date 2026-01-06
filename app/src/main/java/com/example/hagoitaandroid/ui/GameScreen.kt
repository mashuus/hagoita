// ファイルパス: app/src/main/java/com/example/hagoitaandroid/ui/GameScreen.kt
package com.example.hagoitaandroid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hagoitaandroid.R
import com.example.hagoitaandroid.ui.theme.HagoitaandroidTheme

// --- ▼▼▼ GameStartScreen を修正 ▼▼▼ ---
@Composable
fun GameStartScreen(
    // 引数を onScreenClick から onStartClick に変更して分かりやすくする
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Column を使って、画像とボタンを縦に並べる
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // 水平方向中央揃え
        verticalArrangement = Arrangement.SpaceAround // 垂直方向に均等配置
    ) {
        // 上のロゴ（仮）
        Text("Smash Phone")

        // 羽子板の画像
        Image(
            painter = painterResource(id = R.drawable.frame1),
            contentDescription = "スタート画面の羽子板",
            modifier = Modifier.weight(1f), // ボタン以外の残りのスペースを埋める
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp)) // 画像とボタンの間にスペースを空ける

        // Game Start ボタン
        Button(
            onClick = onStartClick, // ボタンがクリックされたら、渡された処理を実行
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Game Start")
        }
    }
}

// --- ▼▼▼ GamePlayScreen を修正 ▼▼▼ ---
@Composable
fun GamePlayScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel()
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // ゲームプレイ画面では frame2.png を表示する
        Image(
            painter = painterResource(id = R.drawable.frame2),
            contentDescription = "プレイ中の羽子板",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}


// --- プレビュー用のコード ---
@Preview(showBackground = true)
@Composable
private fun GameStartScreenPreview() {
    HagoitaandroidTheme {
        GameStartScreen(onStartClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun GamePlayScreenPreview() {
    HagoitaandroidTheme {
        GamePlayScreen()
    }
}

