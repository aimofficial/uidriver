package com.newtaxiprime.driver.home.managevehicles

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.newtaxiprime.driver.BuildConfig
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.dependencies.module.ImageCompressAsyncTask
import com.newtaxiprime.driver.common.helper.Constants
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.Enums.UPLOAD_VEHICLE_DOCUMENT
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.util.RuntimePermissionDialogFragment
import com.newtaxiprime.driver.databinding.ViewDocumentLayoutBinding
import com.newtaxiprime.driver.home.datamodel.AddDocumentDetails
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ImageListener
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ViewVehicleDocumentFragment : Fragment(), ServiceListener, ImageListener,
    RuntimePermissionDialogFragment.RuntimePermissionRequestedCallback,
    DatePickerDialog.OnDateSetListener {


    lateinit var viewDocument: View

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
    private var dialog: AlertDialog? = null
    private lateinit var imageFile: File
    private lateinit var imageUri: Uri
    private var imagePath: String = ""
    var docType = 9
    var isExpiredateRequired = ""
    private lateinit var binding: ViewDocumentLayoutBinding
    var documentUrl: String? = null
    private var isImageuupload: Boolean = false

    private var addDocumentDetails = AddDocumentDetails()

    lateinit var rltExpiryDate: RelativeLayout

    lateinit var tvExpiryDate: TextView

    private var isExpirydatechange: Boolean = false

    lateinit var ivImage: ImageView

    lateinit var rltNext: RelativeLayout




    /**
     * Add Docs
     */
    fun uploadImage() {
        if (Build.VERSION.SDK_INT >= 33) {
            RuntimePermissionDialogFragment.checkPermissionStatus(
                requireActivity(),
                requireActivity().supportFragmentManager,
                this,
                RuntimePermissionDialogFragment.IMAGE_PERMISSION_ARRAY,
                0,
                0
            )
        } else {
            RuntimePermissionDialogFragment.checkPermissionStatus(
                requireActivity(),
                requireActivity().supportFragmentManager,
                this,
                RuntimePermissionDialogFragment.CAMERA_PERMISSION_ARRAY,
                0,
                0
            )
        }
    }


    fun OnNext() {

        if (!sessionManager.isTrip) {
            if (isExpirydatechange || isImageuupload) {
                Log.d("Document","Upload Document")
                Toast.makeText(context, "On next Click", Toast.LENGTH_SHORT).show()

                if (documentUrl != null && !documentUrl.equals("")) {
                    Log.d("Document","document url is not null")
                    val expirydate = tvExpiryDate.text.toString()
                    commonMethods.showProgressDialog((activity as ManageVehicles).getAppCompatActivity())
                    sessionManager.documentId =
                        (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get(
                            (activity as ManageVehicles).documentPosition!!
                        ).documentId.toString()
                    sessionManager.vehicleId =
                        (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).id
                    ImageCompressAsyncTask(
                        docType,
                        (activity as ManageVehicles).getAppCompatActivity(),
                        imagePath,
                        this,
                        expirydate
                    ).execute()

                } else {

                    val expirydate = tvExpiryDate.text.toString()
                    if (!TextUtils.isEmpty(imagePath) || expirydate.isNotEmpty()) {

                        commonMethods.showProgressDialog((activity as ManageVehicles).getAppCompatActivity())
                        sessionManager.documentId =
                            (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get(
                                (activity as ManageVehicles).documentPosition!!
                            ).documentId.toString()
                        sessionManager.vehicleId =
                            (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).id
                        ImageCompressAsyncTask(
                            docType,
                            (activity as ManageVehicles).getAppCompatActivity(),
                            imagePath,
                            this,
                            expirydate
                        ).execute()
                        Log.d("Document","Executed")

                    } else if (TextUtils.isEmpty(imagePath)) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.please_upload_legal_document),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (expirydate.isEmpty() && isExpiredateRequired.equals("1")) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.please_expiry_date),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            } else {
                (activity as ManageVehicles).onBackPressed()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "you can't update document While  you are in trip ",
                Toast.LENGTH_LONG
            ).show()
        }


    }

    /*
    Choose Expiry Date
   */

    fun OnExpiryDate() {
        val c = Calendar.getInstance()
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_datepicker)
        // set the custom dialog components - text, image and button
        val cancel = dialog.findViewById<View>(R.id.datepicker_cancel) as TextView
        val picker = dialog.findViewById<View>(R.id.datePicker) as DatePicker
        val ok = dialog.findViewById<View>(R.id.datepicker_ok) as TextView
        // if button is clicked, close the custom dialog
        cancel.setOnClickListener { dialog.dismiss() }

        picker.minDate = c.getTimeInMillis()
        ok.setOnClickListener {

            c.set(picker.year, picker.month, picker.dayOfMonth);
            val format = SimpleDateFormat("yyyy-MM-dd");
            val datestring = format.format(c.getTime());
            isExpirydatechange = true
            tvExpiryDate.setText(datestring)
            dialog.dismiss()
        }
        dialog.show()
        /* val locale =  Locale("en");
         Locale.setDefault(locale);
         val config =  Configuration();
         config.setLocale(locale)
         config.setTo(config)
         config.setToDefaults()
         if(Build.VERSION.SDK_INT> Build.VERSION_CODES.JELLY_BEAN){
             config.setLocale(locale);
             (activity as ManageVehicles).createConfigurationContext(config)
             // getContext getContext()?.createConfigurationContext(config);
         }else { //deprecated
             config.setLocale(locale);
             resources.updateConfiguration(config, getResources().getDisplayMetrics());
         }*/
        /* val datePickerDialog1=DatePickerDialog(activity,this,Calendar.getInstance(Locale("en")).get(Calendar.YEAR)
                 ,Calendar.getInstance(Locale("en")).get(Calendar.MONTH)
                 ,Calendar.getInstance(Locale("en")).get(Calendar.DAY_OF_MONTH))
         datePickerDialog1.show()*/

        /*  val datePickerDialog = DatePickerDialog(activity,
                  DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                      c.set(Calendar.YEAR,year)
                      c.set(Calendar.MONTH,monthOfYear)
                      c.set(Calendar.DAY_OF_MONTH,dayOfMonth)
                      val  myFormat = "yyyy-MM-dd";
                      val sdf =  SimpleDateFormat(myFormat, Locale.US)
                      tvExpiryDate.text=sdf.format(c.time)
                      val expiryDate = SimpleDateFormat(myFormat, Locale("en"))
                      println("exp Date "+expiryDate.format(c.time))

                  }, mYear, mMonth, mDay)
          datePickerDialog.datePicker.minDate = c.getTimeInMillis()

          datePickerDialog.show()*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.view_document_layout, container, false
        );


        rltExpiryDate = binding.rltExpiredate

        rltExpiryDate.setOnClickListener {
            OnExpiryDate()
        }
        binding.rltTapToAdd.setOnClickListener {
            uploadImage()
        }

        binding.btnNext.setOnClickListener {
            OnNext()
        }



        tvExpiryDate = binding.tvExpiredateText


        ivImage = binding.ivImage

        rltNext = binding.rltNext
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this, binding.root)

        (activity as ManageVehicles).setHeader(
            (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get(
                (activity as ManageVehicles).documentPosition!!
            ).documentName!!
        )

        dialog = commonMethods.getAlertDialog(requireContext())

        binding.viewDoc =
            (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get(
                (activity as ManageVehicles).documentPosition!!
            )


        isExpiredateRequired =
            (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get(
                (activity as ManageVehicles).documentPosition!!
            ).expiryRequired!!

        documentUrl =
            (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get(
                (activity as ManageVehicles).documentPosition!!
            ).documentUrl

        if (isExpiredateRequired.equals("1"))
            rltExpiryDate.visibility = View.VISIBLE
        else
            rltExpiryDate.visibility = View.GONE

        return binding.root
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(context, dialog, data)
            return
        }

        when (jsonResp.requestCode) {
            UPLOAD_VEHICLE_DOCUMENT ->
                if (jsonResp.isSuccess) {
                    onSuccessVehicleDocument(jsonResp)
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
                }

            else -> {

            }
        }
    }

    private fun onSuccessVehicleDocument(jsonResp: JsonResponse) {
        addDocumentDetails = gson.fromJson(jsonResp.strResponse, AddDocumentDetails::class.java)
        /*(activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get((activity as ManageVehicles).documentPosition!!).documentUrl = addDocumentDetails.document.get((activity as ManageVehicles).documentPosition!!).documentUrl
        (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get((activity as ManageVehicles).documentPosition!!).documentStatus = "0"
        (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get((activity as ManageVehicles).documentPosition!!).expiredDate =addDocumentDetails.document.get((activity as ManageVehicles).documentPosition!!).expiredDate*/
        imagePath = ""
        isImageuupload = false


        (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get(
            (activity as ManageVehicles).documentPosition!!
        ).documentUrl = addDocumentDetails.documentUrl
        (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get(
            (activity as ManageVehicles).documentPosition!!
        ).documentUrl = addDocumentDetails.documentUrl
        (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get(
            (activity as ManageVehicles).documentPosition!!
        ).documentStatus = "0"
        (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).vehicleStatus =
            resources.getString(R.string.inactive)
        (activity as ManageVehicles).onBackPressed()

    }

    override fun onFailure(jsonResp: JsonResponse?, data: String?) {

    }

    /**
     * To fetch RequestBody from ImageCompressAsyncTask
     * @param filePath file path of the image
     * @param requestBody request body from image compress async task
     */

    override fun onImageCompress(filePath: String, requestBody: RequestBody?) {
        commonMethods.hideProgressDialog()
        if (requestBody != null) {
            commonMethods.showProgressDialog((activity as ManageVehicles).getAppCompatActivity())
            apiService.updateDocument(requestBody, sessionManager.accessToken!!)
                .enqueue(RequestCallback(UPLOAD_VEHICLE_DOCUMENT, this))
        }
    }


    /*
      * Get image path from gallery
      */
    private fun onSelectFromGalleryResult(data: Intent?) {

        var bm: Bitmap? = null
        if (data != null) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = requireContext().contentResolver.query(
                selectedImage!!,
                filePathColumn,
                null,
                null,
                null
            )
            cursor?.moveToFirst()
            val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
            //   val picturePath = cursor.getString(columnIndex)
            val picturePath = columnIndex?.let { cursor.getString(it) }
            cursor?.close()
            bm = BitmapFactory.decodeFile(picturePath)
            ivImage.setImageBitmap(bm)
            binding.tvTapToAdd.text = requireContext().resources.getString(R.string.taptochange)
            imagePath = picturePath.toString()
            isImageuupload = true

            /*  if (!TextUtils.isEmpty(imagePath)) {
                  commonMethods.showProgressDialog((activity as ManageVehicles).getAppCompatActivity())
                  sessionManager.documentId = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).document.get((activity as ManageVehicles).documentPosition!!).documentId.toString()
                  sessionManager.vehicleId = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).id
                  ImageCompressAsyncTask(docType, (activity as ManageVehicles).getAppCompatActivity(), imagePath, this).execute()
              }*/
        }
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                Constants.PICK_IMAGE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                    if (imageFile == null) return
                    imagePath = imageFile.path
                    isImageuupload = true
                    val bm = BitmapFactory.decodeFile(imagePath)
                    ivImage.setImageBitmap(bm)
                    binding.tvTapToAdd.text =
                        requireContext().resources.getString(R.string.taptochange)
                }

                Constants.SELECT_FILE -> try {
                    val inputStream = activity?.contentResolver?.openInputStream(data!!.data!!)
                    val fileOutputStream = FileOutputStream(imageFile)
                    commonMethods.copyStream(inputStream, fileOutputStream)
                    fileOutputStream.close()
                    inputStream?.close()
                    imagePath = imageFile.path
                    val bm = BitmapFactory.decodeFile(imagePath)
                    ivImage.setImageBitmap(bm)
                    binding.tvTapToAdd.text =
                        requireContext().resources.getString(R.string.taptochange)
                    isImageuupload = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun permissionGranted(
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {
        documentUpload()
    }

    override fun permissionDenied(
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {

    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val c = Calendar.getInstance(Locale("en"))
        c.set(Calendar.YEAR, p1)
        c.set(Calendar.MONTH, p2)
        c.set(Calendar.DAY_OF_MONTH, p3)
        val myFormat = "yyyy-MM-dd";
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        tvExpiryDate.text = sdf.format(c.time)
        val expiryDate = SimpleDateFormat(myFormat, Locale("en"))
        println("exp Date " + expiryDate.format(c.time))
    }

    /**
     * Bottom Sheet to choose camera or gallery
     */

    fun documentUpload() {
        val view = layoutInflater.inflate(R.layout.app_camera_dialog_layout, null)
        val lltCamera = view.findViewById<LinearLayout>(R.id.llt_camera)
        val lltLibrary = view.findViewById<LinearLayout>(R.id.llt_library)
        val lltcancel = view.findViewById<LinearLayout>(R.id.llt_cancel)


        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)
        /* bottomSheetDialog.setCancelable(true)*/
        /*if (bottomSheetDialog.window == null) return
        bottomSheetDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        bottomSheetDialog.window!!.setGravity(Gravity.BOTTOM)*/
        if (!bottomSheetDialog.isShowing) {
            bottomSheetDialog.show()
        }

        lltCamera.setOnClickListener {
            cameraIntent()
            bottomSheetDialog.dismiss()
        }

        lltLibrary.setOnClickListener {
            imageFile = commonMethods.getDefaultFileName(requireContext())

            galleryIntent()
            bottomSheetDialog.dismiss()
        }
        lltcancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun galleryIntent() {
        imageFile = commonMethods.getDefaultFileName(requireContext())
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, Constants.SELECT_FILE)
    }

    private fun cameraIntent() {
        imageFile = commonMethods!!.cameraFilePath(requireContext())
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageUri = activity?.let { it1 ->
            imageFile?.let { it2 ->
                FileProvider.getUriForFile(
                    it1,
                    BuildConfig.APPLICATION_ID + ".provider",
                    it2
                )
            }
        }
        try {
            val resolvedIntentActivities = activity?.packageManager?.queryIntentActivities(
                cameraIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            if (resolvedIntentActivities != null) {
                for (resolvedIntentInfo in resolvedIntentActivities) {
                    val packageName = resolvedIntentInfo.activityInfo.packageName
                    activity?.grantUriPermission(
                        packageName,
                        imageUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
            cameraIntent.putExtra("return-data", true)
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, Constants.PICK_IMAGE_REQUEST_CODE)
        activity?.let { it1 -> commonMethods.refreshGallery(it1, imageFile) }
    }

}
