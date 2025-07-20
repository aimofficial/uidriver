package com.newtaxiprime.driver.home.splash

/**
 * @package com.newtaxiprime.driver.home.splash
 * @subpackage Splash
 * @category GetDriverTripDetails
 * @author Seen Technologies
 *
 */


import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.os.StrictMode
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import butterknife.ButterKnife
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.Constants.FORCE_UPDATE
import com.newtaxiprime.driver.common.helper.Constants.SKIP_UPDATE
import com.newtaxiprime.driver.common.model.CheckVersionModel
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.Enums.REG_GET_CHECK_VERSION
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.home.MainActivity
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.newtaxiprime.driver.home.permissionoverview.PermissionOverViewActivity
import com.newtaxiprime.driver.home.signinsignup.SigninSignupHomeActivity
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/* ************************************************************
                splash
Its used to start animation screen of splash page
*************************************************************** */

/* ************************************************************
                splash
Its used to start animation screen of splash page
*************************************************************** */
class SplashActivity : CommonActivity(), ServiceListener {


    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    lateinit var dialog: AlertDialog


    private var userid: String? = null
    private var driverStatus: String? = null

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)

        Thread.sleep(3000)


        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        dialog = commonMethods.getAlertDialog(this)
        //Utils().startPowerSaverIntent(this)
        commonMethods.clearImageCacheWhenAppOpens(this)

        userid = sessionManager.accessToken
        driverStatus = sessionManager.driverSignupStatus
        sessionManager.deviceType = "2"
        sessionManager.type = "driver"

        // Set Locale Language
        setLocale()
        callForceUpdateAPI()


        /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */


        difference(getRequestRemainingTime(), 1619600439)
    }

    fun getRequestRemainingTime(): Long {
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH)
        try {
            val date: Date = sdf.parse(commonMethods.getCurrentTime())
            val output: Long = date.getTime() / 1000L
            val str = java.lang.Long.toString(output)
            return str.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    fun difference(currentTimeLong: Long, endTimelong: Long) {
        val diffrence = endTimelong - currentTimeLong
        println("Difference " + diffrence)
        val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(diffrence)
        println("Get Seconds " + seconds)
    }

    private fun callForceUpdateAPI() {
        if (!commonMethods.isOnline(applicationContext)) {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.no_connection))
        } else {
            apiService.checkVersion(
                CommonMethods.getAppVersionNameFromGradle(this),
                sessionManager.type!!,
                CommonKeys.DeviceTypeAndroid
            ).enqueue(RequestCallback(REG_GET_CHECK_VERSION, this))
        }
    }

    private fun setLocale() {
        val lang = sessionManager.language

        if (lang != "") {
            val langC = sessionManager.languageCode
            val locale = Locale(langC)
            val res: Resources = resources
            val configuration: Configuration = res.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            } else
                configuration.setLocale(locale)
            createConfigurationContext(configuration)
            this@SplashActivity.resources.updateConfiguration(
                configuration,
                this@SplashActivity.resources.displayMetrics
            )
        } else {
            sessionManager.language = "English"
            sessionManager.languageCode = "en"
        }


    }


    private fun startMainActivity() {
        sessionManager.isLocationUpdatedForOneTime = true
        val x = Intent(applicationContext, MainActivity::class.java)
        x.putExtra("signinup", true)
        val bndlanimation = ActivityOptions.makeCustomAnimation(
            applicationContext,
            R.anim.cb_fade_in,
            R.anim.cb_face_out
        ).toBundle()
        startActivity(x, bndlanimation)
        finish()
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        try {
            Log.i("CHECK_VERSION", "onSuccess: jsonResp=${Gson().toJson(jsonResp)}")
            Log.i("CHECK_VERSION", "onSuccess: data=$data")
            when (jsonResp.requestCode) {
                REG_GET_CHECK_VERSION -> {
                    checkVersionModel =
                        gson.fromJson(jsonResp.strResponse, CheckVersionModel::class.java);
                    if (null != checkVersionModel && checkVersionModel.statusCode.equals("1")) {
                        onSuccessCheckVersion(checkVersionModel)
                    } else if (!TextUtils.isEmpty(checkVersionModel.statusMessage)) {
                        commonMethods.showMessage(this, dialog, checkVersionModel.statusMessage!!)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Please check your checkVersion api", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

    }

    private fun onSuccessCheckVersion(checkVersionModel: CheckVersionModel) {
        try {
            sessionManager.setReferralOpiton(checkVersionModel.enableReferral)

            val foreceUpdate = checkVersionModel.forceUpdate
            when {
                foreceUpdate!! == SKIP_UPDATE || foreceUpdate == FORCE_UPDATE -> {
                    showAlertDialogButtonClicked(foreceUpdate)
                }
                else -> {
                    moveToNextScreen()
                }
            }

        } catch (j: JSONException) {
            j.printStackTrace()
        }
    }

    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(
            ContextThemeWrapper(this, R.style.AlertDialogCustom)
        )
        alertDialog.setCancelable(false)
        //alertDialog.setTitle("Update");
        alertDialog.setMessage("Please update our app to enjoy the latest features!")
        alertDialog.setPositiveButton(
            "Visit play store"
        ) { _, _ ->
            CommonMethods.openPlayStore(this)
            this.finish()
        }
        alertDialog.show()
    }

    private fun showAlertDialogButtonClicked(updateType: String?) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater

        val dialogView: View = inflater.inflate(R.layout.update_view, null)
        val title = dialogView.findViewById(R.id.tv_title) as TextView
        val description = dialogView.findViewById(R.id.tv_desc) as TextView
        val skip = dialogView.findViewById(R.id.tv_skip) as TextView
        val update = dialogView.findViewById(R.id.tv_update) as TextView
        val titleValue: String
        val descriptionValue: String
        if (updateType.equals(SKIP_UPDATE)) {
            titleValue = resources.getString(R.string.new_version_available)
            descriptionValue = resources.getString(R.string.update_desc)
        } else {
            skip.visibility = View.GONE
            titleValue = resources.getString(R.string.new_version_available)
            descriptionValue = resources.getString(R.string.update_desc1)
        }
        title.text = titleValue
        description.text = descriptionValue
        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()
        alertDialog.setCancelable(false)
        skip.setOnClickListener {
            alertDialog.dismiss()
            moveToNextScreen()
        }
        update.setOnClickListener {
            CommonMethods.openPlayStore(this)
            this.finish()
        }
        alertDialog.show()
    }

    private fun moveToNextScreen() {
        if (userid.isNullOrEmpty()) {
            // Driver status is car_details then automatically redirect to Driver signin signup home page
            val x = Intent(applicationContext, SigninSignupHomeActivity::class.java)
            val bndlanimation = ActivityOptions.makeCustomAnimation(
                applicationContext,
                R.anim.cb_fade_in,
                R.anim.cb_face_out
            ).toBundle()
            startActivity(x, bndlanimation)
            finish()
        } else {
            startMainActivity()
        }
    }

    companion object {

        // Splash screen timer
        lateinit var checkVersionModel: CheckVersionModel
        private val SPLASH_TIME_OUT = 2000
    }
}

