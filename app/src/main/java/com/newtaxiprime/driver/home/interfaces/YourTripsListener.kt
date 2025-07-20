package com.newtaxiprime.driver.home.interfaces

/**
 * @package com.newtaxiprime.driver
 * @subpackage interfaces
 * @category YourTripsListener
 * @author Seen Technologies
 *
 */

import android.content.res.Resources

import com.newtaxiprime.driver.trips.tripsdetails.YourTrips


/*****************************************************************
 * YourTripsListener
 */

interface YourTripsListener {

    val res: Resources

    val instance: YourTrips
}
