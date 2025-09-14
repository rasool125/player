package com.example.blutoothapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blutoothapp.Domain.Utils.Screen
import com.example.blutoothapp.Presentation.BluetoothViewModel
import com.example.blutoothapp.Presentation.DevicesScreen
import com.example.blutoothapp.ui.theme.BlutoothAppTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlutoothAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()



                    NavHost (
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        startDestination = Screen.BluetoothHome.route
                    ) {

                        composable(Screen.BluetoothHome.route) {
                            val viewModel = koinViewModel<BluetoothViewModel>()
                            DevicesScreen(navController , viewModel)
                        }

                    }
                }
            }
        }
    }
}

