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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
                val gameViewModel: GameViewModel = viewModel()

                // センサーと音声の初期化
                val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                gameViewModel.initSystem(applicationContext, sensorManager)

                // 画面が閉じられたり、アプリが裏に回った時に音を強制停止する
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                            gameViewModel.resetGame()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
    gameViewModel: GameViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start",
        modifier = modifier
    ) {
        composable("start") {
            DisposableEffect(Unit) {
                gameViewModel.resetGame()
                onDispose {}
            }
            HomeScreen(
                gameViewModel = gameViewModel,
                onStartClick = {
                    gameViewModel.startGame()
                    navController.navigate("play")
                }
            )
        }

        composable("play") {
            GamePlayScreen(
                onNavigateBack = {
                    gameViewModel.resetGame()
                    navController.popBackStack("start", inclusive = false)
                },
                gameViewModel = gameViewModel
            )
        }
    }
}
