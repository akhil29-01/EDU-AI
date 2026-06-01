package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.AppNavigationWrapper
import com.example.ui.AppViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Simple Constructor Injection ViewModel Provider initialization
    val viewModel = ViewModelProvider(this)[AppViewModel::class.java]
    
    setContent {
      MyApplicationTheme {
        AppNavigationWrapper(viewModel = viewModel)
      }
    }
  }
}

