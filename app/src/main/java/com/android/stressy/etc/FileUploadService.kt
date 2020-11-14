package com.android.stressy.etc

import com.android.stressy.dataclass.BaseUrl
import com.android.stressy.dataclass.MyResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FileUploadService {

    @Headers("content-type: application/json")
    @POST("/weight")

    fun sendFile(@Body binary:RequestBody):Call<MyResponse>

    companion object{
        fun create(): FileUploadService{
            val retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl.url_aggregate)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(FileUploadService::class.java)
        }
    }
}