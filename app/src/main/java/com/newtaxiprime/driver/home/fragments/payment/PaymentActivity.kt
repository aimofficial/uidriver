package com.newtaxiprime.driver.home.fragments.payment

/**
 * @package com.newtaxi
 * @subpackage views.main.paytoadmin
 * @category Payment Activity
 * @author Seen Technologies
 *
 */


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.Enums
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.ActivityPaymentBinding
import com.newtaxiprime.driver.home.datamodel.PaymentMethodsModel
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import javax.inject.Inject


/*************************************************************
 * PaymentActivity
 * Its used to show the Payment details for the Driver
 */

class PaymentActivity : CommonActivity(), View.OnClickListener, ServiceListener,
    PaymentMethodAdapter.ItemOnClickListener {


    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var customDialog1: CustomDialog

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var gson: Gson

    lateinit var ivCardTick: ImageView

    lateinit var ivCard: ImageView

    lateinit var tvCard: TextView

    lateinit var rltCard: RelativeLayout

    lateinit var mpesa: RelativeLayout

    lateinit var paystack: RelativeLayout

    lateinit var flutterwave: RelativeLayout

    lateinit var paytm: RelativeLayout

    lateinit var razorpay: RelativeLayout

    lateinit var mollie: RelativeLayout

    lateinit var rltAddCard: RelativeLayout
    lateinit private var stripePublishKey: String
    lateinit private var dialog: AlertDialog
    private var clientSecretKey = ""

    private lateinit var paymentmethodadapter: PaymentMethodAdapter
    private var paymentArryalist = ArrayList<PaymentMethodsModel.PaymentMethods>()
    private lateinit var binding: ActivityPaymentBinding

    fun onBack() {
        onBackPressed()
        binding.ivbraintreetick
    }

    /*
  *set payment mode as BrainTree
  */
    fun brainTreeClick() {

        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_BRAINTREE
        binding.apply {
            ivbraintreetick.visibility = View.VISIBLE
            ivCardTick.visibility = View.GONE
            paypalTickimg.visibility = View.GONE
            mpesaTickimg.visibility = View.GONE
            paystackTickimg.visibility = View.GONE
            flutterwaveTickimg.visibility = View.GONE
            razorpayTickimg.visibility = View.GONE
            paytmTickimg.visibility = View.GONE
        }

        onBackPressed()
    }


    /*
 *set payment mode as Mpesa
 */
    fun mpesaClick() {

        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_MPESA
        binding.apply {
            ivbraintreetick.visibility = View.GONE
            ivCardTick.visibility = View.GONE
            paypalTickimg.visibility = View.GONE
            mpesaTickimg.visibility = View.VISIBLE
            paystackTickimg.visibility = View.GONE
            flutterwaveTickimg.visibility = View.GONE
            razorpayTickimg.visibility = View.GONE
            paytmTickimg.visibility = View.GONE
        }


        onBackPressed()
    }

    /*
*set payment mode as Paystack
*/
    fun paystackClick() {

        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_MPESA
        binding.apply {


            ivbraintreetick.visibility = View.GONE
            ivCardTick.visibility = View.GONE
            paypalTickimg.visibility = View.GONE
            mpesaTickimg.visibility = View.GONE
            paystackTickimg.visibility = View.VISIBLE
            flutterwaveTickimg.visibility = View.GONE
            razorpayTickimg.visibility = View.GONE
            paytmTickimg.visibility = View.GONE
        }

        onBackPressed()
    }

    /*
 *set payment mode as Flutterwave
 */
    fun flutterwaveClick() {

        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_FLUTTERWAVE
        binding.apply {
            ivbraintreetick.visibility = View.GONE
            ivCardTick.visibility = View.GONE
            paypalTickimg.visibility = View.GONE
            mpesaTickimg.visibility = View.GONE
            paystackTickimg.visibility = View.GONE
            flutterwaveTickimg.visibility = View.VISIBLE
            razorpayTickimg.visibility = View.GONE
            paytmTickimg.visibility = View.GONE
        }
        onBackPressed()
    }

    /*
 *set payment mode as Razorpay
 */
    fun razorpayClick() {

        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_RAZORPAY
        binding.apply {
            ivbraintreetick.visibility = View.GONE
            ivCardTick.visibility = View.GONE
            paypalTickimg.visibility = View.GONE
            mpesaTickimg.visibility = View.GONE
            paystackTickimg.visibility = View.GONE
            flutterwaveTickimg.visibility = View.GONE
            razorpayTickimg.visibility = View.VISIBLE
            paytmTickimg.visibility = View.GONE
        }
        onBackPressed()
    }

    /*
 *set payment mode as Mollie
 */
    fun mollieClick() {

        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_MOLLIE
        binding.apply {


            ivbraintreetick.visibility = View.GONE
            ivCardTick.visibility = View.GONE
            paypalTickimg.visibility = View.GONE
            mpesaTickimg.visibility = View.GONE
            paystackTickimg.visibility = View.GONE
            flutterwaveTickimg.visibility = View.GONE
            razorpayTickimg.visibility = View.GONE
            mollieTickimg.visibility = View.VISIBLE
            paytmTickimg.visibility = View.GONE
        }
        onBackPressed()
    }


    /*
 *set payment mode as Paytm
 */
    fun paytmClick() {

        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_PAYTM
        binding.apply {


            ivbraintreetick.visibility = View.GONE
            ivCardTick.visibility = View.GONE
            paypalTickimg.visibility = View.GONE
            mpesaTickimg.visibility = View.GONE
            paystackTickimg.visibility = View.GONE
            flutterwaveTickimg.visibility = View.GONE
            razorpayTickimg.visibility = View.GONE
            paytmTickimg.visibility = View.VISIBLE
        }
        onBackPressed()
    }

    /*
*set payment mode as Paypal
*/
    fun paypalClick() {
        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_PAYPAL
        binding.apply {

            ivbraintreetick.visibility = View.GONE
            ivCardTick.visibility = View.GONE
            paypalTickimg.visibility = View.VISIBLE
            mpesaTickimg.visibility = View.GONE
            paystackTickimg.visibility = View.GONE
            flutterwaveTickimg.visibility = View.GONE
            razorpayTickimg.visibility = View.GONE
            paytmTickimg.visibility = View.GONE
        }
        onBackPressed()
    }

    /**
     * Set payment as Card
     */
    fun cardClick() {
        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_CARD
        binding.apply {
            ivbraintreetick.visibility = View.GONE
            ivCardTick.visibility = View.VISIBLE
            paypalTickimg.visibility = View.GONE
            mpesaTickimg.visibility = View.GONE
            paystackTickimg.visibility = View.GONE
            flutterwaveTickimg.visibility = View.GONE
            razorpayTickimg.visibility = View.GONE
            paytmTickimg.visibility = View.GONE
        }

        onBackPressed()
    }


    override fun onItemClick() {
        val stripe = Intent(applicationContext, AddCardActivity::class.java)
        startActivityForResult(stripe, REQUEST_CODE_PAYMENT)
    }

    /**
     * goes to Add Card Page
     */
    fun addCardClick() {
        val stripe = Intent(applicationContext, AddCardActivity::class.java)
        startActivityForResult(stripe, REQUEST_CODE_PAYMENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this)
        commonMethods.setheaderText(
            resources.getString(R.string.payment),
            binding.commonHeader.headertext
        )
        dialog = commonMethods.getAlertDialog(this)
        //showPaymentTickAccordingToTheSelection()
        paymentmethodadapter = PaymentMethodAdapter(this@PaymentActivity, paymentArryalist, this)

         ivCardTick = binding.ivCardTick

        ivCard = binding.ivCard

        tvCard = binding.tvCard

        rltCard = binding.rltCard

        rltCard.setOnClickListener{
            cardClick()
        }
        binding.commonHeader.back.setOnClickListener{
            onBack()
        }

        mpesa = binding.mpesa
        mpesa.setOnClickListener{
            mpesaClick()
        }

        binding.rltbraintree.setOnClickListener{
            brainTreeClick()
        }

        paystack = binding.paystack

        paystack.setOnClickListener{
            paystackClick()
        }

        flutterwave = binding.flutterwave

        flutterwave.setOnClickListener{
            flutterwaveClick()
        }


        paytm = binding.paytm

        paytm.setOnClickListener{
            paytmClick()
        }

        razorpay = binding.razorpay

        razorpay.setOnClickListener{
            razorpayClick()
        }

        mollie = binding.mollie

        mollie.setOnClickListener{
            mollieClick()
        }


        rltAddCard = binding.rltAddCard

        rltAddCard.setOnClickListener{
            addCardClick()
        }
        binding.rvPaymentList.adapter = paymentmethodadapter
        /*  if (!TextUtils.isEmpty(sessionManager.cardValue))
          {
              rltCard.visibility = View.VISIBLE
              setCardImage(sessionManager.cardBrand)
              tvCard.text = "•••• ${sessionManager.cardValue}"

          }*/

        getPaymentMethodList()


        /**
         * View Card Details
         */
        //  commonMethods.showProgressDialog(this)
        //  commonMethods.rotateArrow(ivBack, this)
        println("Token " + sessionManager.accessToken)
        //   apiService.viewCard(sessionManager.accessToken!!).enqueue(RequestCallback(Enums.REQ_VIEW_PAYMENT, this))

    }

    fun getPaymentMethodList() {
        commonMethods.showProgressDialog(this);
        apiService.getPaymentMethodlist(sessionManager.accessToken!!, CommonKeys.isWallet)
            .enqueue(RequestCallback(Enums.REG_GET_PAYMENTMETHOD, this))
        /* paymentArryalist.clear()
         for(i in 0..10)
         {
             if(i==3) paymentArryalist.add(PaymentMethodsModel(i.toString(),"Cash",true))
             paymentArryalist.add(PaymentMethodsModel(i.toString(),"Paypal",false))
         }*/
    }


    /**
     * Result form Add card
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (data != null) {
                val setUp_Id = data.getStringExtra("S_intentId")
                if (setUp_Id != null && !setUp_Id.isEmpty()) {
                    commonMethods.showProgressDialog(this@PaymentActivity)
                    addcard(setUp_Id)
                }
            }
        }
    }

    /**
     * After Stripe payment
     */
    fun addcard(payKey: String) {
        if (!TextUtils.isEmpty(payKey)) {
            //            commonMethods.showProgressDialog(this);
            apiService.addCard(payKey, sessionManager.accessToken!!)
                .enqueue(RequestCallback(Enums.REQ_ADD_CARD, this))
        }
    }

    /**
     * On Success From API
     */
    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {

            Enums.REG_GET_PAYMENTMETHOD -> if (jsonResp.isSuccess) {
                val paymentmodel =
                    gson.fromJson(jsonResp.strResponse, PaymentMethodsModel::class.java)
                var isDefaultpaymentmethod = ""
                paymentArryalist.addAll(paymentmodel.paymentlist)

                if (sessionManager.paymentMethodkey.isNotEmpty()) {
                    for (i in 0 until paymentmodel.paymentlist.size) {

                        CommonKeys.isSetPaymentMethod = true
                        if (sessionManager.paymentMethodkey.equals(paymentmodel.paymentlist.get(i).paymenMethodKey)) {
                            paymentmethodadapter.notifyDataSetChanged()
                            return
                        } else {
                            if (paymentmodel.paymentlist[i].isDefaultPaymentMethod) {
                                CommonKeys.isSetPaymentMethod = false
                            }
                        }
                    }
                    sessionManager.paymentMethodkey = ""
                } else {
                    for (i in 0 until paymentmodel.paymentlist.size) {
                        if (paymentmodel.paymentlist[i].isDefaultPaymentMethod) {
                            CommonKeys.isSetPaymentMethod = false
                            /* sessionManager.paymentMethodkey=paymentmodel.paymentlist[i].paymenMethodKey
                             sessionManager.paymentMethod=paymentmodel.paymentlist[i].paymenMethodvalue
                             sessionManager.paymentMethodImage=paymentmodel.paymentlist[i].paymenMethodIcon*/
                        }
                    }
                }
                paymentmethodadapter.notifyDataSetChanged()

            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }

            Enums.REQ_ADD_CARD -> if (jsonResp.isSuccess) {

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
                    rltCard.visibility = View.VISIBLE
                    setCardImage(brand)
                    tvCard.text = "•••• $last4"
                    ivCardTick.visibility = View.VISIBLE
                    binding.apply {
                        ivbraintreetick.visibility = View.GONE
                        paypalTickimg.visibility = View.GONE
                        mpesaTickimg.visibility = View.GONE
                        paystackTickimg.visibility = View.GONE
                        flutterwaveTickimg.visibility = View.GONE
                        razorpayTickimg.visibility = View.GONE
                        paytmTickimg.visibility = View.GONE
                    }
                    sessionManager.paymentMethod = CommonKeys.PAYMENT_CARD
                    sessionManager.paymentMethodkey = CommonKeys.PAYMENT_CARD
                    sessionManager.paymentMethodImage = ""
                    sessionManager.walletCard = 1
                    sessionManager.cardValue = last4
                    sessionManager.cardBrand = brand
                } else {
                    rltCard.visibility = View.GONE
                }
                onBackPressed();
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }

            Enums.REQ_VIEW_PAYMENT -> if (jsonResp.isSuccess) {
                clientSecretKey = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "intent_client_secret",
                    String::class.java
                ) as String
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
                    rltCard.visibility = View.VISIBLE
                    setCardImage(brand)
                    tvCard.text = "•••• $last4"
                    sessionManager.cardValue = last4
                    sessionManager.cardBrand = brand
                    sessionManager.walletCard = 1
                    ivCardTick.visibility = View.GONE
                } else {
                    rltCard.visibility = View.GONE
                }
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg) && jsonResp.statusMsg.equals(
                    "No record found",
                    ignoreCase = true
                )
            ) {
                clientSecretKey = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "intent_client_secret",
                    String::class.java
                ) as String
            } else {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }

            else -> {
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    /**
     * Set Card Images
     */
    fun setCardImage(brand: String?) {
        if ("Visa".contains(brand!!)) {
            ivCard.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.card_visa
                )
            )
        } else if ("MasterCard".contains(brand)) {
            ivCard.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.card_master
                )
            )
        } else if ("Discover".contains(brand)) {
            ivCard.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.card_discover
                )
            )
        } else if (brand.contains("Amex") || brand.contains("American Express")) {
            ivCard.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.card_amex
                )
            )
        } else if (brand.contains("JCB") || brand.contains("JCP")) {
            ivCard.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.card_jcp
                )
            )
        } else if (brand.contains("Diner") || brand.contains("Diners")) {
            ivCard.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.card_diner
                )
            )
        } else if ("Union".contains(brand) || "UnionPay".contains(brand)) {
            ivCard.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.card_unionpay
                )
            )
        } else {
            ivCard.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_card
                )
            )
        }
    }

    override fun onClick(v: View) {

    }

    companion object {


        private val REQUEST_CODE_PAYMENT = 1
    }


}




