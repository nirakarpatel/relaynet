package com.nmiet.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.domain.model.ContentItem
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ContentRepository
) : ViewModel() {

    val categories = listOf("All", "Exams", "Campus Life", "Academics", "Notices", "Research")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncStatus()
        )

    val items: StateFlow<List<ContentItem>> = combine(
        repository.getAllContentFlow(),
        _searchQuery,
        _selectedCategory
    ) { allItems, query, category ->
        filterItems(allItems, query, category)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Automatically ensure initial content is cached on first launch
        viewModelScope.launch {
            repository.refreshContentFromRemote()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }

    fun refreshFeed() {
        viewModelScope.launch {
            repository.refreshContentFromRemote()
        }
    }

    fun toggleSave(id: String) {
        viewModelScope.launch {
            repository.toggleSavedOffline(id)
        }
    }

    companion object {
        fun filterItems(
            items: List<ContentItem>,
            query: String,
            category: String
        ): List<ContentItem> {
            return items.filter { item ->
                val matchesCategory = category.equals("All", ignoreCase = true) ||
                        item.category.equals(category, ignoreCase = true)

                val matchesQuery = query.isBlank() ||
                        item.title.contains(query, ignoreCase = true) ||
                        item.summary.contains(query, ignoreCase = true) ||
                        item.source.contains(query, ignoreCase = true)

                matchesCategory && matchesQuery
            }
        }
    }
}
