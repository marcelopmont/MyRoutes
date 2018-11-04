package com.mpm.myroutes

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.wear.widget.SwipeDismissFrameLayout
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.PolylineOptions

import kotlinx.android.synthetic.main.activity_maps.*
import com.mpm.myroutes.service.RouteService

class MapsActivity : WearableActivity(), OnMapReadyCallback {

    private var routeService: RouteService? = null

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val placesList = ArrayList<LatLng>()

    public override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        setAmbientEnabled()
        setContentView(R.layout.activity_maps)

        routeService = RouteService()

        swipe_dismiss_root_container.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout?) {
                // Hides view before exit to avoid stutter.
                layout?.visibility = View.GONE
                finish()
            }
        })

        swipe_dismiss_root_container.setOnApplyWindowInsetsListener { _, insetsArg ->
            val insets = swipe_dismiss_root_container.onApplyWindowInsets(insetsArg)

            val params = map_container.layoutParams as FrameLayout.LayoutParams

            // Add Wearable insets to FrameLayout container holding map as margins
            params.setMargins(
                    insets.systemWindowInsetLeft,
                    insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom)
            map_container.layoutParams = params

            insets
        }

        // Obtain the MapFragment and set the async listener to be notified when the map is ready.
        val mapFragment = map as MapFragment
        mapFragment.getMapAsync(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setupButtons()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.isMyLocationEnabled = true

        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        mMap.setOnMapLongClickListener {
            placesList.add(it)

            mapsButtons.visibility = View.VISIBLE
            mMap.addMarker(MarkerOptions().position(it))
        }

    }

    private fun setupButtons() {
        mapsDeletePlaces.setOnClickListener {
            mMap.clear()
            placesList.clear()

            mapsButtons.visibility = View.GONE
        }

        mapsDrawRoute.setOnClickListener {
            drawRoute()
        }
    }

    @SuppressLint("MissingPermission")
    private fun drawRoute() {
        mMap.clear()

        placesList.map {
            mMap.addMarker(MarkerOptions().position(it))
        }

        if (placesList.isNotEmpty()) {

            val locationResult = mFusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    routeService?.subscribe(task.result!!,
                            placesList,
                            resources.getString(R.string.google_maps_key),
                            {
                                mMap.addPolyline(PolylineOptions()
                                        .addAll(decodePoly(it.routes!!.first().overviewPolyLine!!.points!!))
                                        .color(ContextCompat.getColor(this, R.color.blue))
                                        .width(5f)
                                        .geodesic(true))
                            },{})
                }
            }
        }

    }

    private fun decodePoly(encoded: String): ArrayList<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val position = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(position)
        }
        return poly
    }

}
