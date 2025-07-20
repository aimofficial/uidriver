package com.newtaxiprime.driver.home.managevehicles

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.Enums.DELETE_VEHICLE
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.databinding.ManageVehicleFragmentBinding
import com.newtaxiprime.driver.home.datamodel.AddedVehiclesModel
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.newtaxiprime.driver.home.managevehicles.adapter.ManageVehicleAdapter
import javax.inject.Inject

class ManageVehicleFragment : Fragment(), ManageVehicleAdapter.OnClickListener, ServiceListener {

    private var deletePosition: Int? = null
    private var addedVehicleDetails = AddedVehiclesModel()

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

    lateinit var manageVehicle: View


    lateinit var rvVehicles: RecyclerView
    lateinit var adapter: ManageVehicleAdapter

    lateinit var binding: ManageVehicleFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ManageVehicleFragmentBinding.inflate(layoutInflater)
        manageVehicle = binding.root
        rvVehicles = binding.rvVehicles
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this, manageVehicle)

        (activity as ManageVehicles).setHeader(getString(R.string.vehicleinformation))

        return manageVehicle
    }


    override fun onResume() {
        super.onResume()
        (activity as ManageVehicles).initViews()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        dialog = commonMethods.getAlertDialog(requireContext())


        adapter = ManageVehicleAdapter(
            requireContext(),
            (activity as ManageVehicles).vehicleDetails,
            this
        )
        rvVehicles.adapter = adapter

        if ((activity as ManageVehicles).vehicleDetails.size == 0)
            binding.tvNoVehicles.visibility = View.VISIBLE
        else
            binding.tvNoVehicles.visibility = View.GONE


    }

    override fun onClick(pos: Int, clickType: String) {
        (activity as ManageVehicles).vehicleClickPosition = pos
        if (clickType.equals(CommonKeys.DOCUMENT)) {
            (activity as ManageVehicles).documentClickedPosition = pos
            findNavController().navigate(R.id.action_vehicleFragment_to_documentFragment)
        } else if (clickType.equals(CommonKeys.EDIT))
            findNavController().navigate(R.id.action_vehicleFragment_to_addVehicle)
        else if (clickType.equals(CommonKeys.DELETE)) {
            confirmationPopup(pos)
        }


    }

    private fun confirmationPopup(pos: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        // set the custom dialog components - text, image and button
        val tvMessage = dialog.findViewById<View>(R.id.tv_message) as TextView
        val cancel = dialog.findViewById<View>(R.id.signout_cancel) as TextView
        val delete = dialog.findViewById<View>(R.id.signout_signout) as TextView
        delete.text = resources.getString(R.string.delete)
        tvMessage.text = resources.getString(R.string.delete_msg)
        // if button is clicked, close the custom dialog
        cancel.setOnClickListener { dialog.dismiss() }

        delete.setOnClickListener {
            deletePosition = pos
            deleteVehicle()
            dialog.dismiss()

        }
        dialog.show()
    }

    private fun deleteVehicle() {

        commonMethods.showProgressDialog((activity as ManageVehicles).getAppCompatActivity())
        apiService.deleteVehicle(
            sessionManager.accessToken!!,
            (activity as ManageVehicles).vehicleDetails.get(deletePosition!!).id
        ).enqueue(RequestCallback(DELETE_VEHICLE, this))
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(context, dialog, data)
            return
        }

        when (jsonResp.requestCode) {
            DELETE_VEHICLE -> if (jsonResp.isSuccess) {
                onSuccessVehicleDeleted(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
            }

            else -> {
            }
        }
    }

    private fun onSuccessVehicleDeleted(jsonResp: JsonResponse) {

        (activity as ManageVehicles).vehicleDetails.removeAt(deletePosition!!)
        adapter.notifyDataSetChanged()

    }


    override fun onFailure(jsonResp: JsonResponse?, data: String?) {

    }

}
