package com.newtaxiprime.driver.home.fragments.Referral

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys.CompletedReferralArray
import com.newtaxiprime.driver.common.util.CommonKeys.IncompleteReferralArray
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.AppActivityShowReferralOptionsBinding
import com.newtaxiprime.driver.home.datamodel.ReferredFriendsModel
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import javax.inject.Inject

class ShowReferralOptionsActivity : CommonActivity(), ServiceListener {

    lateinit var dialog: AlertDialog

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    lateinit var rvIncompletedReferrals: RecyclerView

    lateinit var rvCompletedReferrals: RecyclerView


    lateinit var cvIncompleteFriends: ConstraintLayout

    lateinit var cvCompleteFriends: ConstraintLayout

    lateinit var cvReferralHeader: ConstraintLayout

    lateinit var tvReferralCode: TextView

    lateinit var tv_share_option: ImageView


    lateinit var tvTotalEarned: TextView

    lateinit var tvEarnedAmount: TextView

    lateinit var tvReferralBenifitStatement: TextView

    lateinit var rltShare: RelativeLayout

    lateinit var scvReferal: ScrollView

    lateinit var remainingReferral: TextView


    lateinit var tvNoReferralsYet: TextView

    private var referralCode = ""
    private var referralLink = ""
    private lateinit var referredFriendsModel: ReferredFriendsModel

    lateinit var binding: AppActivityShowReferralOptionsBinding


    fun copy() {
        connect()
    }


    fun share() {
        shareMyReferralCode()
    }


    fun backPressed() {
        onBackPressed()
    }


    fun connect() {
        CommonMethods.copyContentToClipboard(this, referralCode)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityShowReferralOptionsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        rvIncompletedReferrals = binding.rvInCompletedReferrals
        rvCompletedReferrals = binding.rvCompletedReferrals


        cvIncompleteFriends = binding.constraintLayoutInCompletedFriends

        cvCompleteFriends = binding.constraintLayoutCompletedFriends

        cvReferralHeader = binding.constraintLayoutReferralCode

        tvReferralCode = binding.tvReferralCode

        tv_share_option = binding.imagShare
        tv_share_option.setOnClickListener{
            share()
        }

        binding.imagCopy.setOnClickListener{
            copy()
        }
        binding.commonHeader.back.setOnClickListener{
            backPressed()
        }
        tvTotalEarned = binding.tvTotalEarned

        tvEarnedAmount = binding.tvAmount

        tvReferralBenifitStatement = binding.tvReferralBenifitText

        rltShare = binding.rltShare

        scvReferal = binding.scvReferal

        remainingReferral = binding.remaingReferralAmount


        tvNoReferralsYet = binding.tvNoReferralsYet
        commonMethods.setheaderText(
            resources.getString(R.string.referral),
            binding.commonHeader.headertext
        )
        //commonMethods.imageChangeforLocality(this,arrow)
        dialog = commonMethods.getAlertDialog(this)
        scvReferal.visibility = View.GONE
        initView()
    }

    /**
     * init Views For Referral
     */
    private fun initView() {
        showOrHideReferralAccordingToSessionData()
        getReferralInformationFromAPI()
    }

    /**
     * Hide and Show the Referral  Based on Referral Enable
     */
    private fun showOrHideReferralAccordingToSessionData() {
        if (sessionManager.isReferralOptionEnabled) {
            cvReferralHeader.visibility = View.VISIBLE
        } else {
            cvReferralHeader.visibility = View.GONE
        }
    }

    /**
     * get Referral info for user
     */
    private fun getReferralInformationFromAPI() {
        commonMethods.showProgressDialog(this)
        apiService.getReferralDetails(sessionManager.accessToken!!).enqueue(RequestCallback(this))
    }


    /**
     * Get My Referral Code
     */
    fun shareMyReferralCode() {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(
            Intent.EXTRA_SUBJECT,
            resources.getString(R.string.app_name) + " " + resources.getString(R.string.referral)
        )
        share.putExtra(
            Intent.EXTRA_TEXT,
            resources.getString(R.string.invite_msg) + " " + spannableString(referralCode) + " " + referralLink
        )
        startActivity(Intent.createChooser(share, resources.getString(R.string.share_my_code)))
    }

