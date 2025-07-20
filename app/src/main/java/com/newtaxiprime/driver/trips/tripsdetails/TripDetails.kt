package com.newtaxiprime.driver.trips.tripsdetails

/**
 * @package com.newtaxiprime.driver.trips.tripsdetails
 * @subpackage tripsdetails model
 * @category TripsDetails
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.database.Sqlite
import com.newtaxiprime.driver.common.helper.Constants
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.CommonMethods.Companion.showInternetNotAvailableForStoredDataViewer
import com.newtaxiprime.driver.common.util.CommonMethods.Companion.showNoInternetAlert
import com.newtaxiprime.driver.common.util.Enums.REQ_TRIP_DETAILS
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.AppActivityTripDetailsBinding
import com.newtaxiprime.driver.home.datamodel.InvoiceModel
import com.newtaxiprime.driver.home.datamodel.TripDetailsModel
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.newtaxiprime.driver.trips.rating.PriceRecycleAdapter
import com.newtaxiprime.driver.trips.rating.Riderrating
import com.squareup.picasso.Picasso
import org.json.JSONException
import java.lang.Double
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/* ************************************************************
                TripsDetails
Its used to show all the trips details information to view the page
*************************************************************** */
class TripDetails : CommonActivity(), ServiceListener {

    private var isViewUpdatedWithLocalDB: Boolean = false
    lateinit var tripId: String

    @Inject
    lateinit var sessionManager: SessionManager
    var dialog: AlertDialog? = null

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var dbHelper: Sqlite


    lateinit var trip_amount: TextView

    lateinit var trip_km: TextView

    lateinit var trip_duration: TextView

    lateinit var drop_address: TextView

    lateinit var pickup_address: TextView

    lateinit var seatcount: TextView

    lateinit var trip_date: TextView

    lateinit var route_image: ImageView
    var payment_method: String? = null
    var currencysymbol: String? = null

    lateinit var recyclerView: RecyclerView
    internal var invoiceModels = ArrayList<InvoiceModel>()

    lateinit var staticmapview: RelativeLayout

    lateinit var btnrate: Button

    lateinit var carname: TextView

    lateinit var tripstatus: TextView

    lateinit var tvTripid: TextView

    lateinit var binding: AppActivityTripDetailsBinding

    /*@BindView(R.id.rlt_mapview)
    lateinit var rltImageView: RelativeLayout*/

    /* @BindView(R.id.basrfarelayout)
     lateinit var farelayout: RelativeLayout*/
    var tripDetailsModels: TripDetailsModel? = null

    fun backPressed() {
        onBackPressed()
    }

    fun rate() {
        sessionManager.tripId = tripDetailsModels?.riderDetails?.get(0)?.tripId!!.toString()
        val rating = Intent(this, Riderrating::class.java)
        rating.putExtra("imgprofile", tripDetailsModels?.riderDetails?.get(0)?.profileImage)
        rating.putExtra("back", 1)
        startActivity(rating)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityTripDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this)
        trip_amount = binding.tripAmount
        trip_km = binding.tripKm
        trip_duration = binding.tripDuration
        drop_address = binding.tvDropAddress
        pickup_address = binding.tvPickAddress
        seatcount = binding.seatcount
        trip_date = binding.tripDate
        route_image = binding.routeImage
        recyclerView = binding.rvPrice
        staticmapview = binding.rltMapview
        tvTripid = binding.tvTripid
        btnrate = binding.btnrate
        carname = binding.carname
        tripstatus = binding.tvTripstatus
        btnrate.setOnClickListener {
            rate()
        }

        binding.commonHeader.back.setOnClickListener {
            backPressed()
        }


        /**Commmon Header Text View */
        commonMethods.setheaderText(
            resources.getString(R.string.tripsdetails),
            binding.commonHeader.headertext
        )
        /*  drop_address.visibility = View.GONE
          pickup_address.visibility = View.GONE*/
        currencysymbol = sessionManager.currencySymbol
        val intent = intent
        tripId = intent.getStringExtra("tripId").toString()
        //  farelayout.visibility = View.GONE

        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.setLayoutManager(layoutManager)

