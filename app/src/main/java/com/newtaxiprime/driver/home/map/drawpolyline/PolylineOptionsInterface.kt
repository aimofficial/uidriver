package com.newtaxiprime.driver.home.map.drawpolyline

/**
 * @package com.newtaxiprime.driver
 * @subpackage map.drawpolyline
 * @category PolylineOptionsInterface
 * @author Seen Technologies
 *
 */

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.newtaxiprime.driver.home.datamodel.StepsClass
import java.util.*

interface PolylineOptionsInterface {
    fun getPolylineOptions(output: PolylineOptions, points: ArrayList<LatLng>, distance: String, overviewPolyline: String, stepPoints: ArrayList<StepsClass>, totalDuration : Int)
}
