package com.a0100019.mypat.presentation.main.management

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.a0100019.mypat.trash.AppBgmManager
import com.a0100019.mypat.presentation.activity.store.BillingManager
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        InterstitialAdManager.loadAd(this)

        // ✅ 시스템 윈도우 영역 사용 (상단 상태바 보이게)
//        WindowCompat.setDecorFitsSystemWindows(window, true)

        // ✅ 뒤로가기 버튼 클릭 시 앱을 완전히 종료하는 콜백
        onBackPressedDispatcher.addCallback(this) {
            // 모든 액티비티를 종료하고 프로세스를 완전히 죽입니다.
            finishAffinity()
            // 선택사항: 시스템적으로 프로세스까지 즉시 종료하고 싶을 때 사용
            // android.os.Process.killProcess(android.os.Process.myPid())
        }

        // 🔹 SharedPreferences에서 배경음악 설정 불러오기
        val prefs = getSharedPreferences("bgm_prefs", Context.MODE_PRIVATE)
        val bgm = prefs.getString("bgm", "area/normal.webp")
        val bgmOn = prefs.getBoolean("bgmOn", true)

        // 🔹 앱 전역 배경음악 초기화
        AppBgmManager.init(
            context = this,
            name = bgm!!,
            loop = true,
            volume = 0.1f
        )
        AppBgmManager.pause()

        setContent {
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp
            val screenHeight = configuration.screenHeightDp
            val aspectRatio = screenWidth.toFloat() / screenHeight.toFloat()
            val minRatio = 9f / 22f
            val maxRatio = 9f / 17f

            MypatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), // 1. 패딩을 여기서 제거 (배경이 끝까지 차도록)
                    color = Color.Black
                    , // ✅ 상태바와 겹치지 않도록 패딩
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(), // 3. 실제 콘텐츠에만 상태바 패딩 적용
                        contentAlignment = Alignment.Center
                    ) {
                        // 검은 배경
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                        )

                        // 화면 비율 조정
                        val contentModifier = when {
                            aspectRatio < minRatio -> Modifier
                                .fillMaxHeight()
                                .aspectRatio(minRatio)
                                .border(2.dp, Color.Red)
                                .shadow(8.dp, RectangleShape, clip = false)
                            aspectRatio > maxRatio -> Modifier
                                .fillMaxHeight()
                                .aspectRatio(maxRatio)
                                .border(1.dp, Color.Black)
                                .shadow(8.dp, RectangleShape, clip = false)
                            else -> Modifier.fillMaxSize()
                        }

                        Column {
                            // --------------------------------------------------
                            // [추가] 최상단 광고 배치
                            // --------------------------------------------------
                            // 1. SharedPreferences 정의
                            val adPrefs = getSharedPreferences("ad_prefs", Context.MODE_PRIVATE)
                            val banner = adPrefs.getString("banner", "0")

//                            if(banner == "1") {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(50.dp)
//                                        .background(Color(0xFFBCE8E3))
//                                ) {
//                                    BannerAd()
//                                }
//                            } else {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(20.dp)
//                                ) {
//                                }
//                            }

                            Box(modifier = contentModifier) {
                                MainNavHost(
                                    billingManager = billingManager
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showStatusBarHideNavBar() // 🔹 상태바는 보이고, 네비게이션 바 숨김

        val prefs = getSharedPreferences("bgm_prefs", Context.MODE_PRIVATE)
        val bgmOn = prefs.getBoolean("bgmOn", true)
//        if (bgmOn) AppBgmManager.play()
    }

    override fun onStop() {
        super.onStop()
        AppBgmManager.pause()
    }

    // 상태바는 보이고, 네비게이션 바만 숨기기
    private fun showStatusBarHideNavBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        // 상태바 보이기
        controller.show(WindowInsetsCompat.Type.statusBars())

        // 네비게이션 바 숨기기
        controller.hide(WindowInsetsCompat.Type.navigationBars())

        // 스와이프하면 잠깐 네비게이션 바 나타나게
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun hideSystemUI() {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            show(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }
}
