package com.rishav.fuelcost.util

object FuelCalculator {
    /**
     * mileageKmPerL: vehicle ki mileage (km/litre)
     * fuelPricePerL: current fuel price (₹/litre)
     * distanceKm: order ka total distance
     */
    fun calculateCost(distanceKm: Float, mileageKmPerL: Float, fuelPricePerL: Float): Float {
        if (mileageKmPerL <= 0) return 0f
        val litresUsed = distanceKm / mileageKmPerL
        return litresUsed * fuelPricePerL
    }
}
