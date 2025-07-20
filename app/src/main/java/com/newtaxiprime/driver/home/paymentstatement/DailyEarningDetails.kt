package com.newtaxiprime.driver.home.paymentstatement


import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.custompalette.FontTextView
import com.newtaxiprime.driver.common.database.Sqlite
import com.newtaxiprime.driver.common.helper.Constants
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.Enums.REQ_DAILY_STATEMENT
import com.newtaxiprime.driver.common.util.PaginationScrollListener
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.ActivityDailyEarningDetailsBinding
import com.newtaxiprime.driver.home.datamodel.DailyStatement
import com.newtaxiprime.driver.home.datamodel.InvoiceContent
import com.newtaxiprime.driver.home.datamodel.WeeklyStatementModel
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.PaginationAdapterCallback
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.newtaxiprime.driver.trips.tripsdetails.TripDetails
import org.json.JSONException
import java.util.*
import javax.inject.Inject

/* ************************************************************
                DailyEarningDetails
Its used to view the list dailyearningdetails
*************************************************************** */
class DailyEarningDetails : CommonActivity(), ServiceListener,
    DailyHoursPaginationAdapter.DailyEarngCallback, PaginationAdapterCallback {

    private var isViewUpdatedWithLocalDB: Boolean = false

    @Inject
    lateinit var dbHelper: Sqlite


    lateinit var listView: RecyclerView

    internal lateinit var tvDay: FontTextView

    internal lateinit var tvDailyAmt: FontTextView

    internal lateinit var tvBaseFare: FontTextView

    internal lateinit var tvAccessFare: FontTextView

    internal lateinit var tvTimeFare: FontTextView

    internal lateinit var tvNewtaxiEarn: FontTextView

    internal lateinit var tvCashColltd: FontTextView

    internal lateinit var tvBankDep: FontTextView

    internal lateinit var tvTimeOnline: FontTextView

    internal lateinit var tvCmptdTrip: FontTextView

    internal lateinit var tvCmptdTripTitle: FontTextView

    internal lateinit var rlTimeFare: RelativeLayout

    internal lateinit var nsvWhole: NestedScrollView

    internal lateinit var rvDailyStamt: RecyclerView

    internal lateinit var tvTitle: FontTextView

    lateinit var binding: ActivityDailyEarningDetailsBinding


    @Inject
    internal lateinit var commonMethods: CommonMethods

    @Inject
    internal lateinit var customDialog: CustomDialog

    @Inject
    internal lateinit var apiService: ApiService

    @Inject
    internal lateinit var sessionManager: SessionManager
    internal lateinit var dialog: AlertDialog

    @Inject
    internal lateinit var gson: Gson
    internal lateinit var dailyStatement: DailyStatement
    internal lateinit var dailyDriverStatement: DailyStatement.Driver_statement
    internal var dailyStatementList: MutableList<DailyStatement.Statement> = ArrayList()
    var dailyHoursPaginationAdapter: DailyHoursPaginationAdapter? = null
    var Paydate = arrayOf("5.36 AM", "6.10 AM", "7.05 AM", "9.00AM", "1.00PM", "4.35PM", "6.00PM")
    var Payamount =
        arrayOf("$92.88", "$82.88", "$72.88", "$9002.88", "$902.88", "$92.88", "$892.88")
    private val searchlist: MutableList<PayModel>? = null
    internal lateinit var weeklyDriverStatement: WeeklyStatementModel.DriverStatement
    internal lateinit var dailyDriverStatementContent: ArrayList<InvoiceContent>

    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES = 0
    private var currentPage = Constants.PAGE_START

    private var dailyDate: String? = null

    fun onBack() {
        onBackPressed()
    }

    private fun dailyEarngApiCall(showLoader: Boolean) {
        if (commonMethods.isOnline(this)) {
            if (currentPage == 1) {
                if (showLoader) {
                    commonMethods.showProgressDialog(this)
                }
            }
            apiService.dailyStatement(
                sessionManager.accessToken!!,
                dailyDate!!,
                TimeZone.getDefault().id,
                currentPage.toString()
            ).enqueue(RequestCallback(REQ_DAILY_STATEMENT, this@DailyEarningDetails))
        } else {
            CommonMethods.showInternetNotAvailableForStoredDataViewer(this)
        }
    }

    private fun getIntents() {
        val intent = intent
        if (intent.hasExtra("daily_date")) {
            dailyDate = intent.getStringExtra("daily_date").toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyEarningDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        /*common header text*/

        listView = binding.tripearningList

        tvDay = binding.tvDay
        tvDailyAmt = binding.tvDailyAmt

        tvBaseFare = binding.tvBaseFare

        tvAccessFare = binding.tvAccessFare

        tvTimeFare = binding.tvTimeFare

        tvNewtaxiEarn = binding.tvNewtaxiEarn

        tvCashColltd = binding.tvCashColltd

        tvBankDep = binding.tvBankDep

        tvTimeOnline = binding.tvTimeOnline

        tvCmptdTrip = binding.tvCmptdTrip

        tvCmptdTripTitle = binding.tvCmptdTripTitle

        rlTimeFare = binding.rlTimeFare

        nsvWhole = binding.nsvWhole

        rvDailyStamt = binding.rvDailyStamt

        tvTitle = binding.ftvTitle
        commonMethods.setheaderText(
            resources.getString(R.string.dailyearning),
            binding.commonHeader.headertext
        )
        binding.commonHeader.back.setOnClickListener {
            onBack()
        }
        //commonMethods.imageChangeforLocality(this, insurance_back)
        getIntents()
        setRecyclerView()
        dailyEarnsFlow()

    }

    private fun dailyEarnsFlow() {
        try {
            val allHomeDataCursor: Cursor =
                dbHelper.getDocument(Constants.DB_KEY_PAY_STATEMENTS_DAILY_DETAILS.toString())
            if (allHomeDataCursor.moveToFirst()) {
                isViewUpdatedWithLocalDB = true
                nsvWhole.visibility = View.VISIBLE
                //tvOfflineAnnouncement.setVisibility(View.VISIBLE)
                try {
                    onSuccessDaily(allHomeDataCursor.getString(0), true)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                followProcedureForNoDataPresentInDB()
            }
        } catch (e: Exception) {
            commonMethods.showMessage(this, dialog, e.message.toString())
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            nsvWhole.visibility = View.GONE
            dailyEarngApiCall(true)
        } else {
            CommonMethods.showNoInternetAlert(
                this,
                object : CommonMethods.INoInternetCustomAlertCallback {
                    override fun onOkayClicked() {
                        finish()
                    }

                    override fun onRetryClicked() {
                        followProcedureForNoDataPresentInDB()
                    }

                })
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
        when (jsonResp.requestCode) {
            REQ_DAILY_STATEMENT -> if (jsonResp.isSuccess) {
                val getCurrentPage = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "current_page",
                    Int::class.java
                ) as Int
                currentPage = getCurrentPage
                nsvWhole.visibility = View.VISIBLE
                if (currentPage == 1) {
                    dbHelper.insertWithUpdate(
                        Constants.DB_KEY_PAY_STATEMENTS_DAILY_DETAILS.toString(),
                        jsonResp.strResponse
                    )
                    onSuccessDaily(jsonResp.strResponse, false)
                } else {
                    onLoadMoreDailyHoreList(jsonResp)
                }
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
    }

    private fun onSuccessDaily(jsonResponse: String, isFromDatabase: Boolean) {
        dailyStatement = gson.fromJson(jsonResponse, DailyStatement::class.java)
        dailyStatementList.clear()
        dailyStatementList = dailyStatement.daily_statement as MutableList<DailyStatement.Statement>
        dailyDriverStatement = dailyStatement.driver_statement!!
        dailyDriverStatementContent = dailyStatement.driver_statement!!.content!!

        //header
        tvDay.text = dailyDriverStatement.header!!.key
        tvDailyAmt.text = dailyDriverStatement.header!!.value

        //title
        tvTitle.text = dailyDriverStatement.title

        //footer
        tvCmptdTripTitle.text = dailyDriverStatement.footer!![0].key
        tvCmptdTrip.text = dailyDriverStatement.footer!![0].value

        //DriverStatementContent
        rvDailyStamt.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        rvDailyStamt.layoutManager = layoutManager
        var adapter: RecyclerView.Adapter<*> =
            PriceStatementAdapter(this, dailyDriverStatementContent)
        rvDailyStamt.adapter = adapter

        if (dailyStatementList.isNotEmpty()) {
            TOTAL_PAGES = dailyStatement.totalPage
            dailyHoursPaginationAdapter?.clearAll()
            dailyHoursPaginationAdapter?.addAll(dailyStatementList as ArrayList<DailyStatement.Statement>)
            dailyHoursPaginationAdapter!!.notifyDataSetChanged()
            if (isFromDatabase) {
                isLastPage = true
                if (isViewUpdatedWithLocalDB) {
                    isViewUpdatedWithLocalDB = false
                    currentPage = 1
                    dailyEarngApiCall(false)
                }
            } else {
                if (currentPage <= TOTAL_PAGES && TOTAL_PAGES > 1) {
                    if (commonMethods.isOnline(this)) {
                        isLastPage = false
                        dailyHoursPaginationAdapter!!.addLoadingFooter()
                    }
                } else
                    isLastPage = true
            }
        }
    }

    private fun onLoadMoreDailyHoreList(jsonResp: JsonResponse) {
        dailyStatement = gson.fromJson(jsonResp.strResponse, DailyStatement::class.java)
        dailyHoursPaginationAdapter!!.removeLoadingFooter()
        isLoading = false

        dailyHoursPaginationAdapter?.addAll(dailyStatement.daily_statement as ArrayList<DailyStatement.Statement>)
        dailyHoursPaginationAdapter!!.notifyDataSetChanged()
        if (currentPage != TOTAL_PAGES)
            dailyHoursPaginationAdapter!!.addLoadingFooter()
        else
            isLastPage = true
    }

    private fun setRecyclerView() {
        listView.setHasFixedSize(false)
        listView.isNestedScrollingEnabled = false
        val linearLayoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        listView.layoutManager = linearLayoutManager
        dailyHoursPaginationAdapter = DailyHoursPaginationAdapter(this, this, this)
        listView.adapter = dailyHoursPaginationAdapter
        listView.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {
            override fun loadMoreItems() {
                if (commonMethods.isOnline(this@DailyEarningDetails)) {
                    isLoading = true
                    currentPage += 1
                    dailyEarngApiCall(false)
                }
            }

            override fun getTotalPageCount(): Int {
                return TOTAL_PAGES
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

        })
    }

    override fun onItemClick(tripId: String?) {
        val intent = Intent(this@DailyEarningDetails, TripDetails::class.java)
        intent.putExtra("tripId", tripId)
        startActivity(intent)
    }

    override fun retryPageLoad() {
        dailyEarngApiCall(false)
    }
}