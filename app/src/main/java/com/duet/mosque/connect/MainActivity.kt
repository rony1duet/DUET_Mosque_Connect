package com.duet.mosque.connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duet.mosque.connect.ui.screens.MainAppContainer
import com.duet.mosque.connect.ui.theme.MyApplicationTheme
import com.duet.mosque.connect.ui.viewmodel.MosqueViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MosqueViewModel = viewModel()
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContainer(viewModel = viewModel)
                }
            }
        }
    }
}