    /**
     * Combine Your Referral Code
     */
    private fun spannableString(referralCode: String): String {
        val ss = SpannableString(referralCode)
        ss.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            referralCode.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return ss.toString()
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (jsonResp.isSuccess) {
            scvReferal.visibility = View.VISIBLE
            onSuccessResult(jsonResp.strResponse)
            //jsonResp.strResponse.let { onSuccessResult(it) }
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    /**
     * onSuccessResponse of the Referral
     */
    private fun onSuccessResult(strResponse: String) {
        referredFriendsModel = gson.fromJson(strResponse, ReferredFriendsModel::class.java)
        if (!TextUtils.isEmpty(referredFriendsModel.remainingReferral)) {
            remainingReferral.text = referredFriendsModel.remainingReferral
        } else {
            remainingReferral.text = sessionManager.currencySymbol + "0"
        }
        updateReferralCodeInUI()
        if (referredFriendsModel.pendingReferrals?.size != 0 || referredFriendsModel.completedReferrals?.size != 0) {
            showReferralsNotAvailable(true)
            proceedCompleteReferralDetails()
            proceedIncompleteReferralDetails()
        } else {
            showReferralsNotAvailable(false)
        }
    }

    /**
     * Update Your Referral UI
     */
    private fun updateReferralCodeInUI() {
        referralCode = referredFriendsModel.referralCode.toString()
        referralLink = referredFriendsModel.referralLink.toString()
        tvReferralCode.text = referralCode
        if ("1".equals(resources.getString(R.string.layout_direction), ignoreCase = true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvReferralBenifitStatement.text = resources.getString(
                    R.string.max_referral_earning_statement,
                    setCurrencyFrontForRTL(
                        Html.fromHtml(
                            referredFriendsModel.referralAmount,
                            Html.FROM_HTML_MODE_LEGACY
                        ).toString()
                    )
                )
            } else {
                tvReferralBenifitStatement.text = resources.getString(
                    R.string.max_referral_earning_statement,
                    setCurrencyFrontForRTL(
                        Html.fromHtml(referredFriendsModel.referralAmount).toString()
                    )
                )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvReferralBenifitStatement.text = resources.getString(
                    R.string.max_referral_earning_statement,
                    Html.fromHtml(referredFriendsModel.referralAmount, Html.FROM_HTML_MODE_LEGACY)
                        .toString()
                )
            } else {
                tvReferralBenifitStatement.text = resources.getString(
                    R.string.max_referral_earning_statement,
                    Html.fromHtml(referredFriendsModel.referralAmount).toString()
                )
            }
        }
        //tvTotalEarned.append(referredFriendsModel.getTotalEarning().toString());
        tvEarnedAmount.text = referredFriendsModel.totalEarning
    }

    /**
     * Currency symbol  RTL for Amount
     */
    private fun setCurrencyFrontForRTL(amount: String): String {
        println("amount $amount")
        val currency = amount[0].toString()
        println("currency $currency")
        return amount.replace(currency, " ") + currency
    }

    /**
     * Referral Hide
     */
    private fun showReferralsNotAvailable(show: Boolean) {
        if (show) {
            cvIncompleteFriends.visibility = View.VISIBLE
            cvCompleteFriends.visibility = View.VISIBLE
            tvNoReferralsYet.visibility = View.GONE
        } else {
            cvIncompleteFriends.visibility = View.GONE
            cvCompleteFriends.visibility = View.GONE
            tvNoReferralsYet.visibility = View.VISIBLE
        }
    }

    /**
     * InComplete ReferralDetails
     */
    private fun proceedIncompleteReferralDetails() {
        if (referredFriendsModel.pendingReferrals?.size != 0) {
            rvIncompletedReferrals.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(this)
            rvIncompletedReferrals.layoutManager = layoutManager
            rvIncompletedReferrals.adapter = ReferralFriendsListRecyclerViewAdapter(
                this,
                referredFriendsModel.pendingReferrals,
                IncompleteReferralArray
            )
        } else {
            cvIncompleteFriends.visibility = View.GONE
        }
    }

    /**
     * Proceed Completed ReferralDetails
     */
    private fun proceedCompleteReferralDetails() {
        if (referredFriendsModel.completedReferrals?.size != 0) {
            rvCompletedReferrals.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(this)
            rvCompletedReferrals.layoutManager = layoutManager
            rvCompletedReferrals.adapter = ReferralFriendsListRecyclerViewAdapter(
                this,
                referredFriendsModel.completedReferrals,
                CompletedReferralArray
            )
        } else {
            cvCompleteFriends.visibility = View.GONE
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

    }
}