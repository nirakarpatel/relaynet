package com.nmiet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.nmiet.app.data.local.AppDatabase
import com.nmiet.app.data.repository.ContentRepositoryImpl
import com.nmiet.app.navigation.AppNavHost
import com.nmiet.app.nearby.BluetoothTransferManager
import com.nmiet.app.nearby.DemoTransferManager
import com.nmiet.app.ui.screens.home.HomeViewModel
import com.nmiet.app.ui.screens.library.LibraryViewModel
import com.nmiet.app.ui.screens.nearbyshare.NearbyShareViewModel
import com.nmiet.app.ui.screens.settings.SettingsViewModel
import com.nmiet.app.ui.theme.NMIETAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Room Database & DAO
        val database = AppDatabase.getInstance(applicationContext)
        val contentDao = database.contentDao()

        // 2. Initialize Repository
        val repository = ContentRepositoryImpl(contentDao = contentDao)

        // 3. Initialize Transfer Managers with callback to persist received peer content
        val demoTransferManager = DemoTransferManager(
            scope = lifecycleScope,
            onContentReceivedCallback = { items, peerName ->
                repository.saveIncomingPeerContent(items, peerName)
            }
        )

        val bluetoothTransferManager = BluetoothTransferManager(
            context = applicationContext,
            scope = lifecycleScope,
            onContentReceivedCallback = { items, peerName ->
                repository.saveIncomingPeerContent(items, peerName)
            }
        )

        // 4. Initialize ViewModels (constructor injection for prototype MVP)
        val homeViewModel = HomeViewModel(repository)
        val nearbyShareViewModel = NearbyShareViewModel(
            repository = repository,
            demoManager = demoTransferManager,
            bluetoothManager = bluetoothTransferManager
        )
        val libraryViewModel = LibraryViewModel(repository)
        val settingsViewModel = SettingsViewModel(repository, applicationContext)

        // 5. Render App UI
        setContent {
            NMIETAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        repository = repository,
                        homeViewModel = homeViewModel,
                        nearbyShareViewModel = nearbyShareViewModel,
                        libraryViewModel = libraryViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
