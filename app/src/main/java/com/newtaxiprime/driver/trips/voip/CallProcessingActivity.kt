package com.newtaxiprime.driver.trips.voip


import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.newtaxiprime.driver.R
import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.helper.CustomDialog
import com.newtaxiprime.driver.common.helper.NetworkChangeReceiver
import com.newtaxiprime.driver.common.model.JsonResponse
import com.newtaxiprime.driver.common.network.AppController
import com.newtaxiprime.driver.common.util.CommonKeys
import com.newtaxiprime.driver.common.util.CommonKeys.KEY_CALLER_ID
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.CommonMethods.Companion.DebuggableLogD
import com.newtaxiprime.driver.common.util.CommonMethods.Companion.playVibration
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.util.RuntimePermissionDialogFragment
import com.newtaxiprime.driver.common.views.CommonActivity
import com.newtaxiprime.driver.databinding.ActivityCallingProcessingBinding
import com.newtaxiprime.driver.home.interfaces.ApiService
import com.newtaxiprime.driver.home.interfaces.ServiceListener
import com.newtaxiprime.driver.home.pushnotification.Config
import com.newtaxiprime.driver.trips.voip.NewTaxiSinchService.Companion.call
import com.newtaxiprime.driver.trips.voip.NewTaxiSinchService.Companion.sinchClient
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import javax.inject.Inject


