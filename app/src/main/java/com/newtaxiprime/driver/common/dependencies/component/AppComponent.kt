package com.newtaxiprime.driver.common.dependencies.component

/**
 * @package com.newtaxiprime.driver
 * @subpackage dependencies.component
 * @category AppComponent
 * @author Seen Technologies
 *
 */

import com.newtaxiprime.driver.common.configs.SessionManager
import com.newtaxiprime.driver.common.database.AddFirebaseDatabase
import com.newtaxiprime.driver.common.dependencies.module.AppContainerModule
import com.newtaxiprime.driver.common.dependencies.module.ApplicationModule
import com.newtaxiprime.driver.common.dependencies.module.ImageCompressAsyncTask
import com.newtaxiprime.driver.common.dependencies.module.NetworkModule
import com.newtaxiprime.driver.common.helper.CarTypeAdapter
import com.newtaxiprime.driver.common.helper.CommonDialog
import com.newtaxiprime.driver.common.helper.RunTimePermission
import com.newtaxiprime.driver.common.util.CommonMethods
import com.newtaxiprime.driver.common.util.RequestCallback
import com.newtaxiprime.driver.common.util.userchoice.UserChoice
import com.newtaxiprime.driver.common.views.*
import com.newtaxiprime.driver.google.direction.GetDirectionData
import com.newtaxiprime.driver.google.locationmanager.*
import com.newtaxiprime.driver.home.MainActivity
import com.newtaxiprime.driver.home.facebookAccountKit.FacebookAccountKitActivity
import com.newtaxiprime.driver.home.firebaseChat.ActivityChat
import com.newtaxiprime.driver.home.firebaseChat.AdapterFirebaseRecylcerview
import com.newtaxiprime.driver.home.firebaseChat.FirebaseChatHandler
import com.newtaxiprime.driver.home.fragments.AccountFragment
import com.newtaxiprime.driver.home.fragments.EarningActivity
import com.newtaxiprime.driver.home.fragments.HomeFragment
import com.newtaxiprime.driver.home.fragments.RatingActivity
import com.newtaxiprime.driver.home.fragments.Referral.ShowReferralOptionsActivity
import com.newtaxiprime.driver.home.fragments.currency.CurrencyListAdapter
import com.newtaxiprime.driver.home.fragments.language.LanguageAdapter
import com.newtaxiprime.driver.home.fragments.payment.*
import com.newtaxiprime.driver.home.managevehicles.*
import com.newtaxiprime.driver.home.managevehicles.adapter.*
import com.newtaxiprime.driver.home.map.GpsService
import com.newtaxiprime.driver.home.map.drawpolyline.DownloadTask
import com.newtaxiprime.driver.home.paymentstatement.*
import com.newtaxiprime.driver.home.payouts.*
import com.newtaxiprime.driver.home.payouts.adapter.PayoutCountryListAdapter
import com.newtaxiprime.driver.home.payouts.payout_model_classed.PayPalEmailAdapter
import com.newtaxiprime.driver.home.profile.DriverProfile
import com.newtaxiprime.driver.home.profile.VehiclInformation
import com.newtaxiprime.driver.home.pushnotification.MyFirebaseInstanceIDService
import com.newtaxiprime.driver.home.pushnotification.MyFirebaseMessagingService
import com.newtaxiprime.driver.home.service.ForeService
import com.newtaxiprime.driver.home.service.LocationService
import com.newtaxiprime.driver.home.signinsignup.*
import com.newtaxiprime.driver.home.splash.SplashActivity
import com.newtaxiprime.driver.trips.*
import com.newtaxiprime.driver.trips.rating.*
import com.newtaxiprime.driver.trips.tripsdetails.*
import com.newtaxiprime.driver.trips.viewmodel.ReqAccpVM
import com.newtaxiprime.driver.trips.voip.CallProcessingActivity
import com.newtaxiprime.driver.trips.voip.NewTaxiSinchService
import com.newtaxiprime.driver.common.views.FlutterwaveWebViewActivity
import com.newtaxiprime.driver.home.profile.ProfileMenu
import com.newtaxiprime.driver.home.search.PlaceSearchActivity
import dagger.Component
import javax.inject.Singleton


