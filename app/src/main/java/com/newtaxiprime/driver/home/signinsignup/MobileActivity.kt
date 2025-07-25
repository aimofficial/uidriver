package com.newtaxiprime.driver.home.signinsignup

/**
 * @package com.newtaxiprime.driver
 * @subpackage signinsignup model
 * @category MobileActivity
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.ActivityMobilenumberBinding
import com.newtaxiprime.driver.home.datamodel.ForgetPwdModel
import com.newtaxiprime.driver.home.facebookAccountKit.FacebookAccountKitActivity
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import java.util.Locale
import javax.inject.Inject

/* ************************************************************
                MobileActivity
Its used to get the mobile number detail function
*************************************************************** */

class MobileActivity : CommonActivity(), ServiceListener {


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

    lateinit var ccp: CountryCodePicker

    lateinit var next: RelativeLayout

    lateinit var back: RelativeLayout

    lateinit var mobilelayout: LinearLayout

    lateinit var entermobileno: TextView

    lateinit var phone: EditText

    lateinit var progressBar: ProgressBar

    lateinit var backArrow: ImageView

    lateinit var binding: ActivityMobilenumberBinding
    protected var isInternetAvailable: Boolean = false

    operator fun next() {
        getUserProfile()
    }

    fun back() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobilenumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ccp = binding.ccp

        next = findViewById<RelativeLayout>(R.id.next)

        next.setOnClickListener {
            getUserProfile()
        }

        back = binding.commonHeader.back

        back.setOnClickListener {
            back()
        }

        mobilelayout = binding.mobilelayout

        entermobileno = binding.entermobileno

        phone = binding.phone

        progressBar = binding.progressBar


        backArrow = binding.backArrow
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(this)

        ccp.detectLocaleCountry(true)
        if (Locale.getDefault().language == "fa") {
            ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        } else if (Locale.getDefault().language == "es") {
            ccp.changeDefaultLanguage(CountryCodePicker.Language.SPANISH)
        } else if (Locale.getDefault().language == "ar") {
            ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        } else if (Locale.getDefault().language == "en") {
            ccp.changeDefaultLanguage(CountryCodePicker.Language.ENGLISH)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            window.sharedElementEnterTransition.duration = 600
            window.sharedElementReturnTransition.setDuration(600).interpolator =
                DecelerateInterpolator()
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            entermobileno.transitionName = "mobilenumber"
            mobilelayout.transitionName = "mobilelayout"
        }


        //Text listner
        phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                CommonMethods.DebuggableLogI("Character sequence ", " Check")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (phone.text.toString().startsWith("0")) {
                    phone.setText("")
                }
            }

            override fun afterTextChanged(s: Editable) {

                CommonMethods.DebuggableLogI("Character sequence ", " Checkins")

            }
        })

        sessionManager.countryCode = ccp.selectedCountryCodeWithPlus.replace("\\+".toRegex(), "")


    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {

            onSuccessForgetPwd(jsonResp)

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {

            onSuccessForgetPwd(jsonResp)

        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        progressBar.visibility = View.GONE
        backArrow.visibility = View.VISIBLE
    }


    private fun onSuccessForgetPwd(jsonResp: JsonResponse) {

        progressBar.visibility = View.GONE
        backArrow.visibility = View.VISIBLE

        val forgetPwdModel = gson.fromJson(jsonResp.strResponse, ForgetPwdModel::class.java)
        if (forgetPwdModel != null) {


            if (forgetPwdModel.statusCode.matches("1".toRegex())) {
                progressBar.visibility = View.GONE
                backArrow.visibility = View.VISIBLE
                sessionManager.temporaryPhonenumber = phone.text.toString()
                sessionManager.temporaryCountryCode =
                    ccp.selectedCountryCodeWithPlus.replace("\\+".toRegex(), "")

                /*String otp = forgetPwdModel.getOtp();
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Your OTP IS " + forgetPwdModel.getOtp(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();*/
                /*Intent intent = new Intent(getApplicationContext(), RegisterOTPActivity.class);
                intent.putExtra("otp", otp);
                intent.putExtra("resetpassword", true);
                if (sessionManager.getisEdit())
                    intent.putExtra("phone_number", phone.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);
                if (sessionManager.getisEdit()) {
                    finish();
                }*/

                FacebookAccountKitActivity.openFacebookAccountKitActivity(this)
            } else {
                progressBar.visibility = View.GONE
                backArrow.visibility = View.VISIBLE
                if (forgetPwdModel.statusMessage == "Message sending Failed,please try again..") {

                    /*String otp = forgetPwdModel.getOtp();
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Your OTP IS " + forgetPwdModel.getOtp(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();*/
                    /*Intent intent = new Intent(getApplicationContext(), RegisterOTPActivity.class);
                    intent.putExtra("otp", otp);
                    intent.putExtra("resetpassword", true);
                    if (sessionManager.getisEdit())
                        intent.putExtra("phone_number", phone.getText().toString());
                    startActivity(intent);
                    overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);
                    if (sessionManager.getisEdit()) {
                        finish();
                    }*/
                    sessionManager.temporaryPhonenumber = phone.text.toString()
                    sessionManager.temporaryCountryCode =
                        ccp.selectedCountryCodeWithPlus.replace("\\+".toRegex(), "")
                    FacebookAccountKitActivity.openFacebookAccountKitActivity(this)
                } else {
                    commonMethods.showMessage(this, dialog, forgetPwdModel.statusMessage)

                }
            }
        }
    }


    fun getUserProfile() {

        isInternetAvailable = commonMethods.isOnline(this)
        val phonestr = phone.text.toString()


        sessionManager.countryCode = ccp.selectedCountryCodeWithPlus.replace("\\+".toRegex(), "")
        if (phonestr.length == 0) {

            commonMethods.showMessage(this, dialog, getString(R.string.pleaseentermobile))


        } else if (phonestr.length > 5) {
            if (isInternetAvailable) {


                progressBar.visibility = View.VISIBLE
                backArrow.visibility = View.GONE

                // isEdit is set from Driver profile page
                if (!sessionManager.getisEdit()) {
                    // this is from forgot password

                    apiService.numberValidation(
                        sessionManager.type!!,
                        phone.text.toString(),
                        sessionManager.countryCode!!,
                        "1",
                        sessionManager.languageCode!!
                    ).enqueue(RequestCallback(this))
                    // here, phone number is stored to retrive from facebook account kit
                    sessionManager.phoneNumber = phone.text.toString()
                } else {

                    apiService.numberValidation(
                        sessionManager.type!!,
                        phone.text.toString(),
                        sessionManager.countryCode!!,
                        "",
                        sessionManager.languageCode!!
                    ).enqueue(RequestCallback(this))


                }


            } else {
                commonMethods.showMessage(this, dialog, resources.getString(R.string.Interneterror))
            }

        } else {
            commonMethods.showMessage(
                this,
                dialog,
                resources.getString(R.string.InvalidMobileNumber)
            )
        }

    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT) { // confirm that this response matches your request
            if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_VERIFACATION_SUCCESS) {
                if (sessionManager.getisEdit()) {
                    this.finish()
                } else {
                    callResetPasswordAPI()
                }
            }
        }
    }

    private fun callResetPasswordAPI() {
        val intent = Intent(applicationContext, ResetPassword::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }

}
