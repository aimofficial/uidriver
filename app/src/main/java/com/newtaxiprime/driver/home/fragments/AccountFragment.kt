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
 * @category AccountFragment
 * @author Seen Technologies
 *
 */


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.Enums.REQ_CURRENCY
import com.newtaxiprime.driver.common.util.Enums.REQ_DRIVER_PROFILE
import com.newtaxiprime.driver.common.util.Enums.REQ_LANGUAGE
import com.newtaxiprime.driver.common.util.Enums.REQ_LOGOUT
import com.newtaxiprime.driver.common.util.Enums.REQ_UPDATE_CURRENCY
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.databinding.FragmentAccountBinding
import com.newtaxiprime.driver.home.MainActivity
import com.newtaxiprime.driver.home.datamodel.BankDetailsModel
import com.newtaxiprime.driver.home.datamodel.CurrencyDetailsModel
import com.newtaxiprime.driver.home.datamodel.CurreneyListModel
import com.newtaxiprime.driver.home.datamodel.DriverProfileModel
import com.newtaxiprime.driver.home.fragments.Referral.ShowReferralOptionsActivity
import com.newtaxiprime.driver.home.fragments.currency.CurrencyListAdapter
import com.newtaxiprime.driver.home.fragments.currency.CurrencyModel
import com.newtaxiprime.driver.home.fragments.language.LanguageAdapter
import com.newtaxiprime.driver.home.fragments.payment.PayToAdminActivity
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.newtaxiprime.driver.home.managevehicles.SettingActivity.Companion.alertDialogStores1
import com.newtaxiprime.driver.home.managevehicles.SettingActivity.Companion.alertDialogStores2
import com.newtaxiprime.driver.home.managevehicles.SettingActivity.Companion.currencyclick
import com.newtaxiprime.driver.home.managevehicles.SettingActivity.Companion.langclick
import com.newtaxiprime.driver.home.payouts.PayoutDetailsListActivity
import com.newtaxiprime.driver.home.payouts.PayoutEmailListActivity
import com.newtaxiprime.driver.home.profile.DriverProfile
import com.newtaxiprime.driver.home.profile.VehiclInformation
import com.newtaxiprime.driver.home.signinsignup.SigninSignupHomeActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


/* ************************************************************
                      AccountFragment
Its used get home screen account fragment details
*************************************************************** */

class AccountFragment : Fragment(), ServiceListener {

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

    lateinit var imglatout1: RelativeLayout

    lateinit var rltBankLayout: RelativeLayout

    lateinit var imglatout2: RelativeLayout

    lateinit var documentlayout: RelativeLayout

    lateinit var signlayout: RelativeLayout

    lateinit var paymentlayout: RelativeLayout

    lateinit var rltPayTo: RelativeLayout

    lateinit var currencylayout: RelativeLayout

    lateinit var languagelayout: RelativeLayout

    lateinit var profile_image1: ImageView

    lateinit var profilename: TextView

    lateinit var carno: TextView

    lateinit var carname: TextView

    lateinit var language: TextView

    lateinit var currency_code: TextView

    lateinit var carImage: ImageView

    lateinit var tvView: TextView

    lateinit var pbLoader: ProgressBar

    lateinit var arrarowone: TextView

    lateinit var arrarowtwo: TextView

    lateinit var arrarowthree: TextView

    lateinit var arrarowfour: TextView

    lateinit var arrarowfive: TextView

    lateinit var arrarowsix: TextView

    lateinit var arrarowseven: TextView

