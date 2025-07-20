package com.newtaxiprime.driver.common.util.userchoice

interface UserChoiceSuccessResponse {
    fun onSuccessUserSelected(type: String?, userChoiceData: String?, userChoiceCode: String?)
}