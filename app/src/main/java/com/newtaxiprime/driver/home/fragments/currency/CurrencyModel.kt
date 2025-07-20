package com.newtaxiprime.driver.home.fragments.currency

import java.io.Serializable

class CurrencyModel : Serializable {

    var currencySymbol: String? = null
    var currencyName: String? = null

    constructor() {

    }

    constructor(currencyname: String, currencysymbol: String) {
        this.currencyName = currencyname
        this.currencySymbol = currencysymbol
    }

}
