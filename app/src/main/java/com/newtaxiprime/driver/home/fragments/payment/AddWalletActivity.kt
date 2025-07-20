package com.newtaxiprime.driver.home.fragments.payment


//import com.paypal.android.sdk.payments.*
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import butterknife.ButterKnife
import com.braintreepayments.api.BraintreeFragment
import com.braintreepayments.api.PayPal
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener
import com.braintreepayments.api.models.PayPalRequest
import com.braintreepayments.api.models.PaymentMethodNonce
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.Constants.INTENT_PAY_FOR
import com.newtaxiprime.driver.common.helper.Constants.Payment
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonKeys.PAY_FOR_WALLET
import com.newtaxiprime.driver.common.util.CommonKeys.REQUEST_CODE_PAYMENT
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.CommonMethods.Companion.DebuggableLogV
import com.newtaxiprime.driver.common.util.Enums.REQ_ADD_WALLET
import com.newtaxiprime.driver.common.util.Enums.REQ_CURRENCY_CONVERT
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.common.views.FlutterwaveWebViewActivity
import com.newtaxiprime.driver.common.views.PaymentWebViewActivity
import com.newtaxiprime.driver.common.views.PaystackWebViewActivity
import com.newtaxiprime.driver.databinding.AppActivityAddWalletBinding
import com.newtaxiprime.driver.home.datamodel.PaymentMethodsModel
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.squareup.picasso.Picasso
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.model.StripeIntent
import java.util.*
import javax.inject.Inject
import kotlin.collections.set


class AddWalletActivity : CommonActivity(), ServiceListener, PaymentMethodNonceCreatedListener {

    private var mBraintreeFragment: BraintreeFragment? = null

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

    lateinit var wallet_acc: TextView

    lateinit var wallet_amt: TextView
    lateinit var dialog2: BottomSheetDialog
    lateinit var change: TextView
    lateinit var paymentmethod_type: TextView
    lateinit var paymentmethod_img: ImageView
    lateinit var walletamt: EditText
    lateinit var add_money: Button
    protected var isInternetAvailable: Boolean = false

    private lateinit var payableWalletAmount: String
    private val REQUEST_CODE = 101
    private lateinit var nonce: String

    private lateinit var binding: AppActivityAddWalletBinding
    var paymentlist = ArrayList<PaymentMethodsModel.PaymentMethods>()

    //  private lateinit var  config: PayPalConfiguration

    fun back() {
        onBackPressed()
    }

    private var mLastClickTime: Long = 0

