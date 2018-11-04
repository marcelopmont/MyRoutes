package com.mpm.myroutes.service

import android.location.Location
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.android.gms.maps.model.LatLng
import com.mpm.myroutes.model.RouteModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RouteService {

    fun subscribe(currentPosition: Location,
                  waypoints: ArrayList<LatLng>,
                  apiKey: String,
                  successCallback: (route: RouteModel) -> Unit,
                  errorCallback: () -> Unit) {

        var waypointsString = "optimize:true|"
        for (index in 0 until waypoints.size) {
            val point = waypoints[index]
            waypointsString += "${point.latitude},${point.longitude}|"
        }

        val farthestLocation = getFarthestLocation(currentPosition, waypoints)

        getRetrofit().getRoute("${currentPosition.latitude},${currentPosition.longitude}",
                "${farthestLocation.latitude},${farthestLocation.longitude}",
                "walking",
                apiKey,
                waypointsString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { response ->
                            successCallback(response) },
                        { _ ->
                            errorCallback() }
                )

    }

    private fun getRetrofit(): RouteServiceInterface {
        val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(StethoInterceptor()).build()

        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://maps.googleapis.com/")
                .client(okHttpClient)
                .build()
                .create(RouteServiceInterface::class.java)
    }

    private fun getFarthestLocation(origin: Location, waypoints: ArrayList<LatLng>): LatLng {
        var farthestLocation = waypoints.first()
        var farthestDistance = 0.0
        waypoints.map {
            val distance = Math.sqrt(Math.pow(Math.abs(origin.latitude - it.latitude), 2.0) +
                    Math.pow(Math.abs(origin.longitude - it.longitude), 2.0))
            if (distance > farthestDistance) {
                farthestLocation = it
                farthestDistance = distance
            }
        }
        return farthestLocation
    }


}