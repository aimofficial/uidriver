package com.newtaxiprime.driver.common.map

/**
 * @package com.newtaxiprime.user
 * @subpackage map
 * @category AppUtils
 * @author Seen Technologies
 * 
 */

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.text.TextUtils


/* ************************************************************
Location constants for location enable
*************************************************************** */
class AppUtils {

    object LocationConstants {
        val SUCCESS_RESULT = 0

        val FAILURE_RESULT = 1

        val PACKAGE_NAME = "com.newtaxiprime.driver.map"

        val RECEIVER = "$PACKAGE_NAME.RECEIVER"

        val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"

        val LOCATION_DATA_EXTRA = "$PACKAGE_NAME.LOCATION_DATA_EXTRA"

        val LOCATION_DATA_AREA = "$PACKAGE_NAME.LOCATION_DATA_AREA"
        val LOCATION_DATA_CITY = "$PACKAGE_NAME.LOCATION_DATA_CITY"
        val LOCATION_DATA_STREET = "$PACKAGE_NAME.LOCATION_DATA_STREET"


    }

    companion object {


        fun hasLollipop(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        }

        /**
         * Check is Location is enable or not
         */
        fun isLocationEnabled(context: Context): Boolean {
            var locationMode = 0
            val locationProviders: String
            var isAvailable: Boolean

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
                   // locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.L)
                } catch (e: Settings.SettingNotFoundException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                isAvailable = locationMode != Settings.Secure.LOCATION_MODE_OFF
            } else {
                locationProviders = Settings.Secure.getString(context.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
                isAvailable = !TextUtils.isEmpty(locationProviders)
            }

            val coarsePermissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val finePermissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

            return isAvailable && (coarsePermissionCheck || finePermissionCheck)
        }
    }


}
