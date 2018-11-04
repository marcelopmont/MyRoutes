package com.mpm.myroutes.model

import com.google.gson.annotations.SerializedName


class RouteModel {
    @SerializedName("routes")
    val routes: List<Route>? = null
}

class Route {
    @SerializedName("overview_polyline")
    val overviewPolyLine: OverviewPolyLine? = null

    val legs: List<Legs>? = null
}

class Legs {
    val steps: List<Steps>? = null
}

class Steps {
    val start_location: Location? = null
    val end_location: Location? = null
    val polyline: OverviewPolyLine? = null
}

class OverviewPolyLine {
    @SerializedName("points")
    var points: String? = null
}

class Location {
    val lat: Double = 0.toDouble()
    val lng: Double = 0.toDouble()
}