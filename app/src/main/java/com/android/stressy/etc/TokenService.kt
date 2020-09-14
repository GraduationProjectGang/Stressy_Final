package com.android.stressy.etc

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TokenService {
    @Headers("Content-Type: application/json")
    @POST("token")
    fun submit(@Body body: RequestBody): Call<ResponseBody>

    companion object{
        fun create(): TokenService{
            val retrofit = Retrofit.Builder()
                .baseUrl("http://localhost:8080")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
            return retrofit.create(TokenService::class.java)
        }
    }
}