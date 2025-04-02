package com.yourfitness.coach

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.yourfitness.coach.data.YFCDatabase
import com.yourfitness.coach.databinding.ActivityMainBinding
import com.yourfitness.coach.domain.fb_remote_config.FirebaseRemoteConfigRepository
import com.yourfitness.coach.domain.fcm.CloudMessagingRepository
import com.yourfitness.coach.domain.session.SessionManager
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.NavigationHandler
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.coach.ui.utils.transparentStatusAndNavigation
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.data.CommonDatabase
import com.yourfitness.common.data.TokenStorage
import com.yourfitness.common.domain.session.TokenManager
import com.yourfitness.common.ui.navigation.CommonNavigation
import com.yourfitness.common.ui.navigation.CommonNavigationHandler
import com.yourfitness.common.ui.navigation.CommonNavigator
import com.yourfitness.community.data.CommunityDatabase
import com.yourfitness.community.data.CommunityStorage
import com.yourfitness.community.ui.navigation.CommunityNavigationHandler
import com.yourfitness.community.ui.navigation.CommunityNavigator
import com.yourfitness.pt.data.PtDatabase
import com.yourfitness.pt.data.PtStorage
import com.yourfitness.pt.ui.navigation.PtNavigationHandler
import com.yourfitness.pt.ui.navigation.PtNavigator
import com.yourfitness.shop.data.ShopDatabase
import com.yourfitness.shop.data.ShopStorage
import com.yourfitness.shop.ui.navigation.ShopNavigationHandler
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import android.net.Uri
import com.yourfitness.coach.domain.spike.deepLinkHost
import com.yourfitness.coach.domain.spike.deppLinkUserId
import com.yourfitness.coach.ui.features.profile.connected_devices.ConnectedDevicesFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    private lateinit var navigationHandler: NavigationHandler
    private lateinit var shopNavigationHandler: ShopNavigationHandler
    private lateinit var commonNavigationHandler: CommonNavigationHandler
    private lateinit var ptNavigationHandler: PtNavigationHandler
    private lateinit var communityNavigationHandler: CommunityNavigationHandler
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var shopNavigator: ShopNavigator
    @Inject lateinit var commonNavigator: CommonNavigator
    @Inject lateinit var ptNavigator: PtNavigator
    @Inject lateinit var communityNavigator: CommunityNavigator
    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var tokenStorage: TokenStorage
    @Inject lateinit var db: YFCDatabase
    @Inject lateinit var commonDb: CommonDatabase
    @Inject lateinit var shopDb: ShopDatabase
    @Inject lateinit var ptDb: PtDatabase
    @Inject lateinit var communityDb: CommunityDatabase
    @Inject lateinit var preferencesStorage: PreferencesStorage
    @Inject lateinit var commonStorage: CommonPreferencesStorage
    @Inject lateinit var shopStorage: ShopStorage
    @Inject lateinit var ptStorage: PtStorage
    @Inject lateinit var communityStorage: CommunityStorage
    @Inject lateinit var cloudMessagingRepository: CloudMessagingRepository

    @Inject lateinit var configRepository: FirebaseRemoteConfigRepository
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        setupView()
        setupActionBar()
        setupNavigation()
        setupCommonNavigation()
        setupShopNavigation()
        setupPtNavigation()
        setupCommunityNavigation()
        setupProgress()
        setupLogoutObserver()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
        navController.handleDeepLink(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.forEach { fragment ->
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleDeepLink(intent: Intent) {
        Timber.tag("DeepLink").d("Here")
        val action = intent.action
        val data = intent.data

        if (Intent.ACTION_VIEW == action && data != null && data.host == deepLinkHost) {
            val url = data.toString()
            val specificPath = data.path
            val userId = data.getQueryParameter(deppLinkUserId)
            Timber.tag("DeepLink").d("URL: ${url}, Path: $specificPath, UserId: $userId")
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?
            val connectedDevicesFragment =
                navHostFragment?.childFragmentManager?.fragments?.find { childFragment -> childFragment is ConnectedDevicesFragment } as ConnectedDevicesFragment?
            connectedDevicesFragment?.onSpikeApiInited(userId)
        }
    }

    private fun setupProgress() {
        binding.progress.root.isVisible = false
    }

    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupView() {
        window.transparentStatusAndNavigation()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupActionBar() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
//        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
    }

    private fun setupNavigation() {
        navigationHandler = NavigationHandler(navController, navigator)
        navigationHandler.observeNavigation(this)
    }

    private fun setupShopNavigation() {
        shopNavigationHandler = ShopNavigationHandler(navController, shopNavigator)
        shopNavigationHandler.observeNavigation(this)
    }

    private fun setupCommonNavigation() {
        commonNavigationHandler = CommonNavigationHandler(navController, commonNavigator, R.id.fragment_welcome_back)
        commonNavigationHandler.observeNavigation(this)
    }

    private fun setupPtNavigation() {
        ptNavigationHandler = PtNavigationHandler(navController, ptNavigator)
        ptNavigationHandler.observeNavigation(this)
    }

    private fun setupCommunityNavigation() {
        communityNavigationHandler = CommunityNavigationHandler(navController, communityNavigator)
        communityNavigationHandler.observeNavigation(this)
    }

    private fun setupLogoutObserver() {
        tokenManager.logoutState.observe(this) {
            if (!it) return@observe
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    sessionManager.logout()
                    Timber.tag("issue").e("${commonDb.profileDao().readProfile()?.phoneNumber}")
                    if (configRepository.needUpdate) {
                        navigator.navigate(Navigation.HardUpdate)
                    } else {
                        commonNavigator.navigate(CommonNavigation.WelcomeBack)
                    }
                    tokenManager.reset()
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (configRepository.needUpdate) {
                navigator.navigate(Navigation.HardUpdate)
            }
        }
    }
}