/*****************************************************************
 * App Component
 */
@Singleton
@Component(modules = [NetworkModule::class, ApplicationModule::class, AppContainerModule::class])
interface AppComponent {
    // ACTIVITY

    fun inject(bankDetailsActivity: BankDetailsActivity)


    fun inject(currencyListAdapter: CurrencyListAdapter)

    fun inject(payoutEmailActivity: PayoutEmailActivity)

    fun inject(payoutEmailListActivity: PayoutEmailListActivity)


    fun inject(payPalEmailAdapter: PayPalEmailAdapter)

    fun inject(payoutAddressDetailsActivity: PayoutAddressDetailsActivity)

    fun inject(payoutBankDetailsActivity: PayoutBankDetailsActivity)

    fun inject(payoutCoutryListAdapter2: PayoutCoutryListAdapter2)

    fun inject(priceStatementAdapter: PriceStatementAdapter)

    fun inject(paymentPage: PaymentPage)

    fun inject(driverDetailsAdapter: DriverDetailsAdapter)

    fun inject(sessionManager: SessionManager)

    fun inject(pendingTripsFragment: PendingTripsFragment)

    fun inject(accountFragment: AccountFragment)

    fun inject(viewDocumentFragment: ViewVehicleDocumentFragment)

    fun inject(homeFragment: HomeFragment)

    fun inject(past: CompletedTripsFragments)

    fun inject(ratingFragment: RatingActivity)

    fun inject(comments: Comments)

    fun inject(yourTrips: YourTrips)

    fun inject(carTypeAdapter: CarTypeAdapter)

    fun inject(tripDetails: TripDetails)

    fun inject(PaymentStatementActivity: PaymentStatementActivity)


    fun inject(ManageVehicleActivity: ManageVehicleFragment)
    fun inject(vehicleTypeAdapter: VehicleTypeAdapter)

