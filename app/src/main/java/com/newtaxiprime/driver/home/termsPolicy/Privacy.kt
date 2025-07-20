package com.newtaxiprime.driver.home.termsPolicy


import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.databinding.PrivacyPolicyBinding

class Privacy : AppCompatActivity() {

    lateinit var webView: WebView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = PrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webView = binding.webView


        // WebViewClient allows you to handle

        // onPageFinished and override Url loading.
        webView.webViewClient = WebViewClient()

        // this will load the url of the website
        webView.loadUrl(resources.getString(R.string.privacy_policy))

        // this will enable the javascript settings, it can also allow xss vulnerabilities
        webView.settings.javaScriptEnabled = true

        // if you want to enable zoom feature
        webView.settings.setSupportZoom(true)
    }

    // if you press Back button this code will work
    override fun onBackPressed() {
        // if your webview can go back it will go back
        if (webView.canGoBack())
            webView.goBack()
        // if your webview cannot go back
        // it will exit the application
        else
            super.onBackPressed()
    }
}
