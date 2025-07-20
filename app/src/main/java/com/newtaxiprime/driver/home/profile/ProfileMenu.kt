package com.newtaxiprime.driver.home.profile

/**
 * @package com.newtaxiprime.driver.home.profile
 * @subpackage profile model
 * @category DriverProfile
 * @author Seen Technologies
 *
 */


import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.dependencies.module.ImageCompressAsyncTask
import com.newtaxiprime.driver.common.helper.Constants
import com.newtaxiprime.driver.common.helper.Constants.PICK_IMAGE_REQUEST_CODE
import com.newtaxiprime.driver.common.helper.Constants.SELECT_FILE
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.helper.RunTimePermission
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.*
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.newtaxiprime.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.Enums
import com.newtaxiprime.driver.common.util.Enums.REQ_DRIVER_PROFILE
import com.newtaxiprime.driver.common.util.Enums.REQ_UPDATE_PROFILE
import com.newtaxiprime.driver.common.util.Enums.REQ_UPLOAD_PROFILE_IMG
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.common.views.SupportActivityCommon
import com.newtaxiprime.driver.databinding.AppActivityDriverProfileBinding
import com.newtaxiprime.driver.databinding.AppActivityProfileMenuBinding
import com.newtaxiprime.driver.home.datamodel.DriverProfileModel
import com.newtaxiprime.driver.home.fragments.EarningActivity
import com.newtaxiprime.driver.home.fragments.RatingActivity
import com.newtaxiprime.driver.home.fragments.Referral.ShowReferralOptionsActivity
import com.newtaxiprime.driver.home.fragments.payment.PayToAdminActivity
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ImageListener
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.newtaxiprime.driver.home.termsPolicy.Privacy
import com.newtaxiprime.driver.home.termsPolicy.Terms
import com.squareup.picasso.Picasso
//import kotlinx.android.synthetic.main.app_activity_driver_profile.*
//import kotlinx.android.synthetic.main.app_activity_profile_menu.*
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject


/* ************************************************************
                DriverProfile
Its used to get driver profile details to show view on screen
*************************************************************** */

