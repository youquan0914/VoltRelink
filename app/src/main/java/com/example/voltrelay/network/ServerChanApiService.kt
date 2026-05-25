package com.example.voltrelay.network

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface ServerChanApiService {
    @FormUrlEncoded
    @POST("{sendKey}.send")
    suspend fun sendMessage(
        @Path("sendKey") sendKey: String,
        @Field("title") title: String,
        @Field("desp") description: String
    ): Response<ServerChanResponse>
}

@JsonClass(generateAdapter = true)
data class ServerChanResponse(
    val code: Int,
    val message: String,
    val data: Any? = null
)