class CallProcessingActivity : CommonActivity(),
    RuntimePermissionDialogFragment.RuntimePermissionRequestedCallback, SensorEventListener,
    ServiceListener {

    internal lateinit var callAudio: AudioManager

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    lateinit var dialog: AlertDialog

    internal lateinit var clCallAnsweredView: ConstraintLayout

    internal lateinit var clCallRingingView: ConstraintLayout

    internal lateinit var onGoingCallTimerChronometer: Chronometer

    internal lateinit var callConnectionStatus: TextView

    internal lateinit var imgvLoudSpeaker: ImageView

    internal lateinit var imgvMic: ImageView

    internal lateinit var profileImage: CircleImageView

    internal lateinit var tvCallerName: TextView

    lateinit var binding: ActivityCallingProcessingBinding

    internal var mNetworkReceiver: NetworkChangeReceiver? = null
    internal lateinit var defaultRingtone: Uri
    internal var callRingtone: Ringtone? = null
    var outGointRingtoneMediaplayer: MediaPlayer? = null
    var callConnectingSound: MediaPlayer? = null
    private var isRegisterReceiver = false
    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null


    private lateinit var mPowerManager: PowerManager
    private lateinit var mWakeLock: PowerManager.WakeLock

    override fun permissionGranted(
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {
        if (requestCodeForCallbackIdentificationCodeSubDivision == 1) {
            DebuggableLogD("calling user id", intent.getStringExtra(KEY_CALLER_ID))
            call = sinchClient?.callClient?.callUser(intent.getStringExtra(KEY_CALLER_ID))!!
            try {
                initCallListener()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            try {
                call?.answer()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


    }

    override fun permissionDenied(
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {
        finishThisActivity()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            runOnUiThread {
                if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                    //near
                    turnOffScreen()
                } else {
                    //far
                    turnOnScreen()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            commonMethods.hideProgressDialog()
            onSuccessProfile(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    private fun onSuccessProfile(jsonResp: JsonResponse) {
        val profileURL = jsonResp.strResponse.let {
            commonMethods.getJsonValue(
                it,
                "profile_image",
                String::class.java
            )
        } as String
        val firstName = commonMethods.getJsonValue(
            jsonResp.strResponse,
            "first_name",
            String::class.java
        ) as String
        val lastName = commonMethods.getJsonValue(
            jsonResp.strResponse,
            "last_name",
            String::class.java
        ) as String
        Picasso.get().load(profileURL).into(profileImage)
        tvCallerName.text = firstName + "\t" + lastName

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

    }

    @IntDef(CallActivityType.Ringing, CallActivityType.CallProcessing)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class CallActivityType {
        companion object {
            const val Ringing = 0
            const val CallProcessing = 1
        }

    }


    fun answerACall() {

        RuntimePermissionDialogFragment.checkPermissionStatus(
            this,
            supportFragmentManager,
            this,
            arrayOf(
                RuntimePermissionDialogFragment.RECORD_AUDIO_PERMISSION,
                RuntimePermissionDialogFragment.MODIFY_AUDIO_PERMISSION
            ),
            RuntimePermissionDialogFragment.audioCallbackCode,
            0
        )

        stopCallRingtone()


    }


    fun doLoudSpeakerfunctionality() {
        if (callAudio.isSpeakerphoneOn) {
            callAudio.isSpeakerphoneOn = false
            imgvLoudSpeaker.isEnabled = false
        } else {
            imgvLoudSpeaker.isEnabled = true
            callAudio.isSpeakerphoneOn = true

        }

    }

    fun doVoiceMutefunctionality() {
        if (callAudio.isMicrophoneMute) {
            callAudio.isMicrophoneMute = false
            imgvMic.isEnabled = true
        } else {
            imgvMic.isEnabled = false
            callAudio.isMicrophoneMute = true
        }

    }


    fun cutTheCall() {
        if (callRingtone != null) {
            callRingtone?.stop()
        }

        call?.hangup()

        finishThisActivity()

    }

    fun finishThisActivity() {

        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallingProcessingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        clCallAnsweredView = binding.clAnswerView
        clCallRingingView = binding.clIncommingView
        onGoingCallTimerChronometer = binding.chronometerCallTimer
        callConnectionStatus = binding.tvCallConnectionStatus
        imgvLoudSpeaker = binding.imgvLoudSpeaker
        imgvMic = binding.imgvMuteVoice
        profileImage = binding.profileImage
        tvCallerName = binding.tvCallerName

         binding.llMic.setOnClickListener{
             doVoiceMutefunctionality()
         }

        binding.llLoudspeaker.setOnClickListener{
            doLoudSpeakerfunctionality()
        }

        binding.fabDismiss.setOnClickListener{
            cutTheCall()
        }

        binding.fabEndCall.setOnClickListener{
            cutTheCall()
        }

        binding.fabAnswer.setOnClickListener{
            answerACall()
        }

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        callAudio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        /*mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);*/

        dialog = commonMethods.getAlertDialog(this)
        CommonKeys.keyCaller = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        +WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        +WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }



        initInternetBroadCastProcess()
        initView()
    }

    private fun initView() {
        val incommingPageType = intent.getIntExtra(CommonKeys.KEY_TYPE, CallActivityType.Ringing)


        if (incommingPageType == CallActivityType.Ringing) {
            call?.remoteUserId?.let { callApiToGetCallerDetail(it) }
            showRingingView()
            playIncommingCallRingtone()
            initCallListener()


        } else {
            callApiToGetCallerDetail(intent.getStringExtra(KEY_CALLER_ID).toString())
            playOutgoingRingtone()
            callToAppropreatePerson()
            showAnsweredView()
        }


    }

    private fun callApiToGetCallerDetail(userID: String) {
        apiService.getCallerDetail(sessionManager.accessToken!!, userID, "1")
            .enqueue(RequestCallback(this))
    }

    private fun callToAppropreatePerson() {
        RuntimePermissionDialogFragment.checkPermissionStatus(
            this,
            supportFragmentManager,
            this,
            arrayOf(
                RuntimePermissionDialogFragment.RECORD_AUDIO_PERMISSION,
                RuntimePermissionDialogFragment.MODIFY_AUDIO_PERMISSION
            ),
            RuntimePermissionDialogFragment.audioCallbackCode,
            1
        )

    }

    private fun playOutgoingRingtone() {

        callConnectingSound =
            MediaPlayer.create(this@CallProcessingActivity, R.raw.outgoint_call_connection)
        //outGointRingtoneMediaplayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        callConnectingSound?.isLooping = true
        callConnectingSound?.start()

    }

    fun stopCallConnectingSound() {
        if (callConnectingSound != null) {
            callConnectingSound?.release()
        }
    }

    fun stopCallRingtone() {
        if (callRingtone != null) {
            callRingtone?.stop()
        }
    }

    private fun initCallListener() {
        call?.addCallListener(SinchCallListener())
    }

    private fun playIncommingCallRingtone() {
        defaultRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        callRingtone = RingtoneManager.getRingtone(applicationContext, defaultRingtone)
        callRingtone?.play()
    }

    fun showRingingView() {
        clCallRingingView.visibility = View.VISIBLE
        clCallAnsweredView.visibility = View.GONE
    }

    fun showAnsweredView() {
        clCallAnsweredView.visibility = View.VISIBLE
        clCallRingingView.visibility = View.GONE
        imgvLoudSpeaker.isEnabled = false
        imgvMic.isEnabled = true
    }


    private inner class SinchCallListener : CallListener {
        override fun onCallEnded(endedCall: Call) {
            if (outGointRingtoneMediaplayer != null) {
                outGointRingtoneMediaplayer?.release()
            }
            // sinchClient?.stopListeningOnActiveConnection();
            stopCallConnectingSound()
            volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
            callAudio.isSpeakerphoneOn = false
            callAudio.isMicrophoneMute = false

            finishThisActivity()

        }

        override fun onCallEstablished(establishedCall: Call) {

            volumeControlStream = AudioManager.STREAM_VOICE_CALL
            playVibration()

            if (outGointRingtoneMediaplayer != null) {
                outGointRingtoneMediaplayer?.release()
            }

            showAnsweredView()
            callConnectionStatus.text = resources.getString(R.string.connected)
            runTimerForOnGoingCall()

        }

        override fun onCallProgressing(progressingCall: Call) {

            outGointRingtoneMediaplayer =
                MediaPlayer.create(this@CallProcessingActivity, R.raw.outgoing_ringtone)
            //outGointRingtoneMediaplayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            outGointRingtoneMediaplayer?.isLooping = true
            outGointRingtoneMediaplayer?.start()
            stopCallConnectingSound()
            callConnectionStatus.text = resources.getString(R.string.ringing)

        }

        fun onShouldSendPushNotification(call: Call, pushPairs: List<PushPair>) {
            //don't worry about this right now


            /*NotificationResult result = sinchClient.relayRemotePushNotificationPayload(payload);
// handle result, e.g. show a notification for a missed call:
            if (result.isValid() && result.isCall()) {
                CallNotificationResult callResult = result.getCallResult();
                if (callResult.isCallCanceled()) {
                    // user-defined method to show notification
                    createNotification(callResult.getRemoteUserId());
                }
            }*/
        }
    }

    fun runTimerForOnGoingCall() {
        onGoingCallTimerChronometer.visibility = View.VISIBLE
        onGoingCallTimerChronometer.format = " %s"
        onGoingCallTimerChronometer.base = SystemClock.elapsedRealtime()
        onGoingCallTimerChronometer.start()
    }

    override fun onPause() {
        super.onPause()

        stopCallRingtone()
        stopCallConnectingSound()
    }

    override fun onStop() {

        super.onStop()


    }

    override fun onDestroy() {
        super.onDestroy()
        isOnCall = false
        if (callRingtone != null) {
            callRingtone?.stop()
        }

        call?.hangup()

        finishThisActivity()
        unregisterNetworkChanges()


    }

    override fun onResume() {
        super.onResume()
        //mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        isOnCall = true

    }

    fun turnOnScreen() {
        // turn on screen
        Log.v("ProximityActivity", "ON!")
        mPowerManager = this.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = mPowerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "callTag:myWaketag"
        )
        mWakeLock.acquire()
    }

    @TargetApi(21) //Suppress lint error for PROXIMITY_SCREEN_OFF_WAKE_LOCK
    fun turnOffScreen() {
        // turn off screen
        Log.v("ProximityActivity", "OFF!")
        mPowerManager = this.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = mPowerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "callTag:myWaketag"
        )
        mWakeLock.acquire()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //super.onBackPressed();
    }


    private fun initInternetBroadCastProcess() {

        mRegistrationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                println("onReceiveBroadCast:${intent.action}")
                // checking for type intent filter
                if (intent.action == Config.NETWORK_CHANGES && !commonMethods.isOnline(this@CallProcessingActivity)) {
                    finishThisActivity()

                }
            }
        }
        registerNetworkBroadcastForNougat()
    }

    private fun registerNetworkBroadcastForNougat() {

        mNetworkReceiver = NetworkChangeReceiver()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
            // registerReceiver(mRegistrationBroadcastReceiver, IntentFilter(Config.NETWORK_CHANGES))
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mRegistrationBroadcastReceiver!!,
            IntentFilter(Config.NETWORK_CHANGES)
        )


    }

    protected fun unregisterNetworkChanges() {


        try {

            if (mNetworkReceiver != null) {
                unregisterReceiver(mNetworkReceiver)
            }
            if (mRegistrationBroadcastReceiver != null) {
                unregisterReceiver(mRegistrationBroadcastReceiver)
            }

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } finally {
            mRegistrationBroadcastReceiver = null
            mNetworkReceiver = null
        }

    }

    companion object {
        var isOnCall = false

        /*private SensorManager mSensorManager;
    private Sensor mProximity;*/
        private val SENSOR_SENSITIVITY = 4
    }
}
