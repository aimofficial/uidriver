package com.newtaxiprime.driver.home.paymentstatement

/**
 * @package com.newtaxiprime.driver.home.paymentstatement
 * @subpackage paymentstatement model
 * @category PayStatementDetails
 * @author Seen Technologies
 *
 */

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
import butterknife.OnClick
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
import com.newtaxiprime.driver.common.util.Enums.REQ_WEEKLY_STATEMENT
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.ActivityPaystatementDetailsBinding
import com.newtaxiprime.driver.home.datamodel.InvoiceContent
import com.newtaxiprime.driver.home.datamodel.WeeklyStatementModel
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import org.json.JSONException
import java.util.*
import javax.inject.Inject

/* ************************************************************
                PayStatementDetails
Its used to view the paymentstatement details function
*************************************************************** */
class PayStatementDetails : CommonActivity(), ServiceListener {
    private var isViewUpdatedWithLocalDB: Boolean = false

    @Inject
    lateinit var dbHelper: Sqlite


    @Inject
    internal lateinit var commonMethods: CommonMethods

    @Inject
    internal lateinit var customDialog: CustomDialog

    @Inject
    lateinit var apiService: ApiService

    @Inject
    internal lateinit var sessionManager: SessionManager
    internal lateinit var dialog: AlertDialog

    @Inject
    internal lateinit var gson: Gson

    lateinit var listView: RecyclerView

    lateinit var nsvWhole: NestedScrollView

    internal lateinit var tvWeekDate: FontTextView

    internal lateinit var tvWeekAmt: FontTextView

    internal lateinit var tvBaseFare: FontTextView

    internal lateinit var tvAccessFare: FontTextView

    internal lateinit var tvTimeFare: FontTextView

    internal lateinit var rlTimeFare: RelativeLayout

    internal lateinit var tvNewtaxiEarn: FontTextView

    internal lateinit var tvCashColltd: FontTextView

    internal lateinit var tvTimeOnline: FontTextView

    internal lateinit var tvCmptdTrip: FontTextView

    internal lateinit var tvCmptdTripTitle: FontTextView

    internal lateinit var tvBankDep: FontTextView

    internal lateinit var tvTitle: FontTextView

    internal lateinit var rvWeeklyStamt: RecyclerView

    private lateinit var binding: ActivityPaystatementDetailsBinding

    private var weeklylistmodels: MutableList<WeeklyStatementModel.Statement> = ArrayList()
    internal lateinit var weeklyModel: WeeklyStatementModel
    internal lateinit var weeklyDriverStatement: WeeklyStatementModel.DriverStatement
    internal lateinit var weeklyDriverStatementContent: ArrayList<InvoiceContent>


    var searchlist: MutableList<PayModel> = ArrayList()
    var adapter: DailyEarnListAdapter? = null
    var Paydate = arrayOf(
        "Sunday 03/06",
        "Monday 04/06 ",
        "Tuesday 04/06",
        "Wednesday 05/06",
        "Thursday 06/06",
        "Friday 07/06",
        "Saturday 08/06"
    )
    var Payamount =
        arrayOf("$92.88", "$82.88", "$72.88", "$9002.88", "$902.88", "$92.88", "$892.88")

    var weeklyDate: String? = null

    fun onpBack() {
        onBackPressed()
    }

