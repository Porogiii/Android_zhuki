package com.example.beetles.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CbrApiService {
    @GET("scripts/xml_metall.asp")
    suspend fun getMetallRates(
        @Query("date_req1") date1: String,
        @Query("date_req2") date2: String
    ): Response<String>
}