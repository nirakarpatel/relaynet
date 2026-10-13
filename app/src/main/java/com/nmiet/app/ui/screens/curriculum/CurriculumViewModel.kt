package com.nmiet.app.ui.screens.curriculum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.domain.model.Board
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
import kotlinx.coroutines.flow.stateIn

class CurriculumViewModel(
    private val repository: ContentRepository
) : ViewModel() {

    val profile: StateFlow<LearnerProfile> = repository.getLearnerProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LearnerProfile())

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())

    private val _selectedBoard = MutableStateFlow<Board>(Board.ODISHA_STATE)
    val selectedBoard: StateFlow<Board> = _selectedBoard.asStateFlow()

    private val _selectedGrade = MutableStateFlow<Int>(9)
    val selectedGrade: StateFlow<Int> = _selectedGrade.asStateFlow()

    private val _selectedSubject = MutableStateFlow<String>("Science (ବିଜ୍ଞାନ)")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    val learningPacks: StateFlow<List<LearningPack>> = combine(
        repository.getLearningPacksFlow(),
        _selectedBoard,
        _selectedGrade,
        _selectedSubject
    ) { packs, board, grade, subject ->
        packs.filter { pack ->
            (pack.board == board || board == Board.ODISHA_STATE) &&
            (pack.classGrade == grade || grade == 9)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCurriculumNodes: StateFlow<List<CurriculumNode>> = combine(
        repository.getCurriculumNodesFlow(),
        _selectedBoard,
        _selectedGrade
    ) { nodes, board, grade ->
        nodes.filter { node ->
            node.board == board && node.classGrade == grade
        }.ifEmpty { nodes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBoardSelected(board: Board) {
        _selectedBoard.value = board
    }

    fun onGradeSelected(grade: Int) {
        _selectedGrade.value = grade
    }

    fun onSubjectSelected(subject: String) {
        _selectedSubject.value = subject
    }
}