    internal var bankDetailsModel: BankDetailsModel? = null
    var vehicle_name: String? = null
    var vehicle_number: String? = null
    var car_type: String? = null
    var car_image: String? = null
    lateinit var recyclerView1: RecyclerView
    lateinit var languageView: RecyclerView
    lateinit var currencyList: ArrayList<CurrencyModel>
    var languagelist: MutableList<CurrencyModel> = ArrayList<CurrencyModel>()
    lateinit var currencyListAdapter: CurrencyListAdapter
    lateinit var LanguageAdapter: LanguageAdapter
    lateinit var symbol: Array<String?>
    lateinit var currencycode: Array<String?>
    var currency: String = ""
    var Language: String? = null
    var LanguageCode: String? = null
    lateinit var driverProfileModel: DriverProfileModel
    lateinit var langocde: String
    private var dialog: AlertDialog? = null
    private var companyName: String? = null
    private var company_id: Int = 0

    lateinit var binding: FragmentAccountBinding

    lateinit var referral_layout: RelativeLayout

    fun currency() {
        currencylayout.isClickable = false
        currency_list()
    }

    /**
     * Driver Profile
     */
    fun profile() {
        val intent = Intent(activity, DriverProfile::class.java)
        startActivity(intent)
    }

    /**
     * Bank Details
     */
    fun bankDetails() {
        val intent = Intent(activity, PayoutDetailsListActivity::class.java)
        //intent.putExtra("bankDetailsModel", bankDetailsModel)
        startActivity(intent)
    }

    /**
     * Driver Vehicle Profile
     */
    fun vehicleProfile() {
        val intent = Intent(activity, VehiclInformation::class.java)
        intent.putExtra("vehiclename", vehicle_name)
        intent.putExtra("vehiclenumber", vehicle_number)
        intent.putExtra("car_type", car_type)
        intent.putExtra("companyname", companyName)
        intent.putExtra("companyid", company_id)
        intent.putExtra("car_image", car_image)
        startActivity(intent)
    }


    fun payto() {
        val payto = Intent(activity, PayToAdminActivity::class.java)
        startActivity(payto)
    }

    /**
     * Language List
     */
//    @OnClick(R.id.rltLanguage)
//    fun language() {
//        languagelist()
//        languagelayout.isClickable = false
//    }

