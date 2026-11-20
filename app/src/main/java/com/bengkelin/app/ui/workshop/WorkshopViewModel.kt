package com.bengkelin.app.ui.workshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bengkelin.app.data.model.Resource
import com.bengkelin.app.data.repository.WorkshopRepository
import com.bengkelin.app.data.repository.WorkshopRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkshopViewModel(
    private val repository: WorkshopRepository = WorkshopRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkshopUiState())
    val uiState: StateFlow<WorkshopUiState> = _uiState.asStateFlow()

    init {
        loadWorkshops()
    }

    fun onEvent(event: WorkshopUiEvent) {
        when (event) {
            is WorkshopUiEvent.CategoryChanged -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
                loadWorkshops()
            }
            is WorkshopUiEvent.RegionChanged -> {
                _uiState.update { it.copy(selectedRegion = event.region) }
                loadWorkshops()
            }
            is WorkshopUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                loadWorkshops()
            }
            is WorkshopUiEvent.RefreshWorkshops -> {
                loadWorkshops()
            }
        }
    }

    private fun loadWorkshops() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.getNearbyWorkshops(
                category = state.selectedCategory,
                region = state.selectedRegion,
                searchQuery = state.searchQuery
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, workshops = resource.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                    is Resource.Idle -> Unit
                }
            }
        }
    }
}
