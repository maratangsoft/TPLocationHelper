package com.maratangsoft.tplocationhelper.network

import com.maratangsoft.tplocationhelper.model.KakaoPlaceResponse
import com.maratangsoft.tplocationhelper.model.NidUserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitService {

    //네아로 사용자정보 API
    @GET("v1/nid/me")
    fun getNidUserInfo(@Header("Authorization")authorization:String): Call<NidUserInfoResponse>

    //카카오 키워드 장소검색 API
    @Headers("Authorization: KakaoAK bdaa34b766b82da37c2b4db8ce235861")
    @GET("/v2/local/search/keyword.JSON")
    fun getKakaoPlace(@Query("query")query:String, @Query("x")lng:String, @Query("y")lat:String): Call<KakaoPlaceResponse>
}