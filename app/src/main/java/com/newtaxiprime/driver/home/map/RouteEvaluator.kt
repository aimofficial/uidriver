package com.newtaxiprime.driver.home.map

/**
 * @package com.newtaxiprime.driver
 * @subpackage map
 * @category RouteEvaluator
 * @author Seen Technologies
 *
 */

import android.animation.TypeEvaluator

import com.google.android.gms.maps.model.LatLng

class RouteEvaluator : TypeEvaluator<LatLng> {
    override fun evaluate(t: Float, startPoint: LatLng, endPoint: LatLng): LatLng {
        val lat = startPoint.latitude + t * (endPoint.latitude - startPoint.latitude)
        val lng = startPoint.longitude + t * (endPoint.longitude - startPoint.longitude)
        return LatLng(lat, lng)
    }
}
