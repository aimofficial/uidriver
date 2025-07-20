package com.newtaxiprime.driver.trips.viewmodel

import androidx.lifecycle.LiveData
import com.newtaxiprime.driver.common.model.JsonResponse

interface RequestAcceptActivityInterface {

    fun onSuccessResponse(jsonResponse: LiveData<JsonResponse>)
    fun onFailureResponse(jsonResponse: LiveData<JsonResponse>)

}