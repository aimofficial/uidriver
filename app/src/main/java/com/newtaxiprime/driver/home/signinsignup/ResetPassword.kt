package com.newtaxiprime.driver.home.signinsignup

/**
 * @package com.newtaxiprime.driver
 * @subpackage signinsignup model
 * @category ResetPassword
 * @author Seen Technologies
 *
 */

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import butterknife.ButterKnife
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.newtaxiprime.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.newtaxiprime.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.ActivityResetpasswordBinding
import com.newtaxiprime.driver.home.MainActivity
import com.newtaxiprime.driver.home.datamodel.LoginDetails
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import org.json.JSONArray
import org.json.JSONException
import javax.inject.Inject

/* ************************************************************
                ResetPassword
Its used to get the reset password detail function
*************************************************************** */
class ResetPassword : CommonActivity(), ServiceListener {


    lateinit var dialog: AlertDialog

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    lateinit var next: CardView

    lateinit var input_password: EditText

    lateinit var input_confirmpassword: EditText

    lateinit var input_layout_password: TextInputLayout

    lateinit var input_layout_confirmpassword: TextInputLayout

    lateinit var progressBar: ProgressBar

    lateinit var nextArrow: ImageView

    /*@BindView(R.id.backArrow)
    lateinit var backArrow: ImageView*/
    protected var isInternetAvailable: Boolean = false

    lateinit var binding: ActivityResetpasswordBinding


    var facebookKitVerifiedMobileNumber = ""
    var facebookVerifiedMobileNumberCountryCode = ""
    var facebookVerifiedMobileNumberCountryNameCode = ""

    fun forgetPassword() {
        forgotPwd()
    }

