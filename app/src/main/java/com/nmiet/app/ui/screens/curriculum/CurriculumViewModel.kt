package com.nmiet.app.ui.screens.curriculum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.data.local.LanguageManager
import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.data.repository.UserPreferencesRepository
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.ContentOrigin
import com.nmiet.app.domain.model.CurriculumNode
import com.nmiet.app.domain.model.LearnerProfile
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ContentBucketFilter {
    DOWNLOADED, // Directly downloaded from cloud gateway
    SHARED      // P2P transferred or received via Bluetooth RFCOMM / ShikshaMesh
}

class CurriculumViewModel(
    private val repository: ContentRepository,
    val languageManager: LanguageManager? = null,
    private val userPreferencesRepository: UserPreferencesRepository? = null
) : ViewModel() {

    val profile: StateFlow<LearnerProfile> = userPreferencesRepository?.learnerProfile
        ?: repository.getLearnerProfileFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LearnerProfile())

    val currentLanguage: StateFlow<AppLanguage> = languageManager?.currentLanguage
        ?: profile.map { it.languageMode }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.BILINGUAL_EN_HI)

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())

    private val _selectedBucket = MutableStateFlow(ContentBucketFilter.DOWNLOADED)
    val selectedBucket: StateFlow<ContentBucketFilter> = _selectedBucket.asStateFlow()

    private val _selectedBoard = MutableStateFlow<Board>(Board.ODISHA_STATE)
    val selectedBoard: StateFlow<Board> = _selectedBoard.asStateFlow()

    private val _selectedGrade = MutableStateFlow<Int>(9)
    val selectedGrade: StateFlow<Int> = _selectedGrade.asStateFlow()

    private val _selectedSubject = MutableStateFlow<String>("Science (ବିଜ୍ଞାନ)")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    val downloadedPacksCount: StateFlow<Int> = combine(
        repository.getLearningPacksFlow(),
        profile
    ) { all, prof ->
        val targetGradeInt = prof.classGrade.removePrefix("Class ").trim().toIntOrNull() ?: 9
        val targetBoardName = prof.educationalBoard
        val count = all.count { pack ->
            pack.origin in listOf(ContentOrigin.CLOUD_DOWNLOADED, ContentOrigin.INTERNET_DOWNLOADED) &&
                !pack.isReceivedViaMesh &&
                pack.board.name.equals(targetBoardName, ignoreCase = true) &&
                (pack.classGrade == targetGradeInt || "Class ${pack.classGrade}".equals(prof.classGrade, ignoreCase = true))
        }
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val sharedPacksCount: StateFlow<Int> = combine(
        repository.getLearningPacksFlow(),
        profile
    ) { all, prof ->
        val targetGradeInt = prof.classGrade.removePrefix("Class ").trim().toIntOrNull() ?: 9
        val targetBoardName = prof.educationalBoard
        val count = all.count { pack ->
            (pack.origin in listOf(ContentOrigin.P2P_RECEIVED, ContentOrigin.P2P_SENT) || pack.isReceivedViaMesh) &&
                pack.board.name.equals(targetBoardName, ignoreCase = true) &&
                (pack.classGrade == targetGradeInt || "Class ${pack.classGrade}".equals(prof.classGrade, ignoreCase = true))
        }
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val filteredPacks: StateFlow<List<LearningPack>> = combine(
        repository.getLearningPacksFlow(),
        profile,
        _selectedBucket
    ) { allPacks, prof, bucket ->
        val targetGradeInt = prof.classGrade.removePrefix("Class ").trim().toIntOrNull() ?: 9
        val targetBoardName = prof.educationalBoard

        allPacks.filter { pack ->
            val matchesBucket = when (bucket) {
                ContentBucketFilter.DOWNLOADED ->
                    pack.origin in listOf(ContentOrigin.CLOUD_DOWNLOADED, ContentOrigin.INTERNET_DOWNLOADED) && !pack.isReceivedViaMesh
                ContentBucketFilter.SHARED ->
                    pack.origin in listOf(ContentOrigin.P2P_RECEIVED, ContentOrigin.P2P_SENT) || pack.isReceivedViaMesh
            }
            val matchesBoard = pack.board.name.equals(targetBoardName, ignoreCase = true)
            val matchesGrade = (pack.classGrade == targetGradeInt || "Class ${pack.classGrade}".equals(prof.classGrade, ignoreCase = true))
            matchesBucket && matchesBoard && matchesGrade
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Backwards compatibility
    val learningPacks: StateFlow<List<LearningPack>> = filteredPacks

    val allCurriculumNodes: StateFlow<List<CurriculumNode>> = combine(
        repository.getCurriculumNodesFlow(),
        profile
    ) { nodes, prof ->
        val targetGradeInt = prof.classGrade.removePrefix("Class ").trim().toIntOrNull() ?: 9
        val targetBoardName = prof.educationalBoard
        nodes.filter { node ->
            node.board.name.equals(targetBoardName, ignoreCase = true) && node.classGrade == targetGradeInt
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBucketSelected(bucket: ContentBucketFilter) {
        _selectedBucket.value = bucket
    }

    fun onBoardSelected(board: Board) {
        _selectedBoard.value = board
    }

    fun onGradeSelected(grade: Int) {
        _selectedGrade.value = grade
    }

    fun onSubjectSelected(subject: String) {
        _selectedSubject.value = subject
    }

    fun seedDemoSharedPack() {
        viewModelScope.launch {
            val sampleShared = PilotCurriculumCatalog.getSampleSharedPack()
            repository.saveIncomingLearningPack(sampleShared, "Rahul-Phone (Nearby Peer)")
        }
    }
}