    private fun weeklyTripApiCall() {
        if (intent.hasExtra("weekly_date")) {
            val intent = intent
            weeklyDate = intent.getStringExtra("weekly_date").toString()

            val allHomeDataCursor: Cursor =
                dbHelper.getDocument(Constants.DB_KEY_PAY_STATEMENTS_WEEKLY_DETAILS + weeklyDate)
            if (allHomeDataCursor.moveToFirst()) {
                isViewUpdatedWithLocalDB = true
                try {
                    onSuccessWeekly(allHomeDataCursor.getString(0))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                followProcedureForNoDataPresentInDB()
            }
        }
    }

    private fun loadWeeklyTripsDetail() {
        commonMethods.showProgressDialog(this)
        apiService.weeklyStatement(
            sessionManager.accessToken!!,
            sessionManager.userType!!,
            weeklyDate!!
        )
            .enqueue(RequestCallback(REQ_WEEKLY_STATEMENT, this@PayStatementDetails))
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            loadWeeklyTripsDetail()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        binding = ActivityPaystatementDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listView = binding.dailyearningList

        nsvWhole = binding.nsvWhole

        var tvWeekDate = binding.tvWeekDate

        tvWeekAmt = binding.tvWeekAmt
        tvBaseFare = binding.tvBaseFare

        tvAccessFare = binding.tvAccessFare

        tvTimeFare = binding.tvTimeFare

        rlTimeFare = binding.rlTimeFare

        tvNewtaxiEarn = binding.tvNewtaxiEarn

        tvCashColltd = binding.tvCashColltd

        tvTimeOnline = binding.tvTimeOnline

        tvCmptdTrip = binding.tvCmptdTrip

        tvCmptdTripTitle = binding.tvCmptdTripTitle

        tvBankDep = binding.tvBankDep

        tvTitle = binding.header

        rvWeeklyStamt = binding.rvWeeklyStamt
        commonMethods.setheaderText(
            resources.getString(R.string.weeklystatement),
            binding.commonHeader.headertext
        )
        binding.commonHeader.back.setOnClickListener {
            onpBack()
        }
        //commonMethods.imageChangeforLocality(this, insurance_back)
        dialog = commonMethods.getAlertDialog(this)
        nsvWhole.visibility = View.GONE
        weeklyTripApiCall()

        /*
         *  list on click listener
         */
        //        listView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
        //            GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
        //
        //                @Override
        //                public boolean onSingleTapUp(MotionEvent e) {
        //                    return true;
        //                }
        //
        //            });
        //
        //            @Override
        //            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        //                View child = rv.findChildViewUnder(e.getX(), e.getY());
        //                if (child != null && gestureDetector.onTouchEvent(e)) {
        //                    Intent paystatement_details = new Intent(getApplicationContext(), DailyEarningDetails.class);
        //                    startActivity(paystatement_details);
        //                    overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);
        //                }
        //
        //                return false;
        //            }
        //
        //            @Override
        //            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        //
        //            }
        //
        //            @Override
        //            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        //
        //            }
        //        });

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
            REQ_WEEKLY_STATEMENT -> if (jsonResp.isSuccess) {
                dbHelper.insertWithUpdate(
                    Constants.DB_KEY_PAY_STATEMENTS_WEEKLY_DETAILS + weeklyDate,
                    jsonResp.strResponse
                )
                onSuccessWeekly(jsonResp.strResponse)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {}


    private fun onSuccessWeekly(jsonResponse: String) {
        nsvWhole.visibility = View.VISIBLE
        getWeeklyTripDetails()
        weeklyModel = gson.fromJson(jsonResponse, WeeklyStatementModel::class.java)
        weeklylistmodels.clear()
        weeklylistmodels = weeklyModel.statement as MutableList<WeeklyStatementModel.Statement>
        weeklyDriverStatement = weeklyModel.driver_statement!!
        weeklyDriverStatementContent = weeklyModel.driver_statement!!.content!!

        //title
        tvTitle.text = weeklyDriverStatement.title

        //header
        tvWeekDate.text = weeklyDriverStatement.header?.date
        tvWeekAmt.text = weeklyDriverStatement.header?.price

        //dailyDriverStatementContent
        rvWeeklyStamt.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        rvWeeklyStamt.layoutManager = layoutManager
        var adapter: RecyclerView.Adapter<*> =
            PriceStatementAdapter(this, weeklyDriverStatementContent)
        rvWeeklyStamt.adapter = adapter


        //footer
        tvCmptdTripTitle.text = weeklyDriverStatement.footer!![0].key
        tvCmptdTrip.text = weeklyDriverStatement.footer!![0].value


        //        tvBaseFare.setText(weeklyModel.getSymbol() + weeklyModel.getTotal_fare());
        //        tvAccessFare.setText("-"+weeklyModel.getSymbol() + weeklyModel.getAccess_fee());
        //        tvBankDep.setText(weeklyModel.getSymbol() + weeklyModel.getBank_deposits());
        //        rlTimeFare.setVisibility(View.GONE);
        //        tvNewtaxiEarn.setText(weeklyModel.getSymbol() + weeklyModel.getDriver_earnings());
        //        tvCashColltd.setText(weeklyModel.getSymbol() + weeklyModel.getCash_collected());


        if (!weeklylistmodels.isEmpty()) {
            listView.setHasFixedSize(true)
            listView.isNestedScrollingEnabled = false
            listView.layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            adapter = DailyEarnListAdapter(this, weeklylistmodels)
            listView.adapter = adapter
        }
        if (isViewUpdatedWithLocalDB) {
            isViewUpdatedWithLocalDB = false
            getWeeklyTripDetails()
        }
    }

    private fun getWeeklyTripDetails() {
        if (commonMethods.isOnline(this)) {
            apiService.weeklyStatement(
                sessionManager.accessToken!!,
                sessionManager.userType!!,
                weeklyDate!!
            ).enqueue(RequestCallback(REQ_WEEKLY_STATEMENT, this@PayStatementDetails))
        } else {
            CommonMethods.showInternetNotAvailableForStoredDataViewer(this)
        }
    }
}
