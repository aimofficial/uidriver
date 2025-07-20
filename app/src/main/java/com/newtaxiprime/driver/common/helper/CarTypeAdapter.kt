package com.newtaxiprime.driver.common.helper

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.home.MainActivity
import com.newtaxiprime.driver.home.datamodel.VehiclesModel
import com.newtaxiprime.driver.home.interfaces.ApiService
import javax.inject.Inject


class CarTypeAdapter(
    private val context: Context,
    private val vehiclesModelList: ArrayList<VehiclesModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var dialog: AlertDialog

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson
    var selectedcar = 0;
    var vehiclesModelTypeModelList = ArrayList<VehiclesModel>()

    private var vehiclesModelClickListener: onVehicleClickListener? = null


    fun setOnVehicleClickListner(clickListner: onVehicleClickListener) {
        this.vehiclesModelClickListener = clickListner
    }

    interface onVehicleClickListener {
        fun setVehicleClick(vehiclesModelTypeModelList: VehiclesModel, position: Int)
    }

    init {
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this.context)
        vehiclesModelTypeModelList = ArrayList<VehiclesModel>()
        vehiclesModelTypeModelList.addAll(vehiclesModelList)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)
        val viewItem: View

        viewItem = inflater.inflate(R.layout.vehicle_type_layout, parent, false)

        viewHolder = VehicleTypeViewHolder(viewItem)

        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vehiclesModelTypeModel = vehiclesModelTypeModelList!![position] // Upcoming Detail

        val vehiclesModelTypeViewHolder = holder as VehicleTypeViewHolder

        vehiclesModelTypeViewHolder.tvVehicleType?.text = vehiclesModelTypeModel.vehicleName
        vehiclesModelTypeViewHolder.tvVehicleNumber?.text = vehiclesModelTypeModel.licenseNumber

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_enabled)
            ),
            intArrayOf(

                ContextCompat.getColor(context, R.color.newtaxi_app_black),
                ContextCompat.getColor(context, R.color.newtaxi_app_navy)
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vehiclesModelTypeViewHolder.rbVehicleType?.buttonTintList = colorStateList
        }

        if (vehiclesModelTypeModel.isDefault != null && vehiclesModelTypeModel.isDefault.equals("1")) {
            vehiclesModelTypeViewHolder.rbVehicleType?.isChecked = true
            (context as MainActivity).selectedFragment.updateUI(position)
        } else
            vehiclesModelTypeViewHolder.rbVehicleType?.isChecked = false

        if (vehiclesModelTypeModel.isActive != null && vehiclesModelTypeModel.isActive == 0) {
            Color.LTGRAY
            vehiclesModelTypeViewHolder.tvVehicleNumber?.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.newtaxi_app_light_grey
                )
            )
            vehiclesModelTypeViewHolder.tvVehicleType?.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.newtaxi_app_light_grey

                )
            )
        } else {
            vehiclesModelTypeViewHolder.tvVehicleNumber?.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.newtaxi_app_text_ash
                )
            )
            vehiclesModelTypeViewHolder.tvVehicleType?.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.newtaxi_app_black
                )
            )
        }

        vehiclesModelTypeViewHolder.rltVehicleType?.setOnClickListener {
            vehiclesModelClickListener?.setVehicleClick(
                vehiclesModelTypeModelList.get(position),
                position
            )
        }

    }


    override fun getItemCount(): Int {
        return vehiclesModelTypeModelList.size
    }


    protected inner class VehicleTypeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var tvVehicleType: TextView? = null
        internal var tvVehicleNumber: TextView? = null
        internal var rbVehicleType: RadioButton? = null
        internal var rltVehicleType: RelativeLayout? = null


        init {

            tvVehicleType = view.findViewById<View>(R.id.tv_vehicle_type) as? TextView
            tvVehicleNumber = view.findViewById<View>(R.id.tv_vehicle_number) as? TextView
            rbVehicleType = view.findViewById<View>(R.id.rb_vehicle_type) as? RadioButton
            rltVehicleType = view.findViewById<View>(R.id.rlt_vehicle_type) as? RelativeLayout

        }
    }


}
