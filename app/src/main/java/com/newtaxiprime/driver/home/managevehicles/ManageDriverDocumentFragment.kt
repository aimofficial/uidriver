package com.newtaxiprime.driver.home.managevehicles
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.home.managevehicles.adapter.ManageDocumentsAdapter
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.home.datamodel.DriverProfileModel
import javax.inject.Inject

class ManageDriverDocumentFragment : Fragment(), ManageDocumentsAdapter.OnClickListener {


    lateinit var manageDocuments: View

    lateinit var rvDocs: RecyclerView
    lateinit var tvNoDocument: TextView

    @Inject
    lateinit var sessionManager: SessionManager

    lateinit var driverProfileModel: DriverProfileModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        manageDocuments = inflater.inflate(R.layout.manage_documents_fragment, container, false)
        rvDocs = manageDocuments.findViewById<RecyclerView>(R.id.rv_docs)
        tvNoDocument = manageDocuments.findViewById<TextView>(R.id.tv_no_document)
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this, manageDocuments)

        driverProfileModel = Gson().fromJson(sessionManager.profileDetail, DriverProfileModel::class.java)


        initRecyclerView()
        (activity as DocumentDetails).setHeader(resources.getString(R.string.manage_driver_document))

        return manageDocuments
    }


    private fun initRecyclerView() {

        val adapter = ManageDocumentsAdapter(requireContext(), driverProfileModel.driverDocuments, this)
        rvDocs.adapter = adapter


        if(driverProfileModel.driverDocuments.isEmpty())
            tvNoDocument.visibility = View.VISIBLE
        else
            tvNoDocument.visibility = View.GONE

    }

    override fun onClick(pos: Int) {
        (activity as DocumentDetails).documentPosition = pos
        findNavController().navigate(R.id.action_documentFragment_to_viewDocumentFragment)

    }

}
