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
        composable("start") {
            HomeScreen(
                onStartClick = {
                    navController.navigate("play")
                }
            )
        }


        composable("play") {
            GamePlayScreen(gameViewModel = gameViewModel)
        }
    }
}