    fun referral() {
        val intent = Intent(activity, ShowReferralOptionsActivity::class.java)
        startActivity(intent)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)


    }


    fun payout() {
        val signin = Intent(activity, PayoutEmailListActivity::class.java)
        startActivity(signin)
        requireActivity().overridePendingTransition(
            R.anim.ub__slide_in_right,
            R.anim.ub__slide_out_left
        )
    }


    fun logoutpopup() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        // set the custom dialog components - text, image and button
        val cancel = dialog.findViewById<View>(R.id.signout_cancel) as TextView
        val signout = dialog.findViewById<View>(R.id.signout_signout) as TextView
        // if button is clicked, close the custom dialog
        cancel.setOnClickListener { dialog.dismiss() }

        signout.setOnClickListener {
            logout()
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }

    override fun onStart() {
        super.onStart()


    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAccountBinding.inflate(layoutInflater)

        val view = binding.root

        imglatout1 = binding.imglatout1

        rltBankLayout = binding.rltBankLayout

        imglatout2 = binding.imglatout2

        documentlayout = binding.documentlayout

        signlayout = binding.signlayout

        paymentlayout = binding.signlayout

        rltPayTo = binding.rltPayTo

        currencylayout = binding.currencylayout

        languagelayout = binding.languagelayout

        profile_image1 = binding.profileImage1

        profilename = binding.profilename

        carno = binding.carno

        carname = binding.carname

        language = binding.language

        currency_code = binding.currencyCode

        carImage = binding.carImage

        tvView = binding.tvView

        pbLoader = binding.pbLoader


        arrarowone = binding.arrarowone

        arrarowtwo = binding.arrarowtwo

        arrarowthree = binding.arrarowthree

        arrarowfour = binding.arrarowfour

        arrarowfive = binding.arrarowfive

        arrarowsix = binding.arrarowsix

        arrarowseven = binding.arrarowseven

        referral_layout = binding.referralLayout


        signlayout.setOnClickListener {
            logoutpopup()
        }

        paymentlayout.setOnClickListener {
            payout()
        }

        referral_layout.setOnClickListener {
            referral()
        }

        imglatout1.setOnClickListener {
            profile()
        }

        rltBankLayout.setOnClickListener {
            bankDetails()
        }

        imglatout2.setOnClickListener {
            vehicleProfile()
        }


        rltPayTo.setOnClickListener {
            payto()
        }




        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this, view)
        commonMethods.imageChangeforLocality(context!!, arrarowone)
        commonMethods.imageChangeforLocality(context!!, arrarowtwo)
        commonMethods.imageChangeforLocality(context!!, arrarowthree)
        commonMethods.imageChangeforLocality(context!!, arrarowfour)
        commonMethods.imageChangeforLocality(context!!, arrarowfive)
        commonMethods.imageChangeforLocality(context!!, arrarowsix)
        commonMethods.imageChangeforLocality(context!!, arrarowseven)
        dialog = commonMethods.getAlertDialog(activity!!)
        pbLoader.visibility = View.VISIBLE
        currency = sessionManager.currencyCode!!
        print("currency" + currency)
        currency_code.text = currency

        return view
    }

    // Load currency list deatils in dialog
    @SuppressLint("UseRequireInsteadOfGet")
    fun currency_list() {

        getCurrency()

        recyclerView1 = RecyclerView(activity!!)
        currencyList = ArrayList()

        currencyListAdapter = CurrencyListAdapter(activity!!, currencyList)

        recyclerView1.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView1.adapter = currencyListAdapter

        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.header, null)
        alertDialogStores1 =
            android.app.AlertDialog.Builder(activity).setCustomTitle(view).setView(recyclerView1)
                .setCancelable(true).show()
        currencylayout.isClickable = true

        alertDialogStores1!!.setOnDismissListener {
            // TODO Auto-generated method stub
            if (currencyclick!!) {
                currencyclick = false
                currency = sessionManager.currencyCode!!
                // Toast.makeText(getApplicationContext(),"Dismiss dialog "+currency_codes,Toast.LENGTH_SHORT).show();
                print("currency$currency")
                currency_code.text = currency
                updateCurrency()
            }
        }

    }

    /**
     * Language List
     */
    @SuppressLint("UseRequireInsteadOfGet")
    fun languagelist() {

        languageView = RecyclerView(activity!!)
        loadlang()

        LanguageAdapter = LanguageAdapter(activity!!, languagelist)
        languageView.layoutManager =
            LinearLayoutManager(activity!!.applicationContext, LinearLayoutManager.VERTICAL, false)
        languageView.adapter = LanguageAdapter

        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.header, null)
        val T = view.findViewById<View>(R.id.header) as TextView
        T.text = getString(R.string.selectlanguage)
        alertDialogStores2 =
            android.app.AlertDialog.Builder(activity).setCustomTitle(view).setView(languageView)
                .setCancelable(true).show()
        languagelayout.isClickable = true

        alertDialogStores2!!.setOnDismissListener {
            // TODO Auto-generated method stub
            if (langclick) {
                langclick = false


                langocde = sessionManager.languageCode!!
                val lang = sessionManager.language
                language.text = lang
                updateLanguage()
            }
            languagelayout.isClickable = true
        }
    }

    fun loadlang() {

        val languages: Array<String>
        val langCode: Array<String>
        languages = resources.getStringArray(R.array.language)
        langCode = resources.getStringArray(R.array.languageCode)
        languagelist.clear()
        for (i in languages.indices) {
            val listdata = CurrencyModel()
            listdata.currencyName = languages[i]
            listdata.currencySymbol = langCode[i]
            languagelist.add(listdata)

        }
    }

    fun setLocale(lang: String) {
        val myLocale = Locale(lang)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.setLocale(myLocale)
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
    }

    /**
     * Driver Datas
     */
    private fun getDriverProfile() {
        commonMethods.showProgressDialog(activity as AppCompatActivity)
        apiService.getDriverProfile(sessionManager.accessToken!!)
            .enqueue(RequestCallback(REQ_DRIVER_PROFILE, this))
    }

    /**
     * Driver Logout
     */
    private fun logout() {
        commonMethods.showProgressDialog(activity as AppCompatActivity)
        apiService.logout(sessionManager.type!!, sessionManager.accessToken!!)
            .enqueue(RequestCallback(REQ_LOGOUT, this))
    }

    /**
     * get Currency List
     */
    fun getCurrency() {
        apiService.getCurrency(sessionManager.accessToken!!)
            .enqueue(RequestCallback(REQ_CURRENCY, this))
    }

    /**
     * Update Language
     */
    fun updateLanguage() {
        commonMethods.showProgressDialog(activity as AppCompatActivity)
        apiService.language(sessionManager.languageCode!!, sessionManager.accessToken!!)
            .enqueue(RequestCallback(REQ_LANGUAGE, this))
    }

    /**
     * Update Currency
     */
    private fun updateCurrency() {
        commonMethods.showProgressDialog(activity as AppCompatActivity)
        apiService.updateCurrency(currency, sessionManager.accessToken!!)
            .enqueue(RequestCallback(REQ_UPDATE_CURRENCY, this))
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(activity, dialog, data)
            return
        }

        when (jsonResp.requestCode) {
            REQ_LOGOUT -> if (jsonResp.isSuccess) {
                onSuccessLogOut()
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(activity, dialog, jsonResp.statusMsg)
            }

            REQ_DRIVER_PROFILE -> if (jsonResp.isSuccess) {
                onSuccessDriverProfile(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(activity, dialog, jsonResp.statusMsg)
            }

            REQ_CURRENCY -> if (jsonResp.isSuccess) {
                onSuccessgetCurrency(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(activity, dialog, jsonResp.statusMsg)
            }

            REQ_UPDATE_CURRENCY -> if (jsonResp.isSuccess) {
                getDriverProfile()
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(activity, dialog, jsonResp.statusMsg)
            }

            REQ_LANGUAGE -> if (jsonResp.isSuccess) {
                setLocale(langocde)
                activity!!.recreate()
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(activity, dialog, jsonResp.statusMsg)
            }

            else -> {
            }
        }
    }

    private fun onSuccessgetCurrency(jsonResp: JsonResponse) {
        val currencyDetailsModel =
            gson.fromJson(jsonResp.strResponse, CurrencyDetailsModel::class.java)
        curreneyListModels.clear()
        curreneyListModels.addAll(currencyDetailsModel.curreneyListModels)

        currencycode = arrayOfNulls<String>(curreneyListModels.size)
        symbol = arrayOfNulls<String>(curreneyListModels.size)
        currencyList.clear()
        for (i in curreneyListModels.indices) {

            currencycode[i] = curreneyListModels[i].code
            symbol[i] = curreneyListModels[i].symbol

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                symbol[i] = Html.fromHtml(symbol[i], Html.FROM_HTML_MODE_COMPACT).toString()
            } else {
                symbol[i] = Html.fromHtml(symbol[i]).toString()
            }


            val listdata = CurrencyModel()
            listdata.currencyName = currencycode[i]
            listdata.currencySymbol = symbol[i]

            currencyList.add(listdata)
        }
        currencyListAdapter.notifyDataChanged()

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

        CommonMethods.DebuggableLogI("datacheck", "datacheck")
    }

    /**
     * SuccessFully Log out
     */
    @SuppressLint("UseRequireInsteadOfGet")
    private fun onSuccessLogOut() {

        val lang = sessionManager.language
        val langCode = sessionManager.languageCode
        //   CommonMethods.stopSinchService(context)
        CommonMethods.stopSinchService(context!!)
        sessionManager.clearAll()
        clearApplicationData() // Clear cache data

        sessionManager.language = lang
        sessionManager.languageCode = langCode


        val intent = Intent(activity, SigninSignupHomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }

    fun onSuccessDriverProfile(jsonResponse: JsonResponse) {
        driverProfileModel = gson.fromJson(jsonResponse.strResponse, DriverProfileModel::class.java)
        sessionManager.profileDetail = jsonResponse.strResponse
        loadData(driverProfileModel)
        val carid = ""
        sessionManager.vehicle_id = carid
        CommonMethods.DebuggableLogV("localshared", "Carid==" + sessionManager.vehicle_id)
    }

    /*
     *  Load Driver profile details
     **/
    fun loadData(driverProfileModel: DriverProfileModel) {

        val first_name = driverProfileModel.firstName
        val last_name = driverProfileModel.lastName
        val user_thumb_image = driverProfileModel.profileImage
        sessionManager.firstName = first_name
        sessionManager.phoneNumber = driverProfileModel.mobileNumber
        company_id = driverProfileModel.companyId
        companyName = driverProfileModel.companyName
        //bankDetailsModel = driverProfileModel.bank_detail


        vehicle_name = driverProfileModel.vehicleName
        vehicle_number = driverProfileModel.vehicleNumber
        car_type = driverProfileModel.carType
        car_image = driverProfileModel.carActiveImage
        sessionManager.oweAmount = driverProfileModel.oweAmount
        sessionManager.driverReferral = driverProfileModel.driverReferralEarning


        profilename.text = "$first_name $last_name"
        if (isAdded) {
            Picasso.get().load(user_thumb_image).into(profile_image1)
        }

        if (company_id > 1) {
            rltPayTo.visibility = View.GONE
            referral_layout.visibility = View.GONE
        } else {
            rltPayTo.visibility = View.VISIBLE
            referral_layout.visibility = View.VISIBLE
        }

        if (!TextUtils.isEmpty(vehicle_name) && !TextUtils.isEmpty(vehicle_name)) {
            carno.text = vehicle_number
            carname.text = vehicle_name
        } else {
            pbLoader.visibility = View.GONE
            imglatout2.isEnabled = false
            tvView.visibility = View.GONE
            carImage.visibility = View.VISIBLE
            carno.visibility = View.GONE
            carname.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
            carname.text = resources.getString(R.string.no_vehicle_assigned)
        }
        try {
            Picasso.get().load(car_image).error(R.drawable.car).into(carImage, object : Callback {
                override fun onSuccess() {
                    pbLoader.visibility = View.GONE
                }

                override fun onError(e: java.lang.Exception?) {
                    pbLoader.visibility = View.GONE
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /*
     *  Clear app cache data
     **/
    @SuppressLint("UseRequireInsteadOfGet")
    fun clearApplicationData() {
        val cache = activity!!.cacheDir
        val appDir = File(cache.parent)
        if (appDir.exists()) {
            val children = appDir.list()
            for (s in children) {
                if ("lib" != s) {
                    deleteDir(File(appDir, s))
                    CommonMethods.DebuggableLogI(
                        "TAG",
                        "**************** File /data/data/APP_PACKAGE/$s DELETED *******************"
                    )

                    // clearApplicationData();
                }
            }
        }

    }

    /*
     *  Get Driver profile while open the page
     **/
    override fun onResume() {
        super.onResume()
        Language = sessionManager.language
        if (Language != null) {
            language.text = Language
        } else {
            language.text = "English"
        }

        getDriverProfile()

    }


    override fun onPause() {
        super.onPause()

    }

    override fun onStop() {
        super.onStop()

    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onDetach() {
        super.onDetach()

    }

    override fun onDestroy() {
        super.onDestroy()

    }

    companion object {


        var curreneyListModels = ArrayList<CurreneyListModel>()


        /*
     *  Delete app local cache data
     **/
        fun deleteDir(dir: File?): Boolean {
            if (dir != null && dir.isDirectory) {
                val children = dir.list()
                if (children != null) {
                    for (i in children.indices) {
                        val success = deleteDir(File(dir, children[i]))
                        if (!success) {
                            return false
                        }
                    }
                }
            }

            return dir!!.delete()
        }
    }
}
