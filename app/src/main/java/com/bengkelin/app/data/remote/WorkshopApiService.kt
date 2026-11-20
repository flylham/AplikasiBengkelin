package com.bengkelin.app.data.remote

import com.bengkelin.app.data.model.WorkshopListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WorkshopApiService {

    @GET("api/workshops")
    suspend fun getWorkshops(
        @Query("category") category: String? = null,
        @Query("region") region: String? = null,
        @Query("query") query: String? = null
    ): Response<WorkshopListResponse>
}