    fun addamount() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        walletamt.setText("")
        if (!dialog2.isShowing) {
            dialog2.show()
        }
    }

    fun tranfer() {
        val profileIntent = Intent(this, TransferMoney::class.java)
        startActivity(profileIntent)
    }

    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityAddWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ButterKnife.bind(this)
        wallet_acc = binding.tvWalletAccount

        binding.addAmount.setOnClickListener {
            addamount()
        }

        binding.commonHeader.back.setOnClickListener {
            back()
        }

        binding.transfermoney.setOnClickListener {
            tranfer()
        }

        wallet_amt = binding.tvCallerName
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this)
        message = intent.getStringExtra("status_message")

        /**Commmon Header Text View */
        commonMethods.setheaderText(
            resources.getString(R.string.wallet),
            binding.commonHeader.headertext
        )
        /**
         * common loader and internet available or not check
         */
        isInternetAvailable = commonMethods.isOnline(applicationContext)

        /**
         * Wallet Add amount dialog
         */
        addAmount()

        wallet_amt.text = sessionManager.currencySymbol + sessionManager.walletAmount
        wallet_acc.text = sessionManager.walletAccount

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }


    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * Add wallet amount called
     */
    fun addAmount() {
        val currency_symbol_wallet: TextView

        dialog2 = BottomSheetDialog(this@AddWalletActivity, R.style.BottomSheetDialogTheme)
        dialog2.setContentView(R.layout.app_add_wallet_amount)
        paymentmethod_type = (dialog2.findViewById<View>(R.id.paymentmethod_type) as TextView?)!!
        val paymentmethod = (dialog2.findViewById<RelativeLayout>(R.id.paymentmethod))!!
        change = (dialog2.findViewById<View>(R.id.change) as TextView?)!!
        add_money = (dialog2.findViewById<View>(R.id.add_money) as Button?)!!
        walletamt = (dialog2.findViewById<View>(R.id.walletamt) as EditText?)!!
        //currency_symbol_wallet.text = sessionManager.currencySymbol
        paymentmethod_img = (dialog2.findViewById<View>(R.id.paymentmethod_img) as ImageView?)!!
        walletamt.setText("")
        if (sessionManager.payementModeWebView!!) {
            paymentmethod.visibility = View.GONE
        } else {
            paymentmethod.visibility = View.VISIBLE
        }

        walletamt.setText(sessionManager.currencySymbol + " " + "")
        Selection.setSelection(walletamt.text, walletamt.text.length)
        setEditTextMaxLength(
            6,
            walletamt
        ) // length set as 6 bcz currency symbol , Space occupies and Other 4 for amount
        walletamt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.toString().startsWith(sessionManager.currencySymbol + " ")) {
                    walletamt.setText(sessionManager.currencySymbol + " " + "")
                    Selection.setSelection(walletamt.text, walletamt.text.length)

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        add_money.setOnClickListener {
            if (walletamt.text.trim().length > 1 && walletamt.text.trim().substring(2)
                    .toFloat() > 0
            ) {
                payableWalletAmount = walletamt.text.trim().substring(2)

                if (sessionManager.payementModeWebView!!) {
                    val webview = Intent(this, PaymentWebViewActivity::class.java)
                    webview.putExtra("payableAmount", payableWalletAmount)
                    webview.putExtra(INTENT_PAY_FOR, PAY_FOR_WALLET)
                    startActivityForResult(webview, Payment)
                } else {
                    if (sessionManager.walletPaymentMethodkey!!.isNotEmpty() && !sessionManager.walletPaymentMethodkey.equals(
                            CommonKeys.PAYMENT_CASH
                        )
                    ) {
                        if (sessionManager.walletPaymentMethodkey.equals(CommonKeys.PAYMENT_CARD)) {
                            if (commonMethods.isOnline(applicationContext)) {
                                addWalletMoneyUsingStripe("");
                            } else {
                                commonMethods.showMessage(
                                    this@AddWalletActivity,
                                    dialog,
                                    getString(R.string.no_connection)
                                );
                            }
                        } else {
                            currencyConversion(payableWalletAmount)
                        }

                        if (sessionManager.walletPaymentMethodkey.equals(CommonKeys.PAYMENT_FLUTTERWAVE)) {
                            val webview = Intent(this, FlutterwaveWebViewActivity::class.java)
                            webview.putExtra("payableAmount", payableWalletAmount)
                            webview.putExtra(INTENT_PAY_FOR, PAY_FOR_WALLET)
                            startActivityForResult(webview, Payment)
                        } else {
                            currencyConversion(payableWalletAmount)
                        }


                        if (sessionManager.walletPaymentMethodkey.equals(CommonKeys.PAYMENT_PAYSTACK)) {
                            val webview = Intent(this, PaystackWebViewActivity::class.java)
                            webview.putExtra("payableAmount", payableWalletAmount)
                            webview.putExtra(INTENT_PAY_FOR, PAY_FOR_WALLET)
                            startActivityForResult(webview, Payment)
                        } else {
                            currencyConversion(payableWalletAmount)
                        }
                    } else {
                        Toast.makeText(
                            this@AddWalletActivity,
                            resources.getString(R.string.choose_payment_type),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                dialog2.dismiss()
            } else {
                Toast.makeText(
                    this@AddWalletActivity,
                    resources.getString(R.string.enter_wallet_amount),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        change.setOnClickListener {
            CommonKeys.isWallet = 1
            val intent = Intent(this, PaymentPage::class.java)
            intent.putExtra(
                CommonKeys.TYPE_INTENT_ARGUMENT_KEY,
                CommonKeys.StatusCode.startPaymentActivityForAddMoneyToWallet
            )
            startActivity(intent)
            overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
        }
    }


    fun setEditTextMaxLength(length: Int, editText: EditText) {
        val filterArray: Array<InputFilter?> = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(length)
        editText.filters = filterArray
    }

    /**
     * Convert Currency For BrainTree
     * @param payableWalletAmount
     */
    protected fun currencyConversion(payableWalletAmount: String?) {
        if (commonMethods.isOnline(getApplicationContext())) {
            //commonMethods.showProgressDialogPaypal(this, customDialog, resources.getString(R.string.few_seconds))
            commonMethods.showProgressDialog(this)
            apiService.currencyConversion(
                payableWalletAmount!!,
                sessionManager.accessToken!!,
                sessionManager.walletPaymentMethodkey!!.lowercase()
            ).enqueue(RequestCallback(REQ_CURRENCY_CONVERT, this))

        } else {
            commonMethods.showMessage(
                this@AddWalletActivity,
                dialog,
                getString(R.string.no_connection)
            );
        }
    }

    private fun callBrainTreeUI(payableWalletAmount: String) {
        val dropInRequest = DropInRequest()
            .requestThreeDSecureVerification(true)
            .threeDSecureRequest(commonMethods.threeDSecureRequest(payableWalletAmount))
            .clientToken(sessionManager.brainTreeClientToken)
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE)
    }

    override fun onResume() {
        super.onResume()
        commonMethods.hideProgressDialog()
        if (sessionManager.walletPaymentMethodkey!!.isEmpty() || sessionManager.walletPaymentMethodkey.equals(
                CommonKeys.PAYMENT_CASH
            )
        ) {
            paymentmethod_type.text = resources.getString(R.string.Add_payment_type)
            change.text = resources.getString(R.string.choose)
            paymentmethod_img.visibility = View.GONE
        } else {
            if (sessionManager.walletPaymentMethodkey == CommonKeys.PAYMENT_CARD) {

                if (sessionManager.cardValue != "") {
                    paymentmethod_type.text = "•••• " + sessionManager.cardValue
                    if (sessionManager.paymentMethodImage.equals("")) {
                        paymentmethod_img.setImageDrawable(
                            CommonMethods.getCardImage(
                                sessionManager.cardBrand.toString(),
                                resources
                            )
                        )
                    } else
                        Picasso.get().load(sessionManager.paymentMethodImage)
                            .into(paymentmethod_img)
                } else {
                    paymentmethod_type.text = resources.getString(R.string.card)
                }
            } else {
                paymentmethod_type.text = sessionManager.walletPaymentMethod
                Picasso.get().load(sessionManager.paymentMethodImage).into(paymentmethod_img)
            }
            paymentmethod_img.visibility = View.VISIBLE
            change.text = resources.getString(R.string.change)
        }
        /*   if (sessionManager.walletPaymentMethod == "") {
               sessionManager.walletPaymentMethod = resources.getString(R.string.paypal_payment)
           } else  if (sessionManager.walletPaymentMethod == CommonKeys.PAYMENT_CARD) {
               paymentmethod_img.setImageDrawable(CommonMethods.getCardImage(sessionManager.cardBrand, resources))
               if (sessionManager.cardValue != "") {
                   paymentmethod_type.text = "•••• " + sessionManager.cardValue
               } else {
                   paymentmethod_type.text = resources.getString(R.string.card)
               }
           }
           else if (sessionManager.walletPaymentMethod == CommonKeys.PAYMENT_BRAINTREE) {
               paymentmethod_img.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.online_payment))
               paymentmethod_type.text = resources.getString(R.string.onlinepayment)
           } else {
               paymentmethod_img.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.paypal))
               paymentmethod_type.text = resources.getString(R.string.paypal)
           }*/
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
            REQ_CURRENCY_CONVERT -> if (jsonResp.isSuccess) {
                println("response" + jsonResp.strResponse)
                val amount = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "amount",
                    String::class.java
                ) as String
                if (sessionManager.walletPaymentMethodkey.equals(CommonKeys.PAYMENT_BRAINTREE)) {
                    sessionManager.brainTreeClientToken = commonMethods.getJsonValue(
                        jsonResp.strResponse,
                        "braintree_clientToken",
                        String::class.java
                    ) as String
                    callBrainTreeUI(amount)
                } else if (sessionManager.walletPaymentMethodkey.equals(CommonKeys.PAYMENT_PAYPAL)) {
                    commonMethods.showProgressDialog(this)
                    //commonMethods.showProgressDialogPaypal(this, customDialog, resources.getString(R.string.few_seconds))
                    val mAuthorization: String = commonMethods.getJsonValue(
                        jsonResp.strResponse,
                        "braintree_clientToken",
                        String::class.java
                    ) as String

                    try {
                        mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization)
                    } catch (e: Exception) {

                    }

                    mBraintreeFragment?.addListener(this)

                    val currency_code = commonMethods.getJsonValue(
                        jsonResp.strResponse,
                        "currency_code",
                        String::class.java
                    ) as String
                    //payment(amount, currency_code,sessionManager.paypal_mode,sessionManager.paypal_app_id);

                    setupBraintreeAndStartExpressCheckout(amount, currency_code)

                }

            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }

            REQ_ADD_WALLET ->
                if (jsonResp.isSuccess) {
                    if (statuscode.equals("2")) run {
                        commonMethods.showProgressDialog(this@AddWalletActivity)
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
                        walletamt.setText("")
                        commonMethods.hideProgressDialog()
                    }
                } else if (statuscode.equals("2")) run {
                    commonMethods.showProgressDialog(this@AddWalletActivity)
                    //statusDialog(jsonResp.statusMsg)
                    commonMethods.getClientSecret(jsonResp, this)
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.hideProgressDialog()
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                    //statusDialog(jsonResp.statusMsg)
                    DebuggableLogV("jsonResp.getStatusMsg()", "" + jsonResp.statusMsg)
                }
            /*  REQ_ADD_CARD -> if (jsonResp.isSuccess) {
                  val walletamount = commonMethods.getJsonValue(jsonResp.strResponse, "wallet_amount", String::class.java) as String
                  sessionManager.walletAmount = walletamount
                  wallet_amt.text = sessionManager.currencySymbol + sessionManager.walletAmount
                  commonMethods.hideProgressDialog()
              } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                  commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                  DebuggableLogV("jsonResp.getStatusMsg()", "" + jsonResp.statusMsg)
              }*/
            else -> {
            }
        }
    }

    /**
     * Paypal payment
     */
    /*    fun payment(amount:String,currencycode:String,paypal_mode:Int, paypal_app_id:String) {
            *//*
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         *
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         *//*
        loadPayPal(paypal_mode,paypal_app_id);
        val thingToBuy: PayPalPayment = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE, amount, currencycode);
        //PayPalPayment thingToBuy = getStuffToBuy(PayPalPayment.PAYMENT_INTENT_SALE);
        *//*
         * See getStuffToBuy(..) for examples of some available payment options.
         *//*

        //addAppProvidedShippingAddress(thingToBuy);   /// Add shipping address
        //enableShippingAddressRetrieval(thingToBuy,true);  //  Enable retrieval of shipping addresses from buyer's PayPal account

         intent =  Intent(this, PaymentActivity::class.java);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    private fun getThingToBuy( paymentIntent:String,  amount:String,  currencycode:String):PayPalPayment {

        return  PayPalPayment( BigDecimal(amount), currencycode, getResources().getString(R.string.payment_name),
                paymentIntent);
    }
*/
    override fun onFailure(jsonResp: JsonResponse, data: String) {
        DebuggableLogV(TAG, "onFailure")
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            mBraintreeFragment?.removeListener(this)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * To get nounce from brain tree paypal
     */
    override fun onPaymentMethodNonceCreated(paymentMethodNonce: PaymentMethodNonce) {
        commonMethods.hideProgressDialog()
        val nonce: String = paymentMethodNonce.nonce
        println("nonce : " + nonce)
        paymentCompleted(nonce)
    }


    fun setupBraintreeAndStartExpressCheckout(amount: String, currencycode: String) {
        val request = PayPalRequest(amount)
            .currencyCode(currencycode)
            .intent(PayPalRequest.INTENT_SALE)
        PayPal.requestOneTimePayment(mBraintreeFragment, request)
    }


    fun addWalletMoneyUsingStripe(paykey: String) {
        commonMethods.showProgressDialog(this)
        apiService.addWalletMoneyUsingStripe(addYourMoneyToWalletFromStripe(paykey))
            .enqueue(RequestCallback(REQ_ADD_WALLET, this))
    }

    private fun addYourMoneyToWalletFromStripe(payKey: String): LinkedHashMap<String, String> {
        println("walletPaymentmethod ${sessionManager.walletPaymentMethodkey}")
        val addWalletParams = LinkedHashMap<String, String>()
        addWalletParams["token"] = sessionManager.accessToken.toString()
        addWalletParams["payment_type"] = sessionManager.walletPaymentMethodkey.toString()
        addWalletParams["pay_key"] = payKey
        addWalletParams["amount"] = payableWalletAmount
        return addWalletParams
    }

    /**
     * Check paypal payment completed or not
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PAYMENT) {/*
            if (resultCode == Activity.RESULT_OK) {
                val confirm :PaymentConfirmation?=
                        data?.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        DebuggableLogI(TAG, confirm.toJSONObject().toString(4));
                        DebuggableLogI(TAG, confirm.toJSONObject().getJSONObject("response").get("id").toString());
                        DebuggableLogI(TAG, confirm.getPayment().toJSONObject().toString(4));
                        */
            /**
             *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
             * or consent completion.
             * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
             * for more details.
             *
             * For sample mobile backend interactions, see
             * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
             *//*
                        paymentCompleted(confirm.toJSONObject().getJSONObject("response").get("id").toString());
                        // displayResultText("PaymentConfirmation info received from PayPal");

                    } catch ( e: JSONException) {
                        DebuggableLogE(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                DebuggableLogI(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                DebuggableLogI(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
       */
        } else if (requestCode == REQUEST_CODE) {
            if (data != null) {
                val result =
                    data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                nonce = result?.paymentMethodNonce!!.nonce
                addWalletMoneyUsingStripe(nonce)
            }
        } else if (requestCode == Payment) {
            if (data != null) {
                message = data.extras?.getString("status_message")
                val walletAmount = data.extras?.getString("walletAmount")
                if (!walletAmount.isNullOrEmpty()) {
                    wallet_amt.text = sessionManager.currencySymbol + walletAmount
                }
                //statusDialog(message!!)
                commonMethods.showMessage(this, dialog, message!!)
            }
        } else {
            if (data != null) {
                commonMethods.hideProgressDialog()
                commonMethods.stripeInstance()!!.onPaymentResult(
                    requestCode,
                    data,
                    object : ApiResultCallback<PaymentIntentResult> {
                        override fun onError(e: Exception) {
                            e.printStackTrace()
                        }

                        override fun onSuccess(result: PaymentIntentResult) {
                            val paymentIntent = result.intent
                            if (paymentIntent.status == StripeIntent.Status.Succeeded) {
                                addWalletMoneyUsingStripe(paymentIntent.id!!)
                            } else {
                                commonMethods.showMessage(this@AddWalletActivity, dialog, "Failed")
                            }
                        }

                    })
            }
        }
    }

    /**
     * Paypal payment completed to update transcation id
     */
    private fun paymentCompleted(payKey: String) {
        commonMethods.showProgressDialog(this)
        isInternetAvailable = commonMethods.isOnline(getApplicationContext());
        if (isInternetAvailable) {
            apiService.addWalletMoneyUsingStripe(addYourMoneyToWalletFromStripe(payKey))
                .enqueue(RequestCallback(REQ_ADD_WALLET, this))
        } else {
            commonMethods.showMessage(
                this@AddWalletActivity,
                dialog,
                getString(R.string.no_connection)
            );
        }
    }

    /**
     * Add Wallet Amount
     */
    private fun addWalletMoneyUsingPaypal(paykey: String, paypalamount: String) {
//        commonMethods.showProgressDialog(this)
        apiService.addWalletMoneyUsingPaypal(sessionManager.accessToken!!, paykey, paypalamount)
            .enqueue(RequestCallback(REQ_ADD_WALLET, this));

    }

    companion object {

        private val TAG = "paymentExample"
        private var CONFIG_ENVIRONMENT: String? = null

        // note that these credentials will differ between live & sandbox environments.
        private var CONFIG_CLIENT_ID: String? = null
    }


}


