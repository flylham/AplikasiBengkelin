package com.bengkelin.app.data.repository

import com.bengkelin.app.data.model.Resource
import com.bengkelin.app.data.model.Workshop
import kotlinx.coroutines.flow.Flow

interface WorkshopRepository {

    suspend fun getNearbyWorkshops(
        category: String? = null,
        region: String? = null,
        searchQuery: String? = null
    ): Flow<Resource<List<Workshop>>>
}
