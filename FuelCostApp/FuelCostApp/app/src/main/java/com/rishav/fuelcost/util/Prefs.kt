package com.rishav.fuelcost.util

import android.content.Context

object Prefs {
    private const val FILE = "fuel_cost_prefs"

    fun saveVehicleDetails(context: Context, mileageKmPerL: Float, fuelPricePerL: Float, apiKey: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putFloat("mileage", mileageKmPerL)
            .putFloat("fuel_price", fuelPricePerL)
            .putString("maps_api_key", apiKey)
            .apply()
    }

    fun getMileage(context: Context): Float =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getFloat("mileage", 40f)

    fun getFuelPrice(context: Context): Float =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getFloat("fuel_price", 103f)

    fun getMapsApiKey(context: Context): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString("maps_api_key", "") ?: ""
}