        loadTripDetails()
    }

    private fun loadTripDetails() {

        val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_TRIP_DETAILS + tripId)
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            //tvOfflineAnnouncement.setVisibility(View.VISIBLE)
            try {
                onSuccessTripDetail(allHomeDataCursor.getString(0))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            commonMethods.showProgressDialog(this)
            getTripDetails()
        } else {
            showNoInternetAlert(this, object : CommonMethods.INoInternetCustomAlertCallback {
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

    private fun getTripDetails() {
        if (commonMethods.isOnline(this)) {
            apiService.getTripDetails(sessionManager.accessToken!!, tripId)
                .enqueue(RequestCallback(REQ_TRIP_DETAILS, this))
        } else {
            showInternetNotAvailableForStoredDataViewer(this)
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {

            REQ_TRIP_DETAILS -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                dbHelper.insertWithUpdate(
                    Constants.DB_KEY_TRIP_DETAILS.toString() + tripId,
                    jsonResp.strResponse
                )
                onSuccessTripDetail(jsonResp.strResponse)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        commonMethods.showMessage(this, dialog, data)
    }

    private fun onSuccessTripDetail(jsonResponse: String) {

        tripDetailsModels = gson.fromJson(jsonResponse, TripDetailsModel::class.java)
        invoiceModels.clear()
        with(tripDetailsModels) { this?.riderDetails?.get(0)?.invoice }?.let {
            invoiceModels.addAll(
                it
            )
        }
        recyclerView.removeAllViewsInLayout()
        val adapter = PriceRecycleAdapter(this, invoiceModels)
        recyclerView.setAdapter(adapter)

        if (tripDetailsModels?.isPool!! && tripDetailsModels?.seats != 0) {
            seatcount.visibility = View.VISIBLE
            seatcount.setText(resources.getString(R.string.seat_count) + " " + tripDetailsModels?.seats)
        } else {
            seatcount.visibility = View.GONE
        }

        carname.setText(tripDetailsModels?.vehicleName)
        tripstatus.text = with(tripDetailsModels) { this?.riderDetails?.get(0)?.status }



        if (tripDetailsModels?.riderDetails?.size == 0)
            return


        trip_km.text =
            with(tripDetailsModels) { this?.riderDetails?.get(0)?.totalKm } + " " + resources.getString(
                R.string.km_value
            )
        trip_duration.text =
            with(tripDetailsModels) { this?.riderDetails?.get(0)?.totalTime } + " " + resources.getString(
                R.string.mins_value
            )
        pickup_address.text = with(tripDetailsModels) { this?.riderDetails?.get(0)?.pickupAddress }
        drop_address.text = with(tripDetailsModels) { this?.riderDetails?.get(0)?.destAddress }
        tvTripid.text = resources.getString(R.string.trip_id) + with(tripDetailsModels) {
            this?.riderDetails?.get(0)?.tripId
        }

        if (sessionManager.userType != null && !TextUtils.isEmpty(sessionManager.userType) && !sessionManager.userType.equals(
                "0",
                ignoreCase = true
            ) && !sessionManager.userType.equals("1", ignoreCase = true)
        ) {
            // Company
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (java.lang.Float.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }!!) > 0) {
                    trip_amount.text = Html.fromHtml(
                        with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverEarnings }.toString(),
                        Html.FROM_HTML_MODE_LEGACY
                    )
                } else {
                    trip_amount.text =
                        Html.fromHtml(sessionManager.currencySymbol!! + with(tripDetailsModels) {
                            this?.riderDetails?.get(0)?.totalFare
                        }.toString(), Html.FROM_HTML_MODE_LEGACY)
                }

            } else {
                if (java.lang.Float.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }!!) > 0) {
                    trip_amount.text =
                        Html.fromHtml(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }.toString())
                } else {
                    trip_amount.text =
                        Html.fromHtml(sessionManager.currencySymbol!! + with(tripDetailsModels) {
                            this?.riderDetails?.get(0)?.totalFare
                        }.toString())
                }
            }
        } else {

            // Normal Driver
            if (java.lang.Float.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }!!) > 0) {
                trip_amount.text =
                    with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverEarnings }
            } else {
                trip_amount.text = sessionManager.currencySymbol!! + with(tripDetailsModels) {
                    this?.riderDetails?.get(0)?.totalFare
                }
            }
        }

        if (with(tripDetailsModels) { this?.riderDetails?.get(0)?.status }.equals(
                CommonKeys.TripStatus.Rating,
                ignoreCase = true
            )
        ) {
            btnrate.setVisibility(View.VISIBLE)
        } else {
            btnrate.setVisibility(View.GONE)
        }


        var startdate = ""
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("EEEE, dd-MM-yyyy")
        try {
            val date =
                originalFormat.parse(with(tripDetailsModels) { this?.riderDetails?.get(0)?.createdAt })
            startdate = targetFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }


        trip_date.text = startdate

        if (tripDetailsModels?.riderDetails?.get(0)?.mapImage != null && !tripDetailsModels?.riderDetails?.get(
                0
            )?.mapImage.equals("")
        ) {
            Picasso.get().load(tripDetailsModels?.riderDetails?.get(0)?.mapImage)
                .into(route_image)
        }


        if (TextUtils.isEmpty(with(tripDetailsModels) { this?.riderDetails?.get(0)?.mapImage })) {
            val pikcuplatlng =
                LatLng(
                    java.lang.Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.pickup_lat }!!),
                    java.lang.Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.pickup_lng }!!)
                )
            val droplatlng =
                LatLng(
                    Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.drop_lat }!!),
                    Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.drop_lng }!!)
                )

            val pathString = "&path=color:0x000000ff%7Cweight:4%7Cenc:" + with(tripDetailsModels) {
                this?.riderDetails?.get(0)?.tripPath
            }
            val pickupstr = pikcuplatlng.latitude.toString() + "," + pikcuplatlng.longitude
            val dropstr = droplatlng.latitude.toString() + "," + droplatlng.longitude
            val positionOnMap =
                "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "pickup.png|" + pickupstr
            val positionOnMap1 =
                "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "drop.png|" + dropstr

            /* pickup_address.visibility = View.GONE
             drop_address.visibility = View.GONE*/
            staticmapview.visibility = View.GONE
            if (resources.getString(R.string.layout_direction).equals("1")) {
                staticmapview.rotationY = 180f
            }
            binding.tvPickAddress.text =
                tripDetailsModels?.riderDetails?.get(0)?.pickupAddress//pickupLocation
            binding.tvDropAddress.text = tripDetailsModels?.riderDetails?.get(0)?.destAddress


        } else {
            /*  pickup_address.visibility = View.VISIBLE
              drop_address.visibility = View.VISIBLE*/
            staticmapview.visibility = View.VISIBLE
            Picasso.get().load(with(tripDetailsModels) { this?.riderDetails?.get(0)?.mapImage })
                .into(route_image)
        }

        if (isViewUpdatedWithLocalDB) {
            isViewUpdatedWithLocalDB = false
            getTripDetails()
        }
    }
}
