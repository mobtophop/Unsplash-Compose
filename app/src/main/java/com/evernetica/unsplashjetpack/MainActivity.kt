package com.evernetica.unsplashjetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.evernetica.unsplashjetpack.nav.NavGraph
import com.evernetica.unsplashjetpack.ui.theme.UnsplashJetpackTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnsplashJetpackTheme {
                NavGraph(navController = rememberNavController())
            }
        }
    }
}