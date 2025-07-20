package com.newtaxiprime.driver.home.managevehicles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.databinding.ManageDocumentsFragmentBinding
import com.newtaxiprime.driver.home.managevehicles.adapter.ManageDocumentsAdapter
import javax.inject.Inject

class ManageVehicleDocumentFragment : Fragment(), ManageDocumentsAdapter.OnClickListener {


    lateinit var manageDocuments: View

    lateinit var rvDocs: RecyclerView


    @Inject
    lateinit var commonMethods: CommonMethods

    lateinit var tvNoDocument: TextView

    lateinit var binding: ManageDocumentsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ManageDocumentsFragmentBinding.inflate(inflater)

        manageDocuments = binding.root

        tvNoDocument = binding.tvNoDocument

        rvDocs = binding.rvDocs
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this, manageDocuments)

        initRecyclerView()

        return manageDocuments
    }


    private fun initRecyclerView() {

        val adapter = ManageDocumentsAdapter(
            requireContext(),
            (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).documentClickedPosition!!).document,
            this
        )
        rvDocs.adapter = adapter

        (activity as ManageVehicles).setHeader(getString(R.string.manage_documents))
        (activity as ManageVehicles).hideAddButton()



        if ((activity as ManageVehicles).vehicleDetails.size == 0)
            tvNoDocument.visibility = View.VISIBLE
        else
            tvNoDocument.visibility = View.GONE

    }

    override fun onClick(pos: Int) {
        (activity as ManageVehicles).documentPosition = pos
        findNavController().navigate(R.id.action_documentFragment_to_viewDocumentFragment2)

    }

}
