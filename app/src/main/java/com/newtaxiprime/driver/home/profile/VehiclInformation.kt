package com.newtaxiprime.driver.home.profile

/**
 * @package com.newtaxiprime.driver.home.profile
 * @subpackage profile model
 * @category VehiclInformation
 * @author Seen Technologies
 *
 */

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.ActivityVehiclInformationBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import javax.inject.Inject

/* ************************************************************
                VehiclInformation
Its used to view the document information details
*************************************************************** */
class VehiclInformation : CommonActivity() {

    @Inject
    lateinit var commonMethods: CommonMethods

    lateinit var carname: TextView

    lateinit var carnumber: TextView

    lateinit var cartype: TextView

    lateinit var tvCompany: TextView

    lateinit var rlCompanyName: RelativeLayout

    lateinit var pbLoader: ProgressBar

    lateinit var carImage: ImageView
    private var companyName: String? = null
    private var companyId: Int = 0

    private lateinit var binding: ActivityVehiclInformationBinding

    fun onpBack() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehiclInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)


        /*
                *  Driver document information
                */
        carname = binding.carname
        carnumber = binding.carnumber
        cartype = binding.cartype
        tvCompany = binding.tvCompany
        rlCompanyName = binding.rlCompanyName
        pbLoader = binding.pbLoader
        carImage = binding.carimage

        binding.backLay.setOnClickListener {
            onpBack()
        }

        carname.text = intent.getStringExtra("vehiclename")
        carnumber.text = intent.getStringExtra("vehiclenumber")
        cartype.text = intent.getStringExtra("car_type")
        companyName = intent.getStringExtra("companyname")
        companyId = intent.getIntExtra("companyid", 1)
        commonMethods.imageChangeforLocality(this,binding.dochomeBack)
        if (companyName != null && companyName != "" && companyId > 1) {
            tvCompany.text = companyName
            rlCompanyName.visibility = View.VISIBLE
        } else {
            tvCompany.text = ""
            rlCompanyName.visibility = View.GONE
        }


        Picasso.get().load(intent.getStringExtra("car_image")).error(R.drawable.car)
            .into(carImage, object : Callback {
                override fun onSuccess() {
                    pbLoader.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    pbLoader.visibility = View.GONE
                }
            })

    }
}
