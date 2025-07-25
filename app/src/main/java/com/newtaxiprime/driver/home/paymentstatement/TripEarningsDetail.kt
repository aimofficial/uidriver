package com.newtaxiprime.driver.home.paymentstatement

/**
 * @package com.newtaxiprime.driver.trips.tripsdetails
 * @subpackage tripsdetails model
 * @category TripsDetails
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/* ************************************************************
                TripsDetails
Its used to show all the trips details information to view the page
*************************************************************** */
class TripEarningsDetail : CommonActivity(), ServiceListener {

    lateinit var tripId: String

    @Inject
    lateinit var sessionManager: SessionManager
    lateinit var dialog: AlertDialog

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    /* @BindView(R.id.adminamountlayout)
     lateinit var adminamountlayout: RelativeLayout*/
    /*@BindView(R.id.oweamountlayout)
    lateinit var oweamountlayout: RelativeLayout
    @BindView(R.id.driverpayoutlayout)
    lateinit var driverpayoutlayout: RelativeLayout
    @BindView(R.id.cashcollectamountlayout)
    lateinit var cashcollectamountlayout: RelativeLayout
    @BindView(R.id.basefare_amount)
    lateinit var basefare_amount: TextView
    @BindView(R.id.distance_fare)
    lateinit var distance_fare: TextView
    @BindView(R.id.time_fare)
    lateinit var time_fare: TextView
    @BindView(R.id.fee)
    lateinit var fee: TextView
    @BindView(R.id.totalamount)
    lateinit var totalamount: TextView
    @BindView(R.id.total_payouts)
    lateinit var total_payouts: TextView
    @BindView(R.id.cashcollectamount)
    lateinit var cashcollectamount: TextView
    @BindView(R.id.cashcollectamount_txt)
    lateinit var cashcollectamount_txt: TextView
    @BindView(R.id.oweamount)
    lateinit var oweamount: TextView
    @BindView(R.id.driverpayout)
    lateinit var driverpayout: TextView
    @BindView(R.id.adminamount)
    lateinit var adminamount: TextView*/

    lateinit var trip_amount: TextView

    lateinit var trip_km: TextView

    lateinit var trip_duration: TextView

    lateinit var drop_address: TextView

    lateinit var pickup_address: TextView

    lateinit var seatcount: TextView

    lateinit var trip_date: TextView

    lateinit var route_image: ImageView


    lateinit var recyclerView: RecyclerView

    lateinit var btnrate: Button

    lateinit var staticmapview: RelativeLayout

    lateinit var carname: TextView

    lateinit var tripstatus: TextView

    lateinit var tvTripid: TextView
    internal var invoiceModels = ArrayList<InvoiceModel>()
    lateinit var currencysymbol: String


    lateinit var payment_method: String

    lateinit var binding: AppActivityTripDetailsBinding

    internal lateinit var tripDetailsModels: TripDetailsModel

    fun backPressed() {
        onBackPressed()
    }

    fun rate() {
        sessionManager.tripId = tripDetailsModels.riderDetails.get(0).tripId!!.toString()
        val rating = Intent(this, Riderrating::class.java)
        rating.putExtra("imgprofile", tripDetailsModels.riderDetails.get(0).profileImage)
        rating.putExtra("back", 1)
        startActivity(rating)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityTripDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this)
        /*drop_address.visibility=View.GONE
        pickup_address.visibility=View.GONE*/

        trip_amount = binding.tripAmount

        trip_km = binding.tripKm

        trip_duration = binding.tripDuration

        drop_address = binding.tvDropAddress

        pickup_address = binding.tvPickAddress

        seatcount = binding.seatcount

        trip_date = binding.tripDate

        route_image = binding.routeImage


        recyclerView = binding.rvPrice

        btnrate = binding.btnrate

        btnrate.setOnClickListener {
            rate()
        }

        binding.commonHeader.back.setOnClickListener {
            backPressed()
        }

        staticmapview = binding.rltMapview

        carname = binding.carname

        tripstatus = binding.tvTripstatus

        tvTripid = binding.tvTripid
        commonMethods.setheaderText(
            resources.getString(R.string.tripsdetails),
            binding.commonHeader.headertext
        )
        //commonMethods.imageChangeforLocality(this,insurance_back)
        currencysymbol = sessionManager.currencySymbol!!
        val intent = intent
        tripId = intent.getStringExtra("tripId").toString()

