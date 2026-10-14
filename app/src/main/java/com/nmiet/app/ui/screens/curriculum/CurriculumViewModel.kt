package com.nmiet.app.ui.screens.curriculum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.data.local.LanguageManager
import com.nmiet.app.data.remote.PilotCurriculumCatalog
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
    val languageManager: LanguageManager? = null
) : ViewModel() {

    val currentLanguage: StateFlow<AppLanguage> = languageManager?.currentLanguage
        ?: repository.getLearnerProfileFlow()
            .map { it.preferredLanguage }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.BILINGUAL_EN_OR)

    val profile: StateFlow<LearnerProfile> = repository.getLearnerProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LearnerProfile())

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

    val downloadedPacksCount: StateFlow<Int> = repository.getDownloadedPacksFlow()
        .combine(repository.getLearningPacksFlow()) { downloaded, all ->
            val count = all.count { it.origin in listOf(ContentOrigin.CLOUD_DOWNLOADED, ContentOrigin.INTERNET_DOWNLOADED) && !it.isReceivedViaMesh }
            if (count > 0) count else downloaded.size
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val sharedPacksCount: StateFlow<Int> = repository.getSharedPacksFlow()
        .combine(repository.getLearningPacksFlow()) { shared, all ->
            val count = all.count { it.origin in listOf(ContentOrigin.P2P_RECEIVED, ContentOrigin.P2P_SENT) || it.isReceivedViaMesh }
            if (count > 0) count else shared.size
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val filteredPacks: StateFlow<List<LearningPack>> = combine(
        repository.getLearningPacksFlow(),
        _selectedBucket,
        _selectedBoard,
        _selectedGrade,
        _selectedSubject
    ) { allPacks, bucket, board, grade, _ ->
        allPacks.filter { pack ->
            val matchesBucket = when (bucket) {
                ContentBucketFilter.DOWNLOADED ->
                    pack.origin in listOf(ContentOrigin.CLOUD_DOWNLOADED, ContentOrigin.INTERNET_DOWNLOADED) && !pack.isReceivedViaMesh
                ContentBucketFilter.SHARED ->
                    pack.origin in listOf(ContentOrigin.P2P_RECEIVED, ContentOrigin.P2P_SENT) || pack.isReceivedViaMesh
            }
            val matchesBoard = (pack.board == board || board == Board.ODISHA_STATE)
            val matchesGrade = (pack.classGrade == grade || grade == 9)
            matchesBucket && matchesBoard && matchesGrade
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Backwards compatibility
    val learningPacks: StateFlow<List<LearningPack>> = filteredPacks

    val allCurriculumNodes: StateFlow<List<CurriculumNode>> = combine(
        repository.getCurriculumNodesFlow(),
        _selectedBoard,
        _selectedGrade
    ) { nodes, board, grade ->
        nodes.filter { node ->
            node.board == board && node.classGrade == grade
        }.ifEmpty { nodes }
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
