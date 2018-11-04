package com.mpm.myroutes.service

import com.mpm.myroutes.model.RouteModel
import retrofit2.http.GET
import retrofit2.http.Query
import io.reactivex.Observable

interface RouteServiceInterface {
    @GET("maps/api/directions/json")
    fun getRoute(@Query("origin") origin: String,
                 @Query("destination") destination: String,
                 @Query("mode") mode: String,
                 @Query("key") apiKey: String,
                 @Query("waypoints") waypoints: String):
            Observable<RouteModel>
}