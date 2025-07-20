package com.newtaxiprime.driver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TripStatusModel {


    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("otp")
    @Expose
    var tripOtp: String = ""

    @SerializedName("data")
    @Expose
    var tripDetailsModel: TripDetailsModel? = null



}