package com.newtaxiprime.driver.home.fragments.payment

/**
 * @package com.newtaxiprime.user
 * @subpackage Side_Bar.payment
 * @category PaymentPage
 * @author Seen Technologies
 *
 */


//import com.newtaxiprime.driver.common.util.Enums.REQ_ADD_PROMO
//import com.newtaxiprime.driver.common.util.Enums.REQ_GET_PROMO
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.Enums
import com.newtaxiprime.driver.common.util.Enums.REQ_ADD_CARD
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.AppActivityPaymentPageBinding
import com.newtaxiprime.driver.home.datamodel.PaymentMethodsModel
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


/* ************************************************************
    Rider can select the payment method
    *********************************************************** */
class PaymentPage : CommonActivity(), ServiceListener, PaymentMethodAdapter.ItemOnClickListener {


    lateinit var dialog2: AlertDialog

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

    lateinit var wallet_amt: TextView

    lateinit var promo_count: TextView

    lateinit var addCreditOrDebitCardTextView: TextView


    lateinit var alreadyAvailableCardNumber: TextView

    lateinit var arrow: ImageView

    lateinit var wallettick_img: ImageView

    lateinit var paypaltick_img: ImageView

    lateinit var cashtick_img: ImageView

    lateinit var cardtick_img: ImageView

    lateinit var imgViewAlreadyAvailableCardimg: ImageView

    lateinit var cashLayout: RelativeLayout

    lateinit var wallet: RelativeLayout

    lateinit var alreadyAvailableCardDetails: RelativeLayout

    lateinit var promo: RelativeLayout

    lateinit var completePromotionsLayoutIncludingTitle: RelativeLayout

    lateinit var completeWalletLayoutIncludingTitle: RelativeLayout
    lateinit var dialog: BottomSheetDialog
    lateinit var input_promo_code: EditText
    lateinit var cancel_promo_btn: Button
    lateinit var add_promo_btn: Button
    protected var isInternetAvailable: Boolean = false

    lateinit var ivCardTick: ImageView

    lateinit var rltCard: RelativeLayout

    lateinit var binding: AppActivityPaymentPageBinding

    lateinit private var stripePublishKey: String
    lateinit private var stripeToken: String

    lateinit var ivbraintreetick: ImageView

    lateinit var rltBraintree: RelativeLayout
    private var clientSecretKey = ""

    private lateinit var paymentmethodadapter: PaymentMethodAdapter
    private var paymentArryalist = ArrayList<PaymentMethodsModel.PaymentMethods>()


    override fun onItemClick() {
        val stripe =
            Intent(this, com.newtaxiprime.driver.home.fragments.payment.AddCardActivity::class.java)
        startActivityForResult(stripe, CommonKeys.REQUEST_CODE_PAYMENT)
    }

    fun cardclick() {
        /**
         * Payment method paypal clicked
         */
        showPaymentTickAccordingToTheSelection()
        sessionManager.walletPaymentMethod = CommonKeys.PAYMENT_CARD
        sessionManager.paymentMethod = CommonKeys.PAYMENT_CARD
        onBackPressed()
    }

    fun brainTreeclick() {
        /**
         * Payment method paypal clicked
         */
        showPaymentTickAccordingToTheSelection()
        sessionManager.walletPaymentMethod = CommonKeys.PAYMENT_BRAINTREE
        sessionManager.paymentMethod = CommonKeys.PAYMENT_BRAINTREE
        onBackPressed()
    }


    fun payPalclick() {
        /**
         * Payment method paypal clicked
         */
        showPaymentTickAccordingToTheSelection()
        sessionManager.walletPaymentMethod = CommonKeys.PAYMENT_PAYPAL
        sessionManager.paymentMethod = CommonKeys.PAYMENT_PAYPAL
        onBackPressed()
    }


    fun creditOrDebitCard() {
        val stripe =
            Intent(this, com.newtaxiprime.driver.home.fragments.payment.AddCardActivity::class.java)
        startActivityForResult(stripe, CommonKeys.REQUEST_CODE_PAYMENT)

    }

    fun selectPaymentAsCard() {
        showPaymentTickAccordingToTheSelection()
        sessionManager.walletPaymentMethod = CommonKeys.PAYMENT_CARD
        sessionManager.paymentMethod = CommonKeys.PAYMENT_CARD
        onBackPressed()
    }

    fun addPromoclick() {
        /**
         * Add promo code
         */
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        addPromo()
    }

    private var mLastClickTime: Long = 0