        getTripDetails()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    private fun getTripDetails() {
        commonMethods.showProgressDialog(this)
        apiService.getTripDetails(sessionManager.accessToken!!, tripId)
            .enqueue(RequestCallback(REQ_TRIP_DETAILS, this))
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
                onSuccessTripDetail(jsonResp)
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

    private fun onSuccessTripDetail(jsonResponse: JsonResponse) {
        tripDetailsModels = gson.fromJson(jsonResponse.strResponse, TripDetailsModel::class.java)

        tripDetailsModels.riderDetails.get(0).invoice?.let { invoiceModels.addAll(it) }
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        val adapter = PriceRecycleAdapter(this, invoiceModels)
        recyclerView.adapter = adapter
        trip_km.text =
            tripDetailsModels.riderDetails.get(0).totalKm + " " + resources.getString(R.string.km_value)
        trip_duration.text =
            tripDetailsModels.riderDetails.get(0).totalTime + " " + resources.getString(R.string.mins_value)
        pickup_address.text = tripDetailsModels.riderDetails.get(0).pickupAddress
        drop_address.text = tripDetailsModels.riderDetails.get(0).destAddress
        tripstatus.text = tripDetailsModels.riderDetails.get(0).status
        tvTripid.text = tripDetailsModels.riderDetails.get(0).tripId
        //trip_amount.setText(sessionManager.getCurrencySymbol() + tripDetailsModels.getDriverPayout());

        if (tripDetailsModels?.isPool!! && tripDetailsModels?.seats != 0) {
            seatcount.visibility = View.VISIBLE
            seatcount.setText(resources.getString(R.string.seat_count) + " " + tripDetailsModels?.seats)
        } else {
            seatcount.visibility = View.GONE
        }

        carname.setText(tripDetailsModels?.vehicleName)
        trip_amount.text = tripDetailsModels.riderDetails.get(0).driverEarnings

        if (tripDetailsModels.riderDetails.get(0).status.equals("Rating", ignoreCase = true)) {
            btnrate.visibility = View.VISIBLE
        } else {
            btnrate.visibility = View.GONE
        }


        var startdate = ""
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("EEEE, dd-MM-yyyy")
        try {
            val date = originalFormat.parse(tripDetailsModels.riderDetails.get(0).createdAt)
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


        if (TextUtils.isEmpty(tripDetailsModels.riderDetails.get(0).mapImage)) {
            val pikcuplatlng = LatLng(
                java.lang.Double.valueOf(tripDetailsModels.riderDetails.get(0).pickup_lat!!),
                java.lang.Double.valueOf(tripDetailsModels.riderDetails.get(0).pickup_lng!!)
            )
            val droplatlng = LatLng(
                java.lang.Double.valueOf(tripDetailsModels.riderDetails.get(0).drop_lat!!),
                java.lang.Double.valueOf(tripDetailsModels.riderDetails.get(0).drop_lng!!)
            )

            val pickupstr = pikcuplatlng.latitude.toString() + "," + pikcuplatlng.longitude
            val dropstr = droplatlng.latitude.toString() + "," + droplatlng.longitude
            val positionOnMap =
                "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "pickup.png|" + pickupstr
            val positionOnMap1 =
                "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "drop.png|" + dropstr

            /*  pickup_address.visibility=View.GONE
              drop_address.visibility=View.GONE*/

            staticmapview.visibility = View.GONE
            if (resources.getString(R.string.layout_direction).equals("1")) {
                staticmapview.rotationY = 180f
            }

            binding.tvPickAddress.text = tripDetailsModels.riderDetails.get(0)?.pickupAddress
            binding.tvDropAddress.text = tripDetailsModels.riderDetails.get(0)?.destAddress
            /* var staticMapURL = ""
             if (tripDetailsModels.tripPath == null || tripDetailsModels.tripPath == "") {
                 staticMapURL = "https://maps.googleapis.com/maps/api/staticmap?size=640x250&" +
                         pikcuplatlng.latitude + "," + pikcuplatlng.longitude +
                         "" + positionOnMap + "" + positionOnMap1 + //"&zoom=14" +

                         "&key=" + sessionManager.googleMapKey + "&language=" + Locale.getDefault()
             } else {
                 staticMapURL = "https://maps.googleapis.com/maps/api/staticmap?size=640x250&" +
                         pikcuplatlng.latitude + "," + pikcuplatlng.longitude +
                         pathString + "" + positionOnMap + "" + positionOnMap1 + //"&zoom=14" +

                         "&key=" + sessionManager.googleMapKey + "&language=" + Locale.getDefault()
             }
             println("Static Map Url : "+staticMapURL)
             Picasso.with(applicationContext).load(staticMapURL)
                     .into(route_image)*/
        } else {
            /* pickup_address.visibility=View.VISIBLE
             drop_address.visibility=View.VISIBLE*/
            staticmapview.visibility = View.VISIBLE

            Picasso.get().load(with(tripDetailsModels) { this?.riderDetails?.get(0)?.mapImage })
                .into(route_image)
        }
    }
}
