package com.a0100019.mypat.data.remote

import retrofit2.http.GET

interface NoticeService {
    @GET("api/notice")
    suspend fun getNotice(): String
}