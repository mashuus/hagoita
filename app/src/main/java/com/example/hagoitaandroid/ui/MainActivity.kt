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
    // NavController: 画面遷移を管理するコントローラー
    val navController = rememberNavController()

    // ViewModelのインスタンスをここで一度だけ作成し、各画面で共有する
    val gameViewModel: GameViewModel = viewModel()

    // NavHost: 画面の定義とその切り替えルールを記述する場所
    NavHost(
        navController = navController,
        startDestination = "start",
        modifier = modifier
    ) {
        // スタート（ホーム）画面の定義
        composable("start") {
            HomeScreen(
                onStartClick = {
                    // ゲームの状態をリセットして開始する
                    gameViewModel.startGame()
                    // "play" 画面へ移動する
                    navController.navigate("play")
                }
            )
        }

        // ゲームプレイ画面の定義
        composable("play") {
            GamePlayScreen(
                onNavigateBack = {
                    // 勝利/敗北ダイアログで「ホームへ戻る」が押された時、"start" 画面に戻る
                    navController.popBackStack("start", inclusive = false)
                },
                gameViewModel = gameViewModel
            )
        }
    }
}
