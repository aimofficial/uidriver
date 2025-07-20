/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.newtaxiprime.driver.home.fragments

/**
 * @package com.newtaxiprime.driver.home.fragments
 * @subpackage fragments
 * @category RatingActivity
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.ActivityRatingBinding
import com.newtaxiprime.driver.home.datamodel.RatingModel
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.newtaxiprime.driver.trips.rating.Comments
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


/* ************************************************************
                      RatingActivity
Its used get home screen rating fragment details
*************************************************************** */
class RatingActivity : CommonActivity(), ServiceListener {

    lateinit var dialog: AlertDialog

    lateinit @Inject
    var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    lateinit var feedbackhistorylayout: RelativeLayout

    lateinit var rating_lay: RelativeLayout

    lateinit var lifetime: TextView

    lateinit var ratingtrips: TextView

    lateinit var fivestar: TextView

    lateinit var textView2: TextView

    lateinit var tvRatingContent: TextView

    lateinit var arrarowone: TextView

    lateinit var binding: ActivityRatingBinding


    lateinit var tvTitle: TextView
    protected var isInternetAvailable: Boolean = false

    fun onBack() {
        onBackPressed()
    }


    val userRating: HashMap<String, String>
        get() {
            val userRatingHashMap = HashMap<String, String>()
            userRatingHashMap["user_type"] = sessionManager.type!!
            userRatingHashMap["token"] = sessionManager.accessToken!!

            return userRatingHashMap
        }


    fun feedbackHistoryLayout() {
        feedbackhistorylayout.isEnabled = false
        val intent = Intent(this, Comments::class.java)
        startActivity(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        binding = ActivityRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppController.getAppComponent().inject(this)

        feedbackhistorylayout = binding.feedbackhistorylayout

        rating_lay = binding.ratingLay

        lifetime = binding.lifetime

        ratingtrips = binding.ratingtrips

        fivestar = binding.fivestar

        textView2 = binding.textView2

        tvRatingContent = binding.tvRatingContent

        arrarowone = binding.arrarowone

        tvTitle = binding.rltHeader.tvTitle

        dialog = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(this)
        textView2.visibility = View.GONE
        tvRatingContent.visibility = View.GONE
        tvTitle.text = getString(R.string.rating)

        binding.feedbackhistorylayout.setOnClickListener{
            feedbackHistoryLayout()
        }

        binding.rltHeader.ivBack.setOnClickListener {
            onBack()
        }

        if (isInternetAvailable) {
            /*
         *  Get driver rating and feed back details API
         **/
            updateEarningChart()

        } else {
            dialogfunction()
        }
        initView()
    }

    private fun initView() {
    }


    fun updateEarningChart() {

        commonMethods.showProgressDialog(this)
        apiService.updateDriverRating(userRating).enqueue(RequestCallback(this))

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {

            onSuccessRating(jsonResp)

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {

            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)

        }
    }

    override fun onResume() {
        super.onResume()
        feedbackhistorylayout.isEnabled = true
    }

    private fun onSuccessRating(jsonResp: JsonResponse) {

        val ratingModel = gson.fromJson(jsonResp.strResponse, RatingModel::class.java)
        if (ratingModel != null) {
            val total_rating = ratingModel.totalRating
            val total_rating_count = ratingModel.totalRatingCount
            val five_rating_count = ratingModel.fiveRatingCount
            val driver_rating = ratingModel.driverRating

            lifetime.text = total_rating_count
            ratingtrips.text = total_rating
            fivestar.text = five_rating_count

            if (driver_rating!!.equals("0.00", ignoreCase = true) || driver_rating.equals(
                    "0",
                    ignoreCase = true
                )
            ) {
                tvRatingContent.visibility = View.GONE
                textView2.visibility = View.VISIBLE
                textView2.text = resources.getString(R.string.no_ratings_display)
                textView2.textSize = 20f
                textView2.setCompoundDrawablesRelative(null, null, null, null)
            } else {
                textView2.visibility = View.VISIBLE
                textView2.text = driver_rating
                tvRatingContent.visibility = View.VISIBLE
            }


        }


    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

    }


    /*
     *  show dialog for no internet available
     */
    fun dialogfunction() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(resources.getString(R.string.turnoninternet))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.ok)) { _, _ -> builder.setCancelable(true) }

        val alert = builder.create()
        alert.show()
    }

    companion object {


    }

}