    fun inject(earningFragment: EarningActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(signinSignupHomeActivity: SigninSignupHomeActivity)

    fun inject(splashActivity: SplashActivity)

    fun inject(addPayment: AddPayment)

    fun inject(riderProfilePage: RiderProfilePage)

    fun inject(setting_Activity: SettingActivity)

    fun inject(requestReceiveActivity: RequestReceiveActivity)

    fun inject(manageDriverDocFrag: ManageDriverDocumentFragment)

    fun inject(viewVehicleDocFrag: ViewDriverDocumentFragment)

    fun inject(requestAcceptActivity: RequestAcceptActivity)

    fun inject(riderContactActivity: RiderContactActivity)

    fun inject(cancelYourTripActivity: CancelYourTripActivity)

    fun inject(documentDetails: DocumentDetails)

    fun inject(paymentAmountPage: PaymentAmountPage)

    fun inject(payStatementDetails: PayStatementDetails)

    fun inject(tripEarningsDetail: TripEarningsDetail)

    fun inject(dailyEarningDetails: DailyEarningDetails)

    fun inject(riderrating: Riderrating)

    fun inject(gps_service: GpsService)

    fun inject(registerCarDetailsActivity: RegisterCarDetailsActivity)

    fun inject(resetPassword: ResetPassword)

    fun inject(register: Register)

    fun inject(registerOTPActivity: RegisterOTPActivity)

    fun inject(commonMethods: CommonMethods)

    fun inject(MobileActivity: MobileActivity)

    fun inject(signinActivity: SigninActivity)

    fun inject(requestCallback: RequestCallback)

    fun inject(runTimePermission: RunTimePermission)

    fun inject(driverProfile: DriverProfile)

    fun inject(profileMenu: ProfileMenu)

    fun inject(placeSearchActivity: PlaceSearchActivity)

    fun inject(addWalletActivity: AddWalletActivity)

    fun inject(transferMoney: TransferMoney)

    fun inject(vehiclInformation: VehiclInformation)

    fun inject(riderFeedBack: RiderFeedBack)

    fun inject(activityChat: ActivityChat)

    fun inject(facebookAccountKitActivity: FacebookAccountKitActivity)

    // Adapters
    fun inject(manageVehicleAdapter: ManageVehicleAdapter)

    fun inject(yearAdapter: YearAdapter)

    fun inject(payoutDetailsListAdapter: PayoutDetailsListAdapter)

    fun inject(languageAdapter: LanguageAdapter)

    fun inject(addVehicle: AddVehicleFragment)

    fun inject(manageDocumentsAdapter: ManageDocumentsAdapter)

    fun inject(myFirebaseMessagingService: MyFirebaseMessagingService)

    fun inject(myFirebaseInstanceIDService: MyFirebaseInstanceIDService)

    fun inject(imageCompressAsyncTask: ImageCompressAsyncTask)

    fun inject(firebaseChatHandler: FirebaseChatHandler)

    fun inject(payoutCountryListAdapter: PayoutCountryListAdapter)

    fun inject(adapterFirebaseRecylcerview: AdapterFirebaseRecylcerview)

    fun inject(makeAdapter: MakeAdapter)

    fun inject(modelAdapter: ModelAdapter)

    fun inject(reqAccpVM: ReqAccpVM)

    //    service

    fun inject(downloadTask: DownloadTask)

    fun inject(foreService: ForeService)


    fun inject(newTaxiSinchService: NewTaxiSinchService)

    //fun inject(workerUtils: WorkerUtils)


    //fun inject(updateGPSWorker: UpdateGPSWorker)


    fun inject(locationService: LocationService)

    fun inject(firebaseDatabase: AddFirebaseDatabase)

    fun inject(payToAdminActivity: PayToAdminActivity)

   // fun inject(paytmPaymentWebViewActivity: PaytmPaymentWebViewActivity)

   // fun inject(mpesaPaymentWebViewActivity: MpesaPaymentWebViewActivity)

    fun inject(flutterwaveWebViewActivity: FlutterwaveWebViewActivity)

   // fun inject(razorpayWebViewActivity: RazorpayWebViewActivity)

   // fun inject(mollieWebViewActivity: MollieWebViewActivity)

    fun inject(paystackWebViewActivity: PaystackWebViewActivity)

    fun inject(manageVehicles: ManageVehicles)

    fun inject(paymentActivity: PaymentActivity)

    fun inject(addCardActivity: AddCardActivity)

    fun inject(payoutDetailsListActivity: PayoutDetailsListActivity)

    fun inject(manageDocumentActivity: ManageVehicleDocumentFragment)

    fun inject(callProcessingActivity: CallProcessingActivity)

    fun inject(priceRecycleAdapter: PriceRecycleAdapter)

    fun inject(showReferralOptionsActivity: ShowReferralOptionsActivity)

    fun inject(upcomingTripsPaginationAdapter: PendingTripsPaginationAdapter)

    fun inject(pastTripsPaginationAdapter: CompletedTripsPaginationAdapter)
    fun inject(paymentMethodAdapter: PaymentMethodAdapter)


    fun inject(featuresInVehicleAdapter: FeaturesInVehicleAdapter)


    /**
     * Live Tracking Injects
     */
    fun inject(getDirectionData: GetDirectionData)

    fun inject(updateLocations: UpdateLocations)

    fun inject(trackingServiceListener: TrackingServiceListener)

    fun inject(trackingService: TrackingService)

    fun inject(androidPositionProvider: AndroidPositionProvider)

    fun inject(trackingController: TrackingController)

    fun inject(supportActivityCommon: SupportActivityCommon)

    fun inject(supportAdapter: SupportAdapter)

    fun inject(payStatementPaginationAdapter: PayStatementPaginationAdapter)

    fun inject(dailyEarnPaginationAdapter: DailyEarnPaginationAdapter)

    fun inject(dailyHoursPaginationAdapter: DailyHoursPaginationAdapter)

    fun inject(dailyEarnListAdapter: DailyEarnListAdapter)

    fun inject(paymentWebViewActivity: PaymentWebViewActivity)

    fun inject(commonActivity: CommonActivity)

    fun inject(commonDialog: CommonDialog)

    fun inject(commentsPaginationAdapter: CommentsPaginationAdapter)

    fun inject(userChoice: UserChoice)

    fun inject(commentsRecycleAdapter: CommentsRecycleAdapter)

    //fun inject(applicationContext: Context)

}
