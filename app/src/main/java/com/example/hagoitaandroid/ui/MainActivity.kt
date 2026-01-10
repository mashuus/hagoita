package com.example.hagoitaandroid.ui

import android.content.Context
import android.hardware.SensorManager
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
                // ViewModelのインスタンスをここで作成（または取得）
                val gameViewModel: GameViewModel = viewModel()

                // ★ ここにセンサーの初期化処理を追加 ★
                val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                gameViewModel.initSensor(sensorManager)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // HagoitaAppにgameViewModelを渡す
                    HagoitaApp(
                        modifier = Modifier.padding(innerPadding),
                        gameViewModel = gameViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun HagoitaApp(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel() // 引数で受け取るように変更
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start",
        modifier = modifier
    ) {
        composable("start") {
            HomeScreen(
                onStartClick = {
                    gameViewModel.startGame()
                    navController.navigate("play")
                }
            )
        }

        composable("play") {
            GamePlayScreen(
                onNavigateBack = {
                    navController.popBackStack("start", inclusive = false)
                },
                gameViewModel = gameViewModel
            )
        }
    }
}
