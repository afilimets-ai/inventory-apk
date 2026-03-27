package com.inventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for the Inventory app.
 * This is the entry point activity that is launched when the app starts.
 * It serves as the primary interface for user interaction.
 * Annotated with @AndroidEntryPoint to enable Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Layout will be set when activity_main.xml is created
        // setContentView(R.layout.activity_main)
    }
}
