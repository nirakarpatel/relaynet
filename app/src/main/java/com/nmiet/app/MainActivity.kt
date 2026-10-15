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
import com.nmiet.app.data.local.LanguageManager
import com.nmiet.app.data.repository.ContentRepositoryImpl
import com.nmiet.app.navigation.AppNavHost
import com.nmiet.app.nearby.BluetoothTransferManager
import com.nmiet.app.nearby.DemoTransferManager
import com.nmiet.app.ui.screens.curriculum.CurriculumViewModel
import com.nmiet.app.ui.screens.mastery.MasteryViewModel
import com.nmiet.app.ui.screens.nearbyshare.NearbyShareViewModel
import com.nmiet.app.ui.screens.settings.SettingsViewModel
import com.nmiet.app.ui.screens.tutor.AITutorViewModel
import com.nmiet.app.ui.theme.NMIETAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 0. Initialize Firebase with project credentials if uninitialized
        com.nmiet.app.auth.FirebaseAuthManager.initialize(applicationContext)

        // 1. Initialize SQLite Database & Learning DAO
        val database = AppDatabase.getInstance(applicationContext)
        val learningDao = database.learningDao()

        // 2. Initialize Learning Platform Repository
        val repository = ContentRepositoryImpl(
            learningDao = learningDao,
            archiveDir = applicationContext.cacheDir
        )

        // Preload pilot catalog data and Brain Learning Pack if empty or absent
        lifecycleScope.launch(Dispatchers.IO) {
            if (learningDao.countPacks() == 0) {
                repository.resetToPilotDemoPacks()
            }
            com.nmiet.app.data.local.entities.BrainPackSeed.seedBrainPackIntoDatabase(learningDao, applicationContext)
        }

        // 3. Initialize Transfer Managers with pack & note ingestion callback
        val demoTransferManager = DemoTransferManager(
            scope = lifecycleScope,
            onPackReceivedCallback = { pack, peerName ->
                repository.saveIncomingLearningPack(pack, peerName)
            },
            onNoteReceivedCallback = { note, peerName ->
                repository.saveGeneratedNote(note)
            }
        )

        val bluetoothTransferManager = BluetoothTransferManager(
            context = applicationContext,
            scope = lifecycleScope,
            onPackReceivedCallback = { pack, peerName ->
                repository.saveIncomingLearningPack(pack, peerName)
            },
            onNoteReceivedCallback = { note, peerName ->
                repository.saveGeneratedNote(note)
            },
            packLookupProvider = { packId ->
                repository.getPackByIdOnce(packId)
            }
        )

        val googleNearbyManager = com.nmiet.app.nearby.GoogleNearbyConnectionsManager(
            context = applicationContext,
            repository = repository,
            scope = lifecycleScope,
            onPackReceivedCallback = { pack, peerName ->
                repository.saveIncomingLearningPack(pack, peerName)
            },
            onNoteReceivedCallback = { note, peerName ->
                // Single persistence ownership: GoogleNearbyConnectionsManager directly validates and
                // persists the note into Room before dispatching the ACK.
                // This callback is strictly for UI/observer notifications.
            }
        )

        // 4. Initialize Core ViewModels & Language Engine
        val languageManager = LanguageManager.getInstance(applicationContext)
        val userPreferencesRepository = com.nmiet.app.data.repository.UserPreferencesRepository.getInstance(applicationContext)
        val curriculumViewModel = CurriculumViewModel(repository, languageManager, userPreferencesRepository)
        val aiTutorViewModel = AITutorViewModel(repository, languageManager)
        val nearbyShareViewModel = NearbyShareViewModel(
            repository = repository,
            demoManager = demoTransferManager,
            bluetoothManager = bluetoothTransferManager,
            nearbyManager = googleNearbyManager,
            languageManager = languageManager
        )
        val masteryViewModel = MasteryViewModel(repository, languageManager)
        val settingsViewModel = SettingsViewModel(repository, applicationContext, languageManager, userPreferencesRepository)
        val geminiKeyRotator = com.nmiet.app.data.remote.GeminiKeyRotator.getInstance()

        // 5. Mount App Navigation & UI
        setContent {
            NMIETAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        repository = repository,
                        curriculumViewModel = curriculumViewModel,
                        aiTutorViewModel = aiTutorViewModel,
                        nearbyShareViewModel = nearbyShareViewModel,
                        masteryViewModel = masteryViewModel,
                        settingsViewModel = settingsViewModel,
                        languageManager = languageManager,
                        geminiKeyRotator = geminiKeyRotator
                    )
                }
            }
        }
    }
}