class ProfileMenu : CommonActivity(), ServiceListener, ImageListener,
    RuntimePermissionDialogFragment.RuntimePermissionRequestedCallback {
    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var runTimePermission: RunTimePermission

    @Inject
    lateinit var sessionManager: SessionManager


    lateinit var arrow: ImageView

    lateinit var tvPhone: TextView

    lateinit var tvEmail: TextView

    //   @BindView(R.id.profile_image1)

    lateinit var profile_image1: ImageView


    lateinit var rlt_editprofile: RelativeLayout

    lateinit var rlt_rating: RelativeLayout

    lateinit var rlt_earning: RelativeLayout

    lateinit var rlt_payadmin: RelativeLayout

    lateinit var binding: AppActivityProfileMenuBinding


    lateinit var first_name: String
    lateinit var last_name: String
    lateinit var mobile_numbers: String
    lateinit var country_code: String
    lateinit var email_id: String
    lateinit var user_thumb_image: String
    lateinit var address_line1: String
    lateinit var address_line2: String
    lateinit var city: String
    lateinit var state: String
    lateinit var postal_code: String
    lateinit var gender: String
    var bm: Bitmap? = null
    var docType = 6          //By Default Set 6 for Profile Image
    private var isUpdate = false
    private lateinit var dialog: AlertDialog
    private var imagePath: String = ""
    lateinit private var imageFile: File
    private var imageUri: Uri? = null

    /**
     * Getting Driver Deatils to Update
     *
     * @return hashmap Datas
     */
    private val details: LinkedHashMap<String, String>
        get() {
            val hashMap = LinkedHashMap<String, String>()
            hashMap["first_name"] = first_name
            hashMap["last_name"] = last_name
            hashMap["email_id"] = email_id
            hashMap["mobile_number"] = mobile_numbers
            // hashMap["country_code"] = ccp.selectedCountryNameCode.replace("\\+".toRegex(), "")
            hashMap["address_line1"] = address_line1
            hashMap["address_line2"] = address_line2
            hashMap["city"] = city
            hashMap["state"] = state
            hashMap["postal_code"] = postal_code
            hashMap["token"] = sessionManager.accessToken!!
            hashMap["profile_image"] = user_thumb_image
            return hashMap
        }

    fun backpressed() {
        onBackPressed()
    }


    fun profile() {
        val intent = Intent(this, DriverProfile::class.java)
        startActivity(intent)
    }

    fun rating() {
        val rating = Intent(this, RatingActivity::class.java)
        startActivity(rating)
    }

    fun earning() {
        val earnings = Intent(this, EarningActivity::class.java)
        startActivity(earnings)
    }


    fun paytoadmin() {
        val payto = Intent(this, PayToAdminActivity::class.java)
        startActivity(payto)
    }


    fun referral() {
        val intent = Intent(this, ShowReferralOptionsActivity::class.java)
        startActivity(intent)
    }

    fun support() {
        val intent = Intent(applicationContext, SupportActivityCommon::class.java)
        startActivity(intent)
    }

    fun privacy() {
        val intent = Intent(this, Privacy::class.java)
        startActivity(intent)
    }

    fun terms() {
        val intent = Intent(this, Terms::class.java)
        startActivity(intent)
    }

    /**
     * Driver Logout
     */
    private fun logout() {
        commonMethods.showProgressDialog(this as AppCompatActivity)
        apiService.logout(sessionManager.type!!, sessionManager.accessToken!!).enqueue(
            RequestCallback(
                Enums.REQ_LOGOUT, this
            )
        )
    }

    fun logoutpopup() {
        val dialog = Dialog(this)
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

    private lateinit var driverProfileModel: DriverProfileModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityProfileMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(
            resources.getString(R.string.profile),
            binding.commonHeader.headertext
        )

        dialog = commonMethods.getAlertDialog(this)

        arrow = binding.commonHeader.arrow

        tvPhone = binding.tvPhone

        tvEmail = binding.tvEmail

        profile_image1 = binding.commonProfile.profileImage1

        binding.commonHeader.back.setOnClickListener {
            backpressed()
        }


        rlt_editprofile = binding.rltEditprofile

        rlt_editprofile.setOnClickListener {
            profile()
        }

        rlt_rating = binding.rltRating

        rlt_rating.setOnClickListener {
            rating()
        }

        rlt_earning = binding.rltEarning
        rlt_earning.setOnClickListener {
            earning()
        }

        rlt_payadmin = binding.rltPayadmin
        rlt_payadmin.setOnClickListener {
            paytoadmin()
        }

        binding.privacylayout.setOnClickListener {
            privacy()
        }

        binding.termslayout.setOnClickListener {
            terms()
        }
        binding.rltSupport.setOnClickListener {
            support()
        }

        binding.rltReferral.setOnClickListener {
            referral()
        }

        binding.logoutlayout.setOnClickListener{
            logoutpopup()
        }


        /*
         * Get driver profile API call
         */
        val profiledetails = sessionManager.profileDetail
        if (profiledetails == null) {
            getDriver()
        } else {
            /*
             *  Load driver profile API
             *
             */
            loadData(profiledetails)
        }


    }


    /**
     * Api call to fetch profile details
     */

    private fun getDriver() {
        commonMethods.showProgressDialog(this)
        apiService.getDriverProfile(sessionManager.accessToken!!)
            .enqueue(RequestCallback(REQ_DRIVER_PROFILE, this))
    }

    /**
     * Upload Image using Post Method
     */
    private fun onSuccessUploadImage(jsonResponse: JsonResponse) {
        Toast.makeText(this, R.string.image_uploaded, Toast.LENGTH_SHORT).show()
        user_thumb_image = commonMethods.getJsonValue(
            jsonResponse.strResponse,
            Constants.PROFILEIMAGE,
            String::class.java
        ) as String
        loadImage(user_thumb_image)
    }

    /**
     * Load Image
     */
    private fun loadImage(imageurl: String) {
        commonMethods.hideProgressDialog()
        //Picasso.get().load(imageurl).into(profile_image1)

    }


    /**
     * Opens the APP Permission Settings Page
     */
    private fun callPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", applicationContext.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 300)
    }

    /**
     * Bottom Sheet to choose camera or gallery
     */

    fun pickProfileImg() {
        val view = layoutInflater.inflate(R.layout.app_camera_dialog_layout, null)
        val lltCamera = view.findViewById<LinearLayout>(R.id.llt_camera)
        val lltLibrary = view.findViewById<LinearLayout>(R.id.llt_library)
        val lltcancel = view.findViewById<LinearLayout>(R.id.llt_cancel)


        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)
        /* bottomSheetDialog.setCancelable(true)*/
        /*if (bottomSheetDialog.window == null) return
        bottomSheetDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        bottomSheetDialog.window!!.setGravity(Gravity.BOTTOM)*/
        if (!bottomSheetDialog.isShowing) {
            bottomSheetDialog.show()
        }

        lltCamera.setOnClickListener {
            bottomSheetDialog.dismiss()
            cameraIntent()
            bottomSheetDialog.dismiss()
        }

        lltLibrary.setOnClickListener {
            bottomSheetDialog.dismiss()
            imageFile = commonMethods.getDefaultFileName(this@ProfileMenu)

            galleryIntent()
            bottomSheetDialog.dismiss()
        }
        lltcancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }


    /**
     * Intent to camera
     */
    private fun cameraIntent() {
        imageFile = commonMethods!!.cameraFilePath(this)
        commonMethods.cameraIntent(imageFile, this)
    }

    /**
     * Intent to gallery page
     */
    private fun galleryIntent() {
        imageFile = commonMethods.getDefaultFileName(this)
        commonMethods.galleryIntent(this)
    }

    /*
     *  Get data from camera and gallery
     */


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_IMAGE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                if (imageFile == null) return
                imagePath = imageFile.path

                if (!TextUtils.isEmpty(imagePath)) {
                    commonMethods.showProgressDialog(this)
                    ImageCompressAsyncTask(docType, this, imagePath, this, "").execute()
                }
            }

            SELECT_FILE -> try {
                val inputStream = this.contentResolver.openInputStream(data!!.data!!)
                val fileOutputStream = FileOutputStream(imageFile)
                commonMethods.copyStream(inputStream, fileOutputStream)
                fileOutputStream.close()
                inputStream?.close()
                if (imageFile == null) return
                imagePath = imageFile.path
                if (!TextUtils.isEmpty(imagePath)) {
                    commonMethods.showProgressDialog(this)
                    ImageCompressAsyncTask(docType, this, imagePath, this, "").execute()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT -> {
                /*  if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_NEW_USER) {
                    updateDriverPhoneNumber(data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY), data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY));
                } else if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_OLD_USER) {
                    commonMethods.showMessage(this, dialog, data.getStringExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY));

                }*/

                if (resultCode == Activity.RESULT_OK) {
                    updateDriverPhoneNumber(
                        data!!.getStringExtra(
                            FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
                        )!!,
                        data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY)!!
                    )
                }

            }

            else -> {
            }
        }

    }

    fun updateDriverPhoneNumber(phoneNumber: String, countryCode: String) {

        findViewById<TextView>(R.id.mobile_number).setText(phoneNumber)
       // mobile_number.setText(phoneNumber)

    }

    /*
     * Get image path from gallery
     */
    private fun onSelectFromGalleryResult(data: Intent?) {

        bm = null
        if (data != null) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            /*  cursor.close()*/
            bm = BitmapFactory.decodeFile(picturePath)

            imagePath = picturePath

            if (!TextUtils.isEmpty(imagePath)) {
                commonMethods.showProgressDialog(this)
                ImageCompressAsyncTask(docType, this, imagePath, this, "").execute()
            }
        }
    }


    override fun onImageCompress(filePath: String, requestBody: RequestBody?) {
        requestBody?.let {
            apiService.uploadProfileImage(it, sessionManager.accessToken!!)
                .enqueue(RequestCallback(REQ_UPLOAD_PROFILE_IMG, this))
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_UPDATE_PROFILE -> {
                val statusmessage =
                    jsonResp.statusMsg  //(String) commonMethods.getJsonValue(jsonResp.getStrResponse(), "status_message", String.class);
                if (jsonResp.isSuccess) {
                    sessionManager.profileDetail = jsonResp.strResponse

                    if (isUpdate) {
                        isUpdate = false
                        commonMethods.showMessage(
                            this,
                            dialog,
                            resources.getString(R.string.update_successfully)
                        )

                    } else {
                        loadData(jsonResp.strResponse)
                        //jsonResp.strResponse?.let { loadData(it) }
                    }
                } else {
                    commonMethods.showMessage(this, dialog, statusmessage)
                }
            }

            REQ_UPLOAD_PROFILE_IMG -> if (jsonResp.isSuccess) {
                onSuccessUploadImage(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }

            REQ_DRIVER_PROFILE -> {
                val currency_code = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "currency_code",
                    String::class.java
                ) as String
                val dialcode = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "country_code",
                    String::class.java
                ) as String
                var currency_symbol = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "currency_symbol",
                    String::class.java
                ) as String
                //val carid = commonMethods.getJsonValue(jsonResp.strResponse, "car_id", String::class.java) as String
                val oweAmount = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "owe_amount",
                    String::class.java
                ) as String
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    currency_symbol =
                        Html.fromHtml(currency_symbol, Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    currency_symbol = Html.fromHtml(currency_symbol).toString()
                }
                sessionManager.currencyCode = currency_code
                sessionManager.countryCode = dialcode
                sessionManager.currencySymbol = currency_symbol
                //sessionManager.vehicle_id = carid
                sessionManager.oweAmount = oweAmount
            }

            else -> {
            }
        }


    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    fun loadData(profiledetails: String) {
        try {
            val jsonObj = JSONObject(profiledetails)

            first_name = jsonObj.getString("first_name")
            last_name = jsonObj.getString("last_name")
            mobile_numbers = jsonObj.getString("mobile_number")
            country_code = jsonObj.getString("country_code")
            CommonMethods.DebuggableLogI("Country code from api", country_code)
            email_id = jsonObj.getString("email_id")
            user_thumb_image = jsonObj.getString("profile_image")
            address_line1 = jsonObj.getString("address_line1")
            address_line2 = jsonObj.getString("address_line2")
            city = jsonObj.getString("city")
            state = jsonObj.getString("state")
            postal_code = jsonObj.getString("postal_code")
            gender = jsonObj.getString("gender")


            binding.ccp.setCountryForNameCode(country_code)
            binding.ccp.setCcpClickable(false)

            tvPhone.setText(mobile_numbers)
            tvEmail.setText(email_id)
            binding.nametext.text = "$first_name $last_name"
            Picasso.get().load(user_thumb_image).into(profile_image1)


        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    /*
     * Edit address request focus
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.getisEdit()) {
            R.id.mobile_number
            binding.tvPhone.text = sessionManager.phoneNumber
         //  binding. mobile_number.setText(sessionManager.phoneNumber)
            CommonMethods.DebuggableLogI("Country code from session", sessionManager.countryCode)
            // ccp.setCountryForNameCode(sessionManager.countryCode)
            sessionManager.setisEdit(false)
        }
    }

    override fun permissionGranted(
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {
        pickProfileImg()
    }

    override fun permissionDenied(
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {

    }

    /*
     *  edit taxtwatcher
     */

    companion object {

        /*
     * check is valid email or not
     */

    }

}