    fun backPressed() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetpasswordBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)

        next = binding.next

        next.setOnClickListener {
            forgetPassword()
        }
        binding.commonHeader.back.setOnClickListener {
            backPressed()
        }

        input_password = binding.inputPassword

        input_confirmpassword = binding.inputConfirmpassword

        input_layout_password = binding.inputLayoutPassword

        input_layout_confirmpassword = binding.inputLayoutConfirmpassword

        progressBar = binding.progressBar

        nextArrow = binding.nextArrow
        commonMethods.setheaderText(
            resources.getString(R.string.resetpasswords),
            binding.commonHeader.headertext
        )
        //commonMethods.imageChangeforLocality(this,backArrow)
        //commonMethods.imageChangeforLocality(this,nextArrow)
        getMobileNumerAndCountryCodeFromIntent()
        dialog = commonMethods.getAlertDialog(this)

        isInternetAvailable = commonMethods.isOnline(this)


    }

    private fun getMobileNumerAndCountryCodeFromIntent() {
        if (intent != null) {
            facebookKitVerifiedMobileNumber =
                intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY).toString()
            facebookVerifiedMobileNumberCountryCode =
                intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY).toString()
            facebookVerifiedMobileNumberCountryNameCode =
                intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY)
                    .toString()
        }


    }

    private fun forgotPwd() {

        isInternetAvailable = commonMethods.isOnline(this)


        if (!validateFirst()) {
            return
        } else if (!validateconfrom()) {
            return
        } else {
            val input_password_str = input_password.text.toString().trim { it <= ' ' }
            val input_password_confirmstr =
                input_confirmpassword.text.toString().trim { it <= ' ' }
            if (input_password_str.length > 5 && input_password_confirmstr.length > 5 && input_password_confirmstr == input_password_str) {
                sessionManager.password = input_password_str


                if (isInternetAvailable) {
                    progressBar.visibility = View.VISIBLE
                    nextArrow.visibility = View.GONE

                    apiService.forgotpassword(
                        facebookKitVerifiedMobileNumber,
                        sessionManager.type!!,
                        facebookVerifiedMobileNumberCountryNameCode,
                        input_password_str,
                        sessionManager.deviceType!!,
                        sessionManager.deviceId!!,
                        sessionManager.languageCode!!
                    ).enqueue(RequestCallback(this))

                } else {
                    commonMethods.showMessage(
                        this,
                        dialog,
                        resources.getString(R.string.Interneterror)
                    )
                }

            } else {
                if (input_password_confirmstr != input_password_str) {
                    commonMethods.showMessage(
                        this,
                        dialog,
                        resources.getString(R.string.Passwordmismatch)
                    )
                } else {
                    commonMethods.showMessage(
                        this,
                        dialog,
                        resources.getString(R.string.InvalidPassword)
                    )
                }
            }
        }
    }

    /*
     *   Validate password field
     */
    private fun validateFirst(): Boolean {
        if (input_password.text.toString().trim { it <= ' ' }.isEmpty()) {
            //input_layout_password.error = getString(R.string.Enteryourpassword)
            binding.errorPassword.visibility = View.VISIBLE
            requestFocus(input_password)
            return false
        } else {
            //input_layout_password.isErrorEnabled = false
            binding.errorPassword.visibility = View.GONE
        }

        return true
    }

    /*
     *   Validate Confirm password field
     */
    private fun validateconfrom(): Boolean {
        if (input_confirmpassword.text.toString().trim { it <= ' ' }.isEmpty()) {
            binding.errorConfirmPassword.visibility = View.VISIBLE
            //input_layout_confirmpassword.error = getString(R.string.Confirmyourpassword)
            requestFocus(input_confirmpassword)
            return false
        } else {
            binding.errorConfirmPassword.visibility = View.GONE
            //input_layout_confirmpassword.isErrorEnabled = false
        }

        return true
    }

    /*
     *   Focus edit text field
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onBackPressed() {

        val intent = Intent(this, SigninSignupHomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {
            try {
                onSuccessResetPWd(jsonResp)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            progressBar.visibility = View.GONE
            nextArrow.visibility = View.VISIBLE
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    @Throws(JSONException::class)
    private fun onSuccessResetPWd(jsonResp: JsonResponse) {
        progressBar.visibility = View.GONE
        nextArrow.visibility = View.VISIBLE
        val signInUpResultModel = gson.fromJson(jsonResp.strResponse, LoginDetails::class.java)

        if (signInUpResultModel != null) {


            val driverStatus = signInUpResultModel.userStatus
            sessionManager.userId = signInUpResultModel.userID
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sessionManager.currencySymbol =
                    Html.fromHtml(signInUpResultModel.currencySymbol, Html.FROM_HTML_MODE_LEGACY)
                        .toString()
            } else {
                Html.fromHtml(signInUpResultModel.currencySymbol).toString()
            }
            sessionManager.currencyCode = signInUpResultModel.currencyCode
            sessionManager.paypalEmail = signInUpResultModel.payoutId
            sessionManager.driverSignupStatus = signInUpResultModel.userStatus
            sessionManager.setAcesssToken(signInUpResultModel.token)
            sessionManager.isRegister = true
            sessionManager.userType = signInUpResultModel.companyId

            if (driverStatus == "Car_details") {

                val carDeailsModel = gson.toJson(signInUpResultModel.carDetailModel)

                val cardetails = JSONArray(carDeailsModel)

                val carType = StringBuilder()
                carType.append(resources.getString(R.string.vehicle_type)).append(",")
                for (i in 0 until cardetails.length()) {
                    val cartype = cardetails.getJSONObject(i)

                    carType.append(cartype.getString("car_name")).append(",")
                }
                sessionManager.carType = carType.toString()
                startMainActivity()
                finish()
            } else if (driverStatus == "Document_details") {
                // If driver status is document_details then redirect to document upload page
                startMainActivity()
                finish()
            } else if (driverStatus == "pending") {

                // If driver status is pending check paypal email is exists then redirect to home page otherwise redirect to paypal email address page
                sessionManager.vehicle_id = signInUpResultModel.vehicleId
                /*if (sessionManager.getPaypalEmail().length() > 0) {

                    Intent x = new Intent(getApplicationContext(), MainActivity.class);
                    x.putExtra("signinup", true);
                    Bundle bndlanimation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.cb_fade_in, R.anim.cb_face_out).toBundle();
                    startActivity(x, bndlanimation);
                    finish();
                } else {
                    Intent signin = new Intent(getApplicationContext(), PaymentPage.class);
                    startActivity(signin);
                    overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);
                }*/
                openMainActivity()
            } else if (driverStatus == "Active") {
                // If driver status is active check paypal email is exists then redirect to home page otherwise redirect to paypal email address page
                sessionManager.vehicle_id = signInUpResultModel.vehicleId
                /* if (sessionManager.getPaypalEmail().length() > 0) {
                    Intent x = new Intent(getApplicationContext(), MainActivity.class);
                    x.putExtra("signinup", true);
                    Bundle bndlanimation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.cb_fade_in, R.anim.cb_face_out).toBundle();
                    startActivity(x, bndlanimation);
                    finish();
                } else {
                    Intent signin = new Intent(getApplicationContext(), PaymentPage.class);
                    startActivity(signin);
                    overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);
                }*/openMainActivity()
            } else {
                // Redirect to sign in signup home page
                val x = Intent(applicationContext, SigninSignupHomeActivity::class.java)
                val bndlanimation = ActivityOptions.makeCustomAnimation(
                    applicationContext,
                    R.anim.cb_fade_in,
                    R.anim.cb_face_out
                ).toBundle()
                startActivity(x, bndlanimation)
                finish()

            }


        }

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        progressBar.visibility = View.GONE
        nextArrow.visibility = View.VISIBLE
    }


    private fun startMainActivity() {
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

    fun openMainActivity() {
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


}
