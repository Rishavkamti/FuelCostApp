package com.rishav.fuelcost.util

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

object MapsHelper {

    private val client = OkHttpClient()

    interface DistanceCallback {
        fun onResult(distanceKm: Float, durationText: String)
        fun onError(message: String)
    }

    /**
     * origin aur destination: pickup address / drop address (text)
     * Ya "lat,lng" format bhi chalega.
     */
    fun getDistance(origin: String, destination: String, apiKey: String, callback: DistanceCallback) {
        val encodedOrigin = URLEncoder.encode(origin, "UTF-8")
        val encodedDest = URLEncoder.encode(destination, "UTF-8")
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=$encodedOrigin&destination=$encodedDest&key=$apiKey"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                response.use {
                    if (!it.isSuccessful) {
                        callback.onError("API error: ${it.code}")
                        return
                    }
                    val body = it.body?.string() ?: ""
                    try {
                        val json = JSONObject(body)
                        val status = json.getString("status")
                        if (status != "OK") {
                            callback.onError("Directions API status: $status")
                            return
                        }
                        val routes = json.getJSONArray("routes")
                        if (routes.length() == 0) {
                            callback.onError("Koi route nahi mila")
                            return
                        }
                        val leg = routes.getJSONObject(0)
                            .getJSONArray("legs")
                            .getJSONObject(0)
                        val distanceMeters = leg.getJSONObject("distance").getInt("value")
                        val durationText = leg.getJSONObject("duration").getString("text")
                        val distanceKm = distanceMeters / 1000f
                        callback.onResult(distanceKm, durationText)
                    } catch (e: Exception) {
                        callback.onError("Parse error: ${e.message}")
                    }
                }
            }
        })
    }
}
