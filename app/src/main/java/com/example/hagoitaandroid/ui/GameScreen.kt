// ファイルパス: app/src/main/java/com/example/hagoitaandroid/ui/GameScreen.kt
package com.example.hagoitaandroid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hagoitaandroid.R
import com.example.hagoitaandroid.ui.theme.HagoitaandroidTheme

@Composable
fun GameStartScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // BoxWithConstraints で画面全体を囲む
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // this スコープ内で maxWidth や maxHeight が使えるようになる
        // (画面の最大幅と最大高さが dp 単位で取得できる)

        // figure1.png を配置
        Image(
            painter = painterResource(id = R.drawable.figure1),
            contentDescription = "Figure 1",
            modifier = Modifier
                // 位置：(maxWidth * 0.16, maxHeight * 0.10)
                .offset(x = maxWidth * 0.16f, y = maxHeight * 0.10f)
                // 大きさ：幅を画面の68%、高さを画面の12%
                .width(maxWidth * 0.68f)
                .height(maxHeight * 0.12f),
            contentScale = ContentScale.FillBounds // 指定サイズに合わせる
        )

        // figure2.png を配置
        Image(
            painter = painterResource(id = R.drawable.figure2),
            contentDescription = "Figure 2",
            modifier = Modifier
                // 位置：(0, maxHeight * 0.23)
                .offset(x = 0.dp, y = maxHeight * 0.23f)
                // 大きさ：幅を画面いっぱい、高さを画面の55%
                .width(maxWidth)
                .height(maxHeight * 0.55f),
            contentScale = ContentScale.FillBounds
        )

        // Game Start ボタンを画面下部に配置
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .align(Alignment.BottomCenter) // 親(Box)の真下に配置
                .padding(bottom = 32.dp)       // 画面下から少し浮かせる
                .fillMaxWidth()
                .padding(horizontal = 16.dp)   // 左右に余白を持たせる
        ) {
            Text("Game Start")
        }
    }
}


// --- GamePlayScreen は変更なし ---
@Composable
fun GamePlayScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel()
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.frame2),
            contentDescription = "プレイ中の羽子板",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// --- プレビュー用のコード ---
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
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

