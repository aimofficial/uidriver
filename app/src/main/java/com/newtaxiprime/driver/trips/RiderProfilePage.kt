package com.newtaxiprime.driver.trips

/**
 * @package com.newtaxiprime.driver.home
 * @subpackage home
 * @category RiderProfilePage
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.AppActivityRiderProfileBinding
import com.newtaxiprime.driver.home.datamodel.TripDetailsModel
import com.squareup.picasso.Picasso
import javax.inject.Inject

/* ************************************************************
                      RiderProfilePage
Its used to get RiderProfilePage details
*************************************************************** */
class RiderProfilePage : CommonActivity() {

    lateinit @Inject
    var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    lateinit var profileimage: ImageView

    lateinit var riderAcceptedCartypeImage: ImageView

    lateinit var cancel_lay: RelativeLayout

    lateinit var rating_layout: RelativeLayout

    lateinit var ridername: TextView

    lateinit var ratingtext: TextView

    lateinit var adresstext: TextView

    lateinit var droplocation: TextView

    lateinit var cartype: TextView
    lateinit var cancel_txt: TextView

    //AcceptedDriverDetails tripDetailsModel;
    lateinit var tripDetailsModel: TripDetailsModel
    private var currentRiderPosition: Int = 0

    private lateinit var binding: AppActivityRiderProfileBinding

    fun onBack() {
        onBackPressed()
    }

    fun contact() {
        val requstreceivepage = Intent(applicationContext, RiderContactActivity::class.java)
        requstreceivepage.putExtra(
            "ridername",
            tripDetailsModel.riderDetails.get(currentRiderPosition).name
        )
        requstreceivepage.putExtra(
            "mobile_number",
            tripDetailsModel.riderDetails.get(currentRiderPosition).mobile_number
        )
        requstreceivepage.putExtra(
            CommonKeys.KEY_CALLER_ID,
            tripDetailsModel.riderDetails.get(currentRiderPosition).riderId
        )
        startActivity(requstreceivepage)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityRiderProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        profileimage = binding.profileImage1
        riderAcceptedCartypeImage = binding.imgvRiderAcceptedCartypeImage
        cancel_lay = binding.cancelLay
        rating_layout = binding.ratingLayout
        ridername = binding.nametext
        ratingtext = binding.ratingtext
        adresstext = binding.adresstext
        droplocation = binding.droplocation
        cartype = binding.cartype
        cancel_txt = binding.cancelTxt

        binding.commonHeader.back.setOnClickListener {
            onBack()
        }

        binding.contactLay.setOnClickListener {
            contact()
        }


        /* common header tittle */
        commonMethods.setheaderText(
            resources.getString(R.string.enroute),
            binding.commonHeader.headertext
        )
        val extras = intent.extras
        if (extras != null) {
            tripDetailsModel =
                intent.getSerializableExtra("riderDetails") as TripDetailsModel //Obtaining data
            currentRiderPosition = intent.getIntExtra("currentRiderPosition", 0)
        }

        /*
                    *  Request rider details
                    */
        ridername.text = tripDetailsModel.riderDetails.get(currentRiderPosition).name
        insertRiderInfoToSession()
        if (tripDetailsModel.riderDetails.get(currentRiderPosition).rating == "0.0" || tripDetailsModel.riderDetails.get(
                currentRiderPosition
            ).rating == ""
        ) {
            rating_layout.visibility = View.GONE
        } else {
            ratingtext.text = tripDetailsModel.riderDetails.get(currentRiderPosition).rating
        }
        adresstext.text = tripDetailsModel.riderDetails.get(currentRiderPosition).pickupAddress
        val imageUr = tripDetailsModel.riderDetails.get(currentRiderPosition).profileImage
        droplocation.text = tripDetailsModel.riderDetails.get(currentRiderPosition).destAddress
        cartype.text = tripDetailsModel.riderDetails.get(currentRiderPosition).carType

        Picasso.get().load(imageUr)
            .into(profileimage)

        Picasso.get().load(tripDetailsModel.riderDetails.get(currentRiderPosition).carActiveImage)
            .error(R.drawable.car)
            .into(riderAcceptedCartypeImage)

        if (sessionManager.tripStatus != null) {

            if (sessionManager.tripStatus == CommonKeys.TripDriverStatus.BeginTrip || sessionManager.tripStatus == CommonKeys.TripDriverStatus.EndTrip) {
                cancel_lay.isEnabled = false
                cancel_lay.isClickable = false
                // cancelicon.setTextColor(ContextCompat.getColor(applicationContext,R.color.cancel_disable_grey))
                cancel_txt.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.cancel_disable_grey
                    )
                )
            } else {
                cancel_lay.isEnabled = true
                cancel_lay.isClickable = true
                // cancelicon.setTextColor(ContextCompat.getColor(applicationContext,R.color.app_continue))
                cancel_txt.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.newtaxi_app_black
                    )
                )
            }
        } else {
            cancel_lay.isEnabled = true
            cancel_lay.isClickable = true
            //  cancelicon.setTextColor(ContextCompat.getColor(applicationContext,R.color.app_continue))
            cancel_txt.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.newtaxi_app_black
                )
            )
        }
        /*
         *  Redirect to trip cancel
         */
        cancel_lay.setOnClickListener {
            val requstreceivepage = Intent(applicationContext, CancelYourTripActivity::class.java)
            startActivity(requstreceivepage)
        }

    }

    private fun insertRiderInfoToSession() {
        sessionManager.riderProfilePic =
            tripDetailsModel.riderDetails.get(currentRiderPosition).profileImage
        sessionManager.riderRating = tripDetailsModel.riderDetails.get(currentRiderPosition).rating
        sessionManager.riderName = tripDetailsModel.riderDetails.get(currentRiderPosition).name
        sessionManager.riderId = tripDetailsModel.riderDetails.get(currentRiderPosition).riderId!!
    }
}
