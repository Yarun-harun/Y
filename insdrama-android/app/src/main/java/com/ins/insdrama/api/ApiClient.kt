package com.ins.insdrama.api

import com.ins.insdrama.model.Drama
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface DramaApi {
    @GET("INsITdeveloper/Drama-extension/main/datadrama.json")
    suspend fun getDramas(): Response<List<Drama>>
}

object ApiClient {
    private const val BASE_URL = "https://raw.githubusercontent.com/"
    
    val dramaApi: DramaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DramaApi::class.java)
    }
}
