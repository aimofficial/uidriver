package com.newtaxiprime.driver.home.fragments.payment

/**
 * @package com.newtaxi
 * @subpackage views.main.paytoadmin
 * @category Payment Activity
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.CommonMethods.Companion.DebuggableLogV
import com.newtaxiprime.driver.common.util.Enums
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.AppActivityWalletTransferBinding
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import java.util.*
import javax.inject.Inject


/* ************************************************************
   Rider profile details page
    *********************************************************** */

class TransferMoney : CommonActivity(), ServiceListener {

    lateinit var dialog: AlertDialog

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    lateinit var sendbutton: Button // Save button

    lateinit var wallet_amt: TextView

    lateinit var amounttext: EditText

    lateinit var input_account: EditText

    lateinit var accountgettext: String
    lateinit var amountgettext: String

    lateinit var binding: AppActivityWalletTransferBinding


    protected var isInternetAvailable: Boolean = false  // Check Network available or not


    fun back() {
        onBackPressed()
    }

    fun sendbutton() {
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        if (!isInternetAvailable) {
            commonMethods.showMessage(this@TransferMoney, dialog, getString(R.string.no_connection))
        } else {

            amountgettext = amounttext.text.toString()
            accountgettext = input_account.text.toString()

            try {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            /**
             * Update Rider profile
             */

            hideSoftKeyboard()

            amounttext.isFocusableInTouchMode = true
            input_account.isFocusableInTouchMode = true
            // Update profile API Call
            senMoney()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityWalletTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this)

        sendbutton = binding.sendbutton

        sendbutton.setOnClickListener {
            sendbutton()
        }

        binding.commonHeader.back.setOnClickListener {
            onBackPressed()
        }

        wallet_amt = binding.tvCallerName

        amounttext = binding.amounttext

        input_account = binding.inputAccount


        /**Commmon Header Text View */
        commonMethods.setheaderText(
            resources.getString(R.string.wallet_transfer),
            binding.commonHeader.headertext
        )
        // Check network available or not
        isInternetAvailable = commonMethods.isOnline(applicationContext)

        input_account.addTextChangedListener(NameTextWatcher(input_account))
        amounttext.addTextChangedListener(NameTextWatcher(amounttext))

        wallet_amt.text = sessionManager.currencySymbol + sessionManager.walletAmount


    }

    fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    /**
     * Back button pressed
     */
    override fun onBackPressed() {
        super.onBackPressed()
        sessionManager.profileDetail = ""
        sessionManager.phoneNumber = ""
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        val statuscode = commonMethods.getJsonValue(
            jsonResp.strResponse,
            "status_code",
            String::class.java
        ) as String

        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {

            Enums.REQ_ADD_WALLET ->
                if (jsonResp.isSuccess) {
                    if (statuscode.equals("2")) run {
                        commonMethods.showProgressDialog(this@TransferMoney)
                        commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                        //statusDialog(jsonResp.statusMsg)
                        commonMethods.getClientSecret(jsonResp, this)
                    } else {
                        val walletamount = commonMethods.getJsonValue(
                            jsonResp.strResponse,
                            "wallet_amount",
                            String::class.java
                        ) as String

                        sessionManager.walletAmount = walletamount
                        //statusDialog(jsonResp.statusMsg)
                        commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                        wallet_amt.text =
                            sessionManager.currencySymbol + sessionManager.walletAmount
                        //walletamt.setText("")
                        commonMethods.hideProgressDialog()
                    }
                } else if (statuscode.equals("2")) run {
                    commonMethods.showProgressDialog(this@TransferMoney)
                    //statusDialog(jsonResp.statusMsg)
                    commonMethods.getClientSecret(jsonResp, this)
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.hideProgressDialog()
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                    //statusDialog(jsonResp.statusMsg)
                    DebuggableLogV("jsonResp.getStatusMsg()", "" + jsonResp.statusMsg)
                }

            else -> {
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
    }

    /**
     * Update rider profile details API called
     */
    // fun senMoney() {
    // commonMethods.showProgressDialog(this)
    //  apiService.transferMoney(accountgettext, amountgettext,sessionManager.accessToken!!).enqueue(RequestCallback(REQ_UPDATE_PROFILE, this))
    //}

    fun senMoney() {
        commonMethods.showProgressDialog(this)
        apiService.transferMoney(sendMoneyToUser()).enqueue(
            RequestCallback(
                Enums.REQ_ADD_WALLET, this
            )
        )
    }

    private fun sendMoneyToUser(): LinkedHashMap<String, String> {
        val addWalletParams = LinkedHashMap<String, String>()
        addWalletParams["token"] = sessionManager.accessToken.toString()
        addWalletParams["amount"] = amountgettext
        addWalletParams["account_number"] = accountgettext
        return addWalletParams
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

        }

    }


    private fun validateAccount(): Boolean {
        if (input_account.text.toString().trim { it <= ' ' }.isEmpty()) {
            //  input_layout_first.error = getString(R.string.error_msg_firstname)
            //requestFocus(input_first);
            return false
        } else if (input_account.text.trim().length < 8) {
            return false
        } else if (input_account.text.trim().length > 8) {
            return false

        }

        return true
    }


    private fun validateAmount(): Boolean {
        if (amounttext.text.toString().trim { it <= ' ' }.isEmpty()) {
            //  input_layout_first.error = getString(R.string.error_msg_firstname)
            //requestFocus(input_first);
            return false
        } else if (amounttext.text.toString().trim() > sessionManager.walletAmount.toString()) {
            return false
        }

        return true
    }


    /**
     * Name edit text listener
     */
    private inner class NameTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            DebuggableLogV("beforeTextChanged", "beforeTextChanged")
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            //setting save button active when all dates are given
            if (validateAmount() && validateAccount()) {
                sendbutton.isEnabled = true
                sendbutton.setBackground(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.app_curve_button_yellow
                    )
                )
            } else {
                sendbutton.isEnabled = false
                sendbutton.setBackground(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.app_curve_button_yellow_disable
                    )
                )
            }
        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.input_account -> validateAccount()
                R.id.amounttext -> validateAmount()


                else -> {

                }
            }// validatePhone();

        }
    }
}



