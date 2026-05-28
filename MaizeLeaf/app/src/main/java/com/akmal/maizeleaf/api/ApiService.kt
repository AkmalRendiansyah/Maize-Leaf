package com.akmal.maizeleaf.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("deskripsipenyakit")
    suspend fun deskripsiPenyakit(
        @Query("penyakit") penyakit: String
    ): DeskripsiPenyakitResponse

    @Multipart
    @POST("history")
    suspend fun postHistory(
        @Header("Authorization") token: String,
        @Part("penyakit") penyakit: RequestBody,
        @Part gambar: MultipartBody.Part
    ): HistoryResponse

    @GET("history")
    suspend fun getHistory(
        @Header("Authorization") token: String
    ): List<GetHistoryResponseItem>

    @DELETE("history/{id}")
    suspend fun deleteHistory(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @Multipart
    @POST("posting")
    suspend fun postChat(
        @Header("Authorization") token: String,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part gambar: MultipartBody.Part
    ): PostingResponse

    @GET("posting")
    suspend fun getPosting(
        @Header("Authorization") token: String
    ): List<GetAllPostingResponseItem>

    @GET("komentar/{id}")
    suspend fun getKomentarById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<GetKomentarResponseItem>

    @FormUrlEncoded
    @POST("komentar")
    suspend fun postKomentar(
        @Header("Authorization") token: String,
        @Field("id_posting") idPosting: Int,
        @Field("komentar") komentar: String,
    ): PostKomentarResponse

    @GET("artikel")
    suspend fun getArtikel(
        @Header("Authorization") token: String
    ): List<GetArtikelResponseItem>

    @FormUrlEncoded
    @POST("verify-otp")
    suspend fun verifyOtp(
        @Field("userId") userId: Int,
        @Field("otp") otp: Int
    ): VerifyOtpResponse

    @FormUrlEncoded
    @POST("resend-otp")
    suspend fun resendOtp(
        @Field("userId") userId: Int
    ): VerifyOtpResponse



}