package com.newtaxiprime.driver.home.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.newtaxiprime.driver.R


class GoogleMapPlaceSearchAutoCompleteRecyclerView(private var autocompletePredictions: MutableList<AutocompletePrediction>?, private var mContext: Context, private val autoCompleteAddressTouchListener: AutoCompleteAddressTouchListener) : RecyclerView.Adapter<GoogleMapPlaceSearchAutoCompleteRecyclerView.RecyclerViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = inflater.inflate(R.layout.location_search, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val currentObject = autocompletePredictions!![position]
        holder.address.text = currentObject.getPrimaryText(null)
        holder.addressSecondary.text = currentObject.getSecondaryText(null)
        holder.predictedRow.setOnClickListener { autoCompleteAddressTouchListener.selectedAddress(currentObject) }
    }

    fun updateList(autocompletePredictions: MutableList<AutocompletePrediction>) {
        this.autocompletePredictions = autocompletePredictions
        notifyDataSetChanged()
    }

    fun clearAddresses() {
        try {
            this.autocompletePredictions!!.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return autocompletePredictions!!.size
    }

    inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var address: TextView = itemView.findViewById(R.id.address)
        var addressSecondary: TextView = itemView.findViewById(R.id.address_secondry)
        var predictedRow: RelativeLayout = itemView.findViewById(R.id.predictedRow)
    }

    interface AutoCompleteAddressTouchListener {
        fun selectedAddress(autocompletePrediction: AutocompletePrediction?)
    }
}