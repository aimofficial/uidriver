package com.newtaxiprime.driver.home.managevehicles

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.home.datamodel.DocumentsModel
import javax.inject.Inject


class DocumentDetails : CommonActivity() {
    fun onBack() {
        onBackPressed()
    }

    lateinit var tvTitle: TextView

    @Inject
    lateinit var commonMethods: CommonMethods

    var documentDetails = ArrayList<DocumentsModel>()
    var documentPosition: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_details)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.setOnClickListener {
            onBackPressed()
        }
        val back = findViewById<ImageView>(R.id.ivBack)
        back.setOnClickListener {
            onBack()
        }
        getIntentValues()
    }

    private fun getIntentValues() {

        if (intent.extras != null) {
            documentDetails =
                intent.getSerializableExtra(CommonKeys.Intents.DocumentDetailsIntent) as ArrayList<DocumentsModel>
            setHeader(getString(R.string.manage_documents))
        }

    }


    internal fun getAppCompatActivity(): CommonActivity {
        return this
    }


    fun setHeader(title: String) {
        try {
            Log.i("MNG_DOC", "setHeader: Doc title=$title")
            Log.i("MNG_DOC", "setHeader: Doc tvTitle is null=${(false)}")
            tvTitle.text = title
        } catch (e: Exception) {
            Log.i("MNG_DOC", "setHeader: Error=${e.localizedMessage}")
        }
    }


}
