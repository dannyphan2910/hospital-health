package hu.ait.hospitalhealth.network

import hu.ait.hospitalhealth.BuildConfig
import hu.ait.hospitalhealth.data.Base
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MapsAPI {
    @GET("venues/search")
    fun getHospitalDetails(
        @Query("ll") latlng: String,
        @Query("categoryId") category: String = "4bf58dd8d48988d104941735",
        @Query("v") version: String = "20180323",
        @Query("client_id") clientId: String = BuildConfig.FOURSQUARE_CLIENT_ID,
        @Query("client_secret") clientSecret: String = BuildConfig.FOURSQUARE_CLIENT_SECRET
    ): Call<Base>
}