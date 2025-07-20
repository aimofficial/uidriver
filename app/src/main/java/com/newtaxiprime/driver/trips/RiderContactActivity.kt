package com.newtaxiprime.driver.trips

/**
 * @package com.newtaxiprime.driver.home
 * @subpackage home
 * @category RiderContactActivity
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.ButterKnife
import butterknife.OnClick
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.ActivityRiderContactBinding
import com.newtaxiprime.driver.home.firebaseChat.ActivityChat
import javax.inject.Inject

/* ************************************************************
                      RiderContactActivity
Its used to get RiderContactActivity details
*************************************************************** */
class RiderContactActivity : CommonActivity() {

    lateinit @Inject
    var sessionManager: SessionManager

    lateinit @Inject
    var commonMethods: CommonMethods

    lateinit var mobilenumbertext: TextView

    lateinit var ridername: TextView

    lateinit var llMessage: LinearLayout

    private lateinit var binding: ActivityRiderContactBinding

    fun onBack() {
        onBackPressed()
    }

    fun call() {
        if (sessionManager.bookingType == CommonKeys.RideBookedType.manualBooking) {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:" + mobilenumbertext.getText().toString())
            );
            startActivity(intent);
        } else {
            /* val callScreenIntent = Intent(this, CallProcessingActivity::class.java)
             callScreenIntent.putExtra(CommonKeys.KEY_TYPE, CallProcessingActivity.CallActivityType.CallProcessing)
             callScreenIntent.putExtra(KEY_CALLER_ID, intent.getStringExtra(KEY_CALLER_ID))
             startActivity(callScreenIntent) */
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:" + mobilenumbertext.getText().toString())
            );
            startActivity(intent);
        }
    }

    fun startChatActivity() {
        sessionManager.chatJson = ""

        startActivity(Intent(this, ActivityChat::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderContactBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(
            resources.getString(R.string.contact_C),
            binding.commonHeader.headertext
        )
        binding.commonHeader.back.setOnClickListener {
            onBack()
        }
        binding.callbutton.setOnClickListener {
            call()
        }
        mobilenumbertext = binding.mobilenumbertext
        ridername = binding.ridername
        llMessage = binding.llMessage
        llMessage.setOnClickListener {
            startChatActivity()
        }

        ridername.text = intent.getStringExtra("ridername")
        mobilenumbertext.setText(getIntent().getStringExtra("mobile_number"))
        println("mobilenumbertext ${mobilenumbertext.text.toString()}")
        if (sessionManager.bookingType == CommonKeys.RideBookedType.manualBooking) {
            llMessage.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    companion object {

        val CALL = 0x2
    }
}
