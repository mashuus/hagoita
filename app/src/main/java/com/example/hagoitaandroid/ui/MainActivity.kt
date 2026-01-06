package com.example.hagoitaandroid.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hagoitaandroid.ui.theme.HagoitaandroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HagoitaandroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HagoitaApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HagoitaApp(modifier: Modifier = Modifier) {
    // NavController: 画面遷移を命令するコントローラー
    val navController = rememberNavController()

    // ViewModelのインスタンスをここで一度だけ作成し、各画面で共有する
    val gameViewModel: GameViewModel = viewModel()

    // NavHost: 現在表示すべき画面を描画する場所
    NavHost(
        navController = navController,
        startDestination = "start",
        modifier = modifier
    ) {
        // composable("ルート名") { 表示する画面のUI } という形で画面を定義する
        composable("start") {
            // スタート画面を表示 (UIの定義はGameScreen.ktにある)
            GameStartScreen(
                // スタート画面がクリックされたら、以下の処理を実行する
                onStartClick = {
                    gameViewModel.startGame() // ViewModelのゲーム開始関数を呼ぶ
                    navController.navigate("play") // "play"画面に遷移する
                }
            )
        }

        composable("play") {
            // ゲームプレイ画面を表示 (UIの定義はGameScreen.ktにある)
            // HagoitaAppで作成したViewModelを渡す
            GamePlayScreen(gameViewModel = gameViewModel)
        }
    }
}