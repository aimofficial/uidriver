package com.newtaxiprime.driver.trips.tripsdetails

/**
 * @package com.newtaxiprime.driver.trips.tripsdetails
 * @subpackage tripsdetails model
 * @category YourTrips
 * @author Seen Technologies
 *
 */

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.AppActivityYourTipsBinding
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.YourTripsListener
import javax.inject.Inject

/* ************************************************************
                YourTrips page
Its used to your current all the trips to show the fuction
*************************************************************** */
class YourTrips : CommonActivity(), TabLayout.OnTabSelectedListener, YourTripsListener {


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

    //This is our tablayout
    lateinit var tabLayout: TabLayout

    //This is our viewPager
    lateinit var viewPager: ViewPager

    lateinit var binding: AppActivityYourTipsBinding

    protected var isInternetAvailable: Boolean = false

    override val res: Resources
        get() = this@YourTrips.resources

    override val instance: YourTrips
        get() = this@YourTrips

    fun backPressed() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityYourTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        tabLayout = binding.tabLayout
        viewPager = binding.pager

        binding.commonHeader.back.setOnClickListener {
            backPressed()
        }

        sessionManager.isTrip = false
        commonMethods.setheaderText(
            resources.getString(R.string.triphistory),
            binding.commonHeader.headertext
        )
        dialog = commonMethods!!.getAlertDialog(this)
        isInternetAvailable = commonMethods!!.isOnline(this)
        setupViewPager(viewPager!!)
        tabLayout!!.setupWithViewPager(viewPager)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(PendingTripsFragment(), getString(R.string.pending_trips))
        adapter.addFragment(CompletedTripsFragments(), getString(R.string.completed_trips))
        viewPager.adapter = adapter
        val layoutDirection = getString(R.string.layout_direction)
        if ("1" == layoutDirection)
            viewPager.rotationY = 180f
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        viewPager!!.currentItem = tab.position
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}
    override fun onTabReselected(tab: TabLayout.Tab) {}

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }
}