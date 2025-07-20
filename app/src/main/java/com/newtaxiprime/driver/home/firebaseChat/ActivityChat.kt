package com.newtaxiprime.driver.home.firebaseChat

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.AppActivityChatBinding
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.newtaxiprime.driver.home.pushnotification.NotificationUtils
import com.squareup.picasso.Picasso
import org.json.JSONObject
import javax.inject.Inject


class ActivityChat : CommonActivity(), FirebaseChatHandler.FirebaseChatHandlerInterface,
    ServiceListener {


    private var chatJson: String? = null

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var gson: Gson

    lateinit var riderProfileImageView: ImageView

    lateinit var profileName: TextView

    lateinit var profileRating: TextView

    lateinit var newMessage: EditText

    lateinit var rv: RecyclerView

    lateinit var noChats: ImageView

    lateinit internal var adapterFirebaseRecylcerview: AdapterFirebaseRecylcerview
    lateinit internal var firebaseChatHandler: FirebaseChatHandler
    internal var sourceActivityCode: Int = 0

    private lateinit var binding: AppActivityChatBinding

    fun backIconClickEvent() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        riderProfileImageView = binding.imgvuRiderProfile

        profileName = binding.tvProfileName


        profileRating = binding.tvProfileRating

        newMessage = binding.edtNewMsg

        rv = binding.rvChat

        noChats = binding.imgvuEmptychat

        binding.imgvuBack.setOnClickListener {
            backIconClickEvent()
        }

        binding.ivSend.setOnClickListener {
            sendMessage()
        }

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)

    }

    private fun getIntentFromPush() {
        isOnChat = true
        chatJson = sessionManager.chatJson

        if (chatJson != null && !chatJson.equals("")) {
            val json = JSONObject(chatJson)
            sessionManager.chatJson = ""
            sessionManager.riderName =
                json.getJSONObject("custom").getJSONObject("chat_notification")
                    .getString("user_name")
            sessionManager.tripId =
                json.getJSONObject("custom").getJSONObject("chat_notification").getString("trip_id")
            sessionManager.riderProfilePic =
                json.getJSONObject("custom").getJSONObject("chat_notification")
                    .getString("user_image")
            sessionManager.riderId =
                json.getJSONObject("custom").getJSONObject("chat_notification").getString("user_id")
            sessionManager.riderRating =
                json.getJSONObject("custom").getJSONObject("chat_notification").getString("rating")
        }
    }

    override fun pushMessage(firebaseChatModelClass: FirebaseChatModelClass?) {
        firebaseChatModelClass?.let { adapterFirebaseRecylcerview.updateChat(it) }
        rv.scrollToPosition(adapterFirebaseRecylcerview.itemCount - 1)
        rv.visibility = View.VISIBLE
        noChats.visibility = View.GONE
    }

    fun sendMessage() {
        if (commonMethods.isOnline(this)) {
            firebaseChatHandler.addMessage(newMessage.text.toString().trim { it <= ' ' })
            apiService.updateChat(getChatParams()).enqueue(RequestCallback(this))
            newMessage.text.clear()
        } else {
            Toast.makeText(this, resources.getString(R.string.network_failure), Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun getChatParams(): HashMap<String, String> {

        val chatHashMap: java.util.HashMap<String, String> = HashMap()
        chatHashMap["message"] = newMessage.text.toString()
        chatHashMap["trip_id"] = sessionManager.tripId.toString()
        chatHashMap["receiver_id"] = sessionManager.riderId
        chatHashMap["token"] = sessionManager.accessToken!!
        return chatHashMap

    }

    override fun onDestroy() {
        super.onDestroy()
        isOnChat = false
        firebaseChatHandler.unRegister()
    }

    override fun onBackPressed() {
        /*if (sourceActivityCode == CommonKeys.FIREBASE_CHAT_ACTIVITY_REDIRECTED_FROM_RIDER_OR_DRIVER_PROFILE) {
            super.onBackPressed();
        } else {
            CommonMethods.gotoMainActivityFromChatActivity(this);
        }*/
        super.onBackPressed()

    }

    override fun onResume() {
        super.onResume()
        //stopFirebaseChatListenerService(this)

        NotificationUtils.clearNotifications(this)

        getIntentFromPush()
        updateRiderProfileOnHeader()
        rv.layoutManager = LinearLayoutManager(this)
        adapterFirebaseRecylcerview = AdapterFirebaseRecylcerview(this)
        rv.adapter = adapterFirebaseRecylcerview
        firebaseChatHandler =
            FirebaseChatHandler(this, CommonKeys.FirebaseChatServiceTriggeredFrom.chatActivity)
        rv.visibility = View.GONE
        noChats.visibility = View.VISIBLE
        rv.addOnLayoutChangeListener(View.OnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                rv.postDelayed(
                    Runnable { rv.scrollToPosition(adapterFirebaseRecylcerview.itemCount - 1) },
                    100
                )
            }
        })
    }

    override fun onPause() {

        firebaseChatHandler.unRegister()
        /*if (!TextUtils.isEmpty(sessionManager.tripId) && sessionManager.isDriverAndRiderAbleToChat) {
            startFirebaseChatListenerService(this)
        }*/

        super.onPause()
    }

    override fun onSuccess(jsonResp: JsonResponse?, data: String?) {

    }

    override fun onFailure(jsonResp: JsonResponse?, data: String?) {

    }

    companion object {
        var isOnChat = false
    }

    private fun updateRiderProfileOnHeader() {
        val riderProfilePic = sessionManager.riderProfilePic
        val riderName = sessionManager.riderName
        //val riderRating = sessionManager.riderRating
        if (!TextUtils.isEmpty(riderProfilePic)) {
            Picasso.get().load(sessionManager.riderProfilePic).error(R.drawable.car)
                .into(riderProfileImageView)
        }

        if (!TextUtils.isEmpty(riderName)) {
            profileName.text = riderName
        } else {
            profileName.text = resources.getString(R.string.rider)
        }


        try {
            if (!sessionManager.riderRating.isNullOrEmpty() && sessionManager.riderRating?.toFloat()!! > 0) {
                profileRating.visibility = View.VISIBLE
                profileRating.text = sessionManager.riderRating
            } else {
                profileRating.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            profileRating.visibility = View.GONE
        }
    }
}
