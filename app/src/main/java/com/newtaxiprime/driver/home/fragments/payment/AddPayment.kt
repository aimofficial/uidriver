package com.newtaxiprime.driver.home.fragments.payment

/**
 * @package com.newtaxiprime.driver
 * @subpackage fragments.payment
 * @category AddPayment
 * @author Seen Technologies
 *
 */

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import butterknife.ButterKnife
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.ActivityAddPaymentBinding
import com.newtaxiprime.driver.home.MainActivity
import com.newtaxiprime.driver.home.datamodel.VehicleDetails
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import javax.inject.Inject

/* ************************************************************
                      AddPayment
Its used get Add the payment
*************************************************************** */
class AddPayment : CommonActivity(), ServiceListener {

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


    lateinit var payment_msg: TextView

    lateinit var emailName: TextInputLayout

    lateinit var emaitext: EditText
    protected var isInternetAvailable: Boolean = false

    lateinit var binding: ActivityAddPaymentBinding

    /*
     *  Check is email valid or not
     **/
    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }


    fun payment() {
        emaitext.setText("")
    }


    fun back() {
        onBackPressed()
    }

    fun save() {
        addPaymentApi()
    }

    fun arrow() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPaymentBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this)
        payment_msg = binding.paymentMsg
        emailName = binding.emailName

        emaitext = binding.emaitext
        binding.emailclose.setOnClickListener {
            payment()
        }

        binding.save.setOnClickListener {
            save()
        }
        binding.arrow.setOnClickListener {
            onBackPressed()
        }
        binding.arrow.setOnClickListener {
            onBackPressed()
        }

        /*
          *  Common loader and internet check
          **/
        isInternetAvailable = commonMethods.isOnline(this)


        //val appName = resources.getString(R.string.app_name)
        payment_msg.text = resources.getString(R.string.addpayment_msg)

        if (sessionManager.paypalEmail != "") {
            emaitext.setText(sessionManager.paypalEmail)
        }


    }

    private fun addPaymentApi() {

        isInternetAvailable = commonMethods.isOnline(this)

        if (!validateEmail()) {
            return
        }

        if (isInternetAvailable) {
            commonMethods.showProgressDialog(this)
            apiService.addPayout(
                emaitext.text.toString(),
                sessionManager.type!!,
                sessionManager.accessToken!!
            ).enqueue(RequestCallback(this))
        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.Interneterror))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
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
            onSuccessPayment(jsonResp)

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

    }

    fun onSuccessPayment(jsonResp: JsonResponse) {

        val vehicleResultModel = gson.fromJson(jsonResp.strResponse, VehicleDetails::class.java)
        if (vehicleResultModel != null) {


            if (sessionManager.paypalEmail?.length!! > 0) {
                sessionManager.paypalEmail = emaitext.text.toString()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                onBackPressed()
            } else {
                sessionManager.paypalEmail = emaitext.text.toString()
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
    }

    /*
     *   Validate email address
     **/
    private fun validateEmail(): Boolean {
        val email = emaitext.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || !isValidEmail(email)) {
            emailName.error = getString(R.string.error_msg_email)
            return false
        } else {
            emailName.isErrorEnabled = false
        }

        return true
    }


}