    fun wallet() {
        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_WALLET
        ivbraintreetick.visibility = View.GONE
        ivCardTick.visibility = View.GONE
        binding.paypalTickimg.visibility = View.VISIBLE
        onBackPressed()
    }


    fun arrow() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // common declerations
        super.onCreate(savedInstanceState)
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this)
        binding = AppActivityPaymentPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wallet_amt = binding.tvCallerName

        promo_count = binding.promoCount

        addCreditOrDebitCardTextView = binding.tvAddOrChangeCard

        alreadyAvailableCardNumber = binding.tvAlreadyAvailableCardNumber

        arrow = binding.commonHeader.arrow
        wallettick_img = binding.walletTickimg

        paypaltick_img = binding.paypalTickimg

        cashtick_img = binding.cashTickimg

        cardtick_img = binding.imgViewAlreadyAvailableCardTickimg

        imgViewAlreadyAvailableCardimg = binding.imgViewAlreadyAvailableCardimg

        cashLayout = binding.cash
        wallet = binding.wallet
        wallet.setOnClickListener {
            wallet()
        }

        binding.commonHeader.arrow.setOnClickListener {
            arrow()
        }

        binding.addPromo.setOnClickListener {
            addPromoclick()
        }
        binding.alreadyAvailableCreditOrDebitCard.setOnClickListener {
            selectPaymentAsCard()
        }

        binding.addCreditOrDebitCard.setOnClickListener {
            creditOrDebitCard()
        }

        binding.paypal.setOnClickListener {
            payPalclick()
        }

        binding.rltbraintree.setOnClickListener {
            brainTreeclick()
        }

        binding.rltcard.setOnClickListener{
            cardclick()
        }

        alreadyAvailableCardDetails = binding.alreadyAvailableCreditOrDebitCard

        promo = binding.promo

        completePromotionsLayoutIncludingTitle = binding.rltPromotions

        completeWalletLayoutIncludingTitle = binding.rltWallet

        ivCardTick = binding.ivCardTick

        rltCard = binding.rltcard
        ivbraintreetick = binding.ivbraintreetick

        rltBraintree = binding.rltbraintree

        /**Commmon Header Text View */
        commonMethods.setheaderText(
            resources.getString(R.string.payment),
            binding.commonHeader.headertext
        )
        dialog2 = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        proceedActivityAccordingToType(
            intent.getIntExtra(
                CommonKeys.TYPE_INTENT_ARGUMENT_KEY,
                CommonKeys.StatusCode.startPaymentActivityForView
            )
        )
        paymentmethodadapter = PaymentMethodAdapter(this@PaymentPage, paymentArryalist, this)
        binding.rvPaymentList.adapter = paymentmethodadapter

        getPaymentMethodList()

        /* if(!sessionManager.cardValue.isEmpty())
         {
             alreadyAvailableCardDetails.visibility = View.VISIBLE
             imgViewAlreadyAvailableCardimg.setImageDrawable(CommonMethods.getCardImage(sessionManager.cardBrand, resources))

             alreadyAvailableCardNumber.text = "•••• ${sessionManager.cardValue}"
             addCreditOrDebitCardTextViewLabelToChangeCard()
         }else
         {
             alreadyAvailableCardDetails.visibility = View.GONE
             addCreditOrDebitCardTextViewLabelToAddCard()
         }*/

        //start activity views according to the type


    }

    fun getPaymentMethodList() {
        /* // val paymentdetails=JSONArray( sessionManager.paymentMethodDetail) as PaymentMethodsModel.PaymentMethods
           val collectionType = object : TypeToken<Collection<PaymentMethodsModel.PaymentMethods>>() {}.getType()
           paymentArryalist.addAll(gson.fromJson(sessionManager.paymentMethodDetail,collectionType))
           paymentmethodadapter.notifyDataSetChanged()*/
        //  val paymentMethod:PaymentMethodsModel.PaymentMethods=gson.fromJson(paymentdetails.toString(),PaymentMethodsModel.PaymentMethods::class.java)
        // println("paymentArraylist ${paymentArryalist.size}")
        CommonKeys.isFirstSetpaymentMethod = true
        apiService.getPaymentMethodlist(sessionManager.accessToken!!, CommonKeys.isWallet)
            .enqueue(RequestCallback(Enums.REG_GET_PAYMENTMETHOD, this))
    }

    private fun proceedActivityAccordingToType(@CommonKeys.StatusCode type: Int) {
        when (type) {
            CommonKeys.StatusCode.startPaymentActivityForView -> {
                setVisibilityForCompletelyViewActivity()
            }

            CommonKeys.StatusCode.startPaymentActivityForAddMoneyToWallet -> {
                run { startPaymentActivityForAddMoneyToWallet() }
                run {
                    startPaymentActivityForChangePaymentOption()
                }
            }

            CommonKeys.StatusCode.startPaymentActivityForChangePaymentOption -> {
                startPaymentActivityForChangePaymentOption()
            }

            else -> {
            }
        }
        getPromoCode()
    }

    private fun startPaymentActivityForAddMoneyToWallet() {
        CommonKeys.isWallet = 1
        hideWalletAndPromotionsLayout()


    }

    private fun startPaymentActivityForChangePaymentOption() {
        hideWalletAndPromotionsLayout()
    }

    private fun hideWalletAndPromotionsLayout() {
        completeWalletLayoutIncludingTitle.visibility = View.GONE
        completePromotionsLayoutIncludingTitle.visibility = View.GONE
    }

    fun showPaymentTickAccordingToTheSelection() {
        when (sessionManager.paymentMethod) {
            CommonKeys.PAYMENT_PAYPAL -> {
                cardtick_img.setVisibility(View.GONE);
                ivCardTick.visibility = View.GONE

                paypaltick_img.visibility = View.VISIBLE
                ivbraintreetick.visibility = View.GONE


            }

            CommonKeys.PAYMENT_CARD -> {
                cardtick_img.visibility = View.VISIBLE

                paypaltick_img.visibility = View.GONE
                ivbraintreetick.visibility = View.GONE
                ivCardTick.visibility = View.VISIBLE
            }

            CommonKeys.PAYMENT_BRAINTREE -> {
                ivbraintreetick.visibility = View.VISIBLE

                paypaltick_img.visibility = View.GONE
                cardtick_img.setVisibility(View.GONE);
                ivCardTick.visibility = View.VISIBLE

                paypaltick_img.visibility = View.GONE
            }

            else -> {
                sessionManager.paymentMethod = CommonKeys.PAYMENT_PAYPAL
                sessionManager.isWallet = true
                cardtick_img.visibility = View.GONE
                ivbraintreetick.visibility = View.GONE
                cashtick_img.visibility = View.GONE
                paypaltick_img.visibility = View.VISIBLE
            }
        }
    }

    fun setVisibilityForCompletelyViewActivity() {
        CommonKeys.isWallet = 0
        //set wallet symbol and balance
        //val walletAmount=100

        wallet_amt.text = sessionManager.currencySymbol + sessionManager.walletAmount


        if (sessionManager.isWallet) {
            wallettick_img.isEnabled = true
            wallettick_img.visibility = View.VISIBLE
        } else {
            wallettick_img.isEnabled = false
            wallettick_img.visibility = View.GONE
        }

        /*if (getIntent().getStringExtra("type").equals("wallet")) {
            if (sessionManager.getWalletPaymentMethod() != null
                    && sessionManager.getPaymentMethod().equals("PayPal")) {
                paypaltick_img.setVisibility(View.VISIBLE);
            } else {
                paypaltick_img.setVisibility(View.GONE);
            }
            cashLayout.setVisibility(View.GONE);
            wallet.setVisibility(View.GONE);
        }*/
    }

    override fun onBackPressed() {
        super.onBackPressed()  //Back button pressed
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    /**
     * on Activity closed
     */
    public override fun onDestroy() {
        super.onDestroy()
    }
    // method to check if the device is connected to network

    /**
     * Add promo code
     */
    fun addPromo() {

        dialog = BottomSheetDialog(this@PaymentPage, R.style.BottomSheetDialogTheme)
        //dialog.setContentView(R.layout.app_add_promo)

        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BottomSheetDialog d = (BottomSheetDialog) dialog;
                        FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
                        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                },0);
            }
        });*/

        //cancel_promo_btn = (dialog.findViewById<View>(R.id.cancel_promo) as Button?)!!
        //add_promo_btn = (dialog.findViewById<View>(R.id.add_promo) as Button?)!!
        //input_promo_code = (dialog.findViewById<View>(R.id.input_promo_code) as EditText?)!!
        //input_promo_code.clearFocus()

        /**
         * Call promo code check API
         */
        add_promo_btn.setOnClickListener {
            if (input_promo_code.text.length > 0) {
                val promo_code = input_promo_code.text.toString()

                isInternetAvailable = commonMethods.isOnline(applicationContext)
                if (isInternetAvailable) {
                    addPromoCode(promo_code)
                } else {
                    commonMethods.showMessage(
                        this@PaymentPage,
                        dialog2,
                        getString(R.string.no_connection)
                    )
                }

                dialog.dismiss()
            } else {
                dialog.dismiss()
                //commonMethods.showMessage(this@PaymentPage, dialog2, getString(R.string.please_enter_promo))
            }
        }
        cancel_promo_btn.setOnClickListener { dialog.dismiss() }

        if (!dialog.isShowing) {
            dialog.show()

        }
    }

    /**
     * Verify promo code API called
     */
    fun getPromoCode() {
        if (isInternetAvailable) {
            commonMethods.showProgressDialog(this)
            //apiService.promoDetails(sessionManager.accessToken!!).enqueue(RequestCallback(REQ_GET_PROMO, this))
        } else {
            commonMethods.showMessage(this@PaymentPage, dialog2, getString(R.string.no_connection))
        }
    }

    fun addPromoCode(code: String) {
        commonMethods.showProgressDialog(this)
        // apiService.addPromoDetails(sessionManager.accessToken!!, code).enqueue(RequestCallback(REQ_ADD_PROMO, this))
    }

    fun addCardDetail(payKey: String) {
        if (!TextUtils.isEmpty(payKey)) {
            apiService.addCard(payKey, sessionManager.accessToken!!)
                .enqueue(RequestCallback(Enums.REQ_ADD_CARD, this))
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonKeys.REQUEST_CODE_PAYMENT) {
            if (data != null) {
                val setUp_Id = data?.getStringExtra("S_intentId")
                if (setUp_Id != null && !setUp_Id.isEmpty()) {
                    commonMethods.showProgressDialog(this@PaymentPage)
                    addCardDetail(setUp_Id)
                }
            }
        }
    }

    /**
     * After Stripe payment
     *
     */
    fun paymentCompleted(setUpId: String) {
        if (!TextUtils.isEmpty(setUpId))
            apiService.addCard(setUpId, sessionManager.accessToken!!)
                .enqueue(RequestCallback(Enums.REQ_ADD_CARD, this))
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog2, data)
            return
        }
        when (jsonResp.requestCode) {
            // Get Promo Details
            // REQ_GET_PROMO -> if (jsonResp.isSuccess) {
            // commonMethods.hideProgressDialog()
            // onSuccessPromo(jsonResp)
            //} else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            // commonMethods.hideProgressDialog()
            //commonMethods.showMessage(this, dialog2, jsonResp.statusMsg)
            // }
            REQ_ADD_CARD -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                onSuccessCard(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog2, jsonResp.statusMsg)
            }

            Enums.REG_GET_PAYMENTMETHOD -> if (jsonResp.isSuccess) {
                val paymentmodel =
                    gson.fromJson(jsonResp.strResponse, PaymentMethodsModel::class.java)
                var isDefaultpaymentmethod = ""
                sessionManager.paymentMethodDetail = gson.toJson(paymentmodel.paymentlist)
                paymentArryalist.addAll(paymentmodel.paymentlist)

                if (sessionManager.paymentMethodkey!!.isNotEmpty()) {
                    for (i in 0 until paymentmodel.paymentlist.size) {
                        CommonKeys.isSetPaymentMethod = true
                        if (sessionManager.paymentMethodkey.equals(
                                paymentmodel.paymentlist.get(i).paymenMethodKey,
                                ignoreCase = true
                            )
                        ) {
                            val paymentmode = paymentmodel.paymentlist.get(i)
                            sessionManager.walletPaymentMethod = paymentmode.paymenMethodvalue
                            sessionManager.paymentMethod = paymentmode.paymenMethodvalue
                            sessionManager.walletPaymentMethodkey = paymentmode.paymenMethodKey
                            sessionManager.paymentMethodkey = paymentmode.paymenMethodKey
                            sessionManager.paymentMethodImage = paymentmode.paymenMethodIcon
                            paymentmethodadapter.notifyDataSetChanged()
                            return
                        } else {
                            if (paymentmodel.paymentlist[i].isDefaultPaymentMethod) {
                                CommonKeys.isSetPaymentMethod = false
                            }
                        }
                    }
                    CommonKeys.isSetPaymentMethod = false
                    sessionManager.paymentMethodkey = ""
                    sessionManager.walletPaymentMethodkey = ""
                } else {
                    for (i in 0 until paymentmodel.paymentlist.size) {
                        if (paymentmodel.paymentlist[i].isDefaultPaymentMethod) {
                            CommonKeys.isSetPaymentMethod = false
                            /*  sessionManager.walletPaymentMethod=paymentmodel.paymentlist[i].paymenMethodvalue
                              sessionManager.walletPaymentMethodkey=paymentmodel.paymentlist[i].paymenMethodKey*/
                            /*   sessionManager.paymentMethod=paymentmodel.paymentlist[i].paymenMethodvalue
                               sessionManager.paymentMethodkey=paymentmodel.paymentlist[i].paymenMethodKey
                               sessionManager.paymentMethodImage=paymentmodel.paymentlist[i].paymenMethodIcon*/

                        }
                    }

                }
                paymentmethodadapter.notifyDataSetChanged()

            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog2, jsonResp.statusMsg)
            }
            // Add Promo Details
            //REQ_ADD_PROMO -> if (jsonResp.isSuccess) {
            // commonMethods.hideProgressDialog()
            //onSuccessPromo(jsonResp)
            // else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            // commonMethods.hideProgressDialog()
            //commonMethods.showMessage(this, dialog2, jsonResp.statusMsg)
            // }
            else -> {
            }
        }
    }

    private fun onSuccessCard(jsonResp: JsonResponse) {
        if (jsonResp.isSuccess) {
            commonMethods.hideProgressDialog()
            val response = JSONObject(jsonResp.strResponse)
            if (response.has("last4") && !TextUtils.isEmpty(response.getString("last4"))) {
                val alreadyAvailableCardLast4Digits = response.getString("last4")
                val alreadyAddedCardbrand = response.getString("brand")

                sessionManager.cardValue = alreadyAvailableCardLast4Digits
                sessionManager.cardBrand = alreadyAddedCardbrand
                sessionManager.walletPaymentMethodkey = CommonKeys.PAYMENT_CARD
                sessionManager.paymentMethodkey = CommonKeys.PAYMENT_CARD
                sessionManager.paymentMethod = "•••• $alreadyAvailableCardLast4Digits"
                sessionManager.walletPaymentMethod = "•••• $alreadyAvailableCardLast4Digits"
                sessionManager.paymentMethodImage = ""
                alreadyAvailableCardDetails.visibility = View.VISIBLE
                imgViewAlreadyAvailableCardimg.setImageDrawable(
                    CommonMethods.getCardImage(
                        alreadyAddedCardbrand,
                        resources
                    )
                )
                //setCardImage(brand);
                alreadyAvailableCardNumber.text = "•••• $alreadyAvailableCardLast4Digits"
                addCreditOrDebitCardTextViewLabelToChangeCard()
                onBackPressed()
            } else {

                alreadyAvailableCardDetails.visibility = View.GONE
                addCreditOrDebitCardTextViewLabelToAddCard()
            }
        }

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog2, jsonResp.statusMsg)
        }
    }

    fun onSuccessPromo(jsonResp: JsonResponse) {
        try {
            //   clientSecretKey = commonMethods.getJsonValue(jsonResp.strResponse, "intent_client_secret", String::class.java) as String

            val response = JSONObject(jsonResp.strResponse)
            //if (response.has("promo_details")) {
            // val promocount = response.getJSONArray("promo_details").length()
            //sessionManager.promoDetail = response.getString("promo_details")
            //sessionManager.promoCount = promocount
            // }
            if (response.has("stripe_key")) {
                stripePublishKey = response.getString("stripe_key")
            }

            if (response.has("wallet_amount")) {
                try {
                    sessionManager.walletAmount = response.getString("wallet_amount")
                    wallet_amt.text = sessionManager.currencySymbol + sessionManager.walletAmount
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        } catch (j: JSONException) {
            j.printStackTrace()
        }

        /*if (sessionManager.promoCount == 0) {
            promo.visibility = View.GONE
        } else if (sessionManager.promoCount > 0) {
            promo.visibility = View.VISIBLE
            promo_count.text = sessionManager.promoCount.toString()
        } else {
            promo.visibility = View.GONE
        }*/
    }

    fun addCreditOrDebitCardTextViewLabelToAddCard() {
        addCreditOrDebitCardTextView.text = getString(R.string.credit_or_debit)
    }

    fun addCreditOrDebitCardTextViewLabelToChangeCard() {
        addCreditOrDebitCardTextView.text = getString(R.string.change_credit_or_debit_card)

    }

}
