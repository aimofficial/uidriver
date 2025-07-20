package com.newtaxiprime.driver.home.fragments.payment

/**
 * @package com.newtaxiprime.driver
 * @subpackage views.main.paytoadmin
 * @category AddCardActivity
 * @author Seen Technologies
 *
 */


import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import butterknife.ButterKnife
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.AppActivityAddCardBinding
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.stripe.android.ApiResultCallback
import com.stripe.android.SetupIntentResult
import com.stripe.android.model.ConfirmSetupIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.StripeIntent
import com.stripe.android.view.CardMultilineWidget
import javax.inject.Inject

/*************************************************************
 * AddCardActivity
 * Its used to Add Stripe Card details for the Driver
 */
class AddCardActivity : CommonActivity(), ServiceListener {


    lateinit var dialog: AlertDialog

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var sessionManager: SessionManager


    lateinit var btnAddCard: Button

    lateinit var cardMultilineWidget: CardMultilineWidget
    private lateinit var clientSecretKey: String

    lateinit var binding: AppActivityAddCardBinding


    fun onBack() {
        onBackPressed()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityAddCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.commonHeader.back.setOnClickListener {
            onBack()
        }

        btnAddCard = binding.btnAddCard

        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this)

        /**Commmon Header Text View */
        commonMethods.setheaderText(
            resources.getString(R.string.credit_or_debit),
            binding.commonHeader.headertext
        )
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        dialog = commonMethods.getAlertDialog(this)
        btnAddCard = findViewById(R.id.btnAddCard)

        cardMultilineWidget = findViewById(R.id.stripe)
        cardMultilineWidget.setShouldShowPostalCode(false)

        //  clientSecretKey = intent.getStringExtra("clientSecret")
        /**
         * On back pressed
         */
        binding.commonHeader.arrow.setOnClickListener { onBackPressed() }


        /**
         * Pay through  Stripe Payment
         */
        btnAddCard.setOnClickListener { view ->
            if (commonMethods.stripeInstance() != null) {
                apiService.viewCard(sessionManager.accessToken!!).enqueue(RequestCallback(this))
                // confirmSetupIntentParams()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.stripe_error_1),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Setup intent For Card
     */
    private fun confirmSetupIntentParams() {
        val card = cardMultilineWidget.paymentMethodCard
        val billingDetails = PaymentMethod.BillingDetails.Builder()
            .setName(sessionManager.firstName)
            .setPhone(sessionManager.phoneNumber)
            .build()
        if (card != null && !clientSecretKey.isEmpty()) {
            // Create SetupIntent confirm parameters with the above
            val paymentMethodParams = PaymentMethodCreateParams
                .create(card, billingDetails)
            val confirmParams = ConfirmSetupIntentParams
                .create(paymentMethodParams, clientSecretKey)
            commonMethods.showProgressDialog(this@AddCardActivity)
            commonMethods.stripeInstance()!!.confirmSetupIntent(this, confirmParams)
        } else {
            commonMethods.hideProgressDialog()
            Toast.makeText(this, resources.getString(R.string.stripe_error_2), Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        commonMethods.hideProgressDialog()
        commonMethods.stripeInstance()
            ?.onSetupResult(requestCode, data, object : ApiResultCallback<SetupIntentResult> {
                override fun onError(e: Exception) {
                    Toast.makeText(this@AddCardActivity, e.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                }

                override fun onSuccess(result: SetupIntentResult) {
                    val setupIntent = result.intent
                    val status = setupIntent.status
                    if (status == StripeIntent.Status.Succeeded) {
                        val intent = Intent()
                        intent.putExtra("S_intentId", setupIntent.id)
                        setResult(REQUEST_CODE_PAYMENT, intent)
                        finish()
                    } else if (status == StripeIntent.Status.RequiresPaymentMethod) {
                        Toast.makeText(
                            this@AddCardActivity,
                            resources.getString(R.string.set_canceled),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            })
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String?) {
        val status = commonMethods.getJsonValue(
            jsonResp.strResponse,
            "status_code",
            String::class.java
        ) as String
        if (jsonResp.isSuccess) {
            clientSecretKey = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "intent_client_secret",
                String::class.java
            ) as String
            confirmSetupIntentParams()
            val brand = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "brand",
                String::class.java
            ) as String
            val last4 = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "last4",
                String::class.java
            ) as String

            if (!TextUtils.isEmpty(last4)) {
                sessionManager.cardValue = last4
                sessionManager.cardBrand = brand
                sessionManager.walletCard = 1
            }
        } else if (!status.equals("0")) {
            clientSecretKey = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "intent_client_secret",
                String::class.java
            ) as String
            confirmSetupIntentParams()
        } else {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String?) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent()
        intent.putExtra("S_intentId", "")
        setResult(REQUEST_CODE_PAYMENT, intent)
        finish()
    }

    companion object {

        private val REQUEST_CODE_PAYMENT = 1
    }
}
