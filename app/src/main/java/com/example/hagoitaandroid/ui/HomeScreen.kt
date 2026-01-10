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
import com.example.hagoitaandroid.R
import com.example.hagoitaandroid.ui.theme.HagoitaandroidTheme


@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 画像の配置 (402x874基準で y=94, height=592 の比率)
        Image(
            painter = painterResource(id = R.drawable.backgrounds1),
            contentDescription = "スタート画面背景",
            modifier = Modifier
                .offset(x = 0.dp, y = maxHeight * 0.107f)
                .width(maxWidth)
                .height(maxHeight * 0.677f),
            contentScale = ContentScale.FillBounds
        )

        // Game Start ボタン
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Game Start")
        }
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun HomeScreenPreview() {
    HagoitaandroidTheme {
        HomeScreen(onStartClick = {})
    }
}
