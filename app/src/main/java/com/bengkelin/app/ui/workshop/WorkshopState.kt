package com.bengkelin.app.ui.workshop

import com.bengkelin.app.data.model.Workshop

data class WorkshopUiState(
    val selectedCategory: String = "SEMUA", // "SEMUA", "MOTOR", "MOBIL"
    val selectedRegion: String = "SEMUA",   // "SEMUA", "Kota Malang", "Kabupaten Malang", "Kota Batu"
    val searchQuery: String = "",
    val workshops: List<Workshop> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class WorkshopUiEvent {
    data class CategoryChanged(val category: String) : WorkshopUiEvent()
    data class RegionChanged(val region: String) : WorkshopUiEvent()
    data class SearchQueryChanged(val query: String) : WorkshopUiEvent()
    data object RefreshWorkshops : WorkshopUiEvent()
}
