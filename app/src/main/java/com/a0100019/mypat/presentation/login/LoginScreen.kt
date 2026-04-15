@file:Suppress("LABEL_NAME_CLASH")

package com.a0100019.mypat.presentation.login

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.a0100019.mypat.R
import com.a0100019.mypat.presentation.main.management.MainRoute
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.a0100019.mypat.presentation.setting.TermsDialog
import com.a0100019.mypat.presentation.ui.MusicPlayer
import com.a0100019.mypat.presentation.ui.SfxPlayer
import com.a0100019.mypat.presentation.ui.component.MainButton

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navController: NavController // NavController를 파라미터로 받아오기
) {
    val context = LocalContext.current

    // 여기서 네비게이션 처리
    loginViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is LoginSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            is LoginSideEffect.NavigateToMainScreen -> {
                navController.navigate(MainRoute.MainScreen.name) {
                    popUpTo(MainRoute.LoginScreen.name) { inclusive = true } // 뒤로가기 방지
                }
            }
            is LoginSideEffect.NavigateToDiaryScreen -> {
                navController.navigate(MainRoute.DiaryScreen.name) {
                    popUpTo(MainRoute.LoginScreen.name) { inclusive = true } // 뒤로가기 방지
                }
            }
            is LoginSideEffect.NavigateToFirstScreen -> {
                navController.navigate(MainRoute.FirstScreen.name) {
                    popUpTo(MainRoute.LoginScreen.name) { inclusive = true } // 뒤로가기 방지
                }
            }
        }
    }

    val loginState = loginViewModel.collectAsState().value

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                loginViewModel.onGoogleLoginClick(idToken)
            } else {
                Log.e("login", "로그인 스크린 로그인 성공")
//                LoginSideEffect.Toast(postLoginSideEffect.Toast("구글 로그인 실패: 토큰 없음"))
            }
        } catch (e: Exception) {
            Log.e("login", "로그인 스크린 로그인 실패: ${e.localizedMessage}", e)
//            loginViewModel.postSideEffect(LoginSideEffect.Toast("구글 로그인 실패"))
        }
    }

    LoginScreen(
        loginState = loginState.loginState,
        dialog = loginState.dialog,
        downloadPhotoCount = loginState.downloadPhotoCount,

        onNavigateToMainScreen = loginViewModel::onNavigateToMainScreen,
        dialogChange = loginViewModel::dialogChange,
        reLoading = loginViewModel::reLoading,
        onGuestLoginClick = loginViewModel::onGuestLoginClick,
        onNavigateToDiaryScreen = loginViewModel::onNavigateToDiaryScreen,
        onNavigateToFirstScreen = loginViewModel::onNavigateToFirstScreen,
        todayAttendance = loginViewModel::todayAttendance,

        googleLoginClick = {
            if (!isInternetAvailable(context)) {
                Toast.makeText(context, "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                return@LoginScreen
            }

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            // 자동 로그인 방지: 로그아웃 후 다시 실행
            googleSignInClient.signOut().addOnCompleteListener {
                launcher.launch(googleSignInClient.signInIntent)
            }
        }
        ,
    )
}

//인터넷 연결 확인 코드
fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun LoginScreen(
    googleLoginClick: () -> Unit,
    onNavigateToMainScreen: () -> Unit,
    onNavigateToFirstScreen: () -> Unit = {},
    dialogChange: (String) -> Unit = {},
    reLoading: () -> Unit = {},
    onGuestLoginClick: () -> Unit = {},
    onNavigateToDiaryScreen: () -> Unit = {},
    todayAttendance: () -> Unit = {},

    loginState: String = "loading",
    dialog: String = "",
    downloadPhotoCount: Int = 0,
) {

    // 상태를 remember로 관리해야 UI가 갱신됨
    var termsChecked by remember { mutableStateOf(true) }

    val context = LocalContext.current

    when (dialog) {
        "loginWarning" -> {
            LoginWarningDialog(
                onClose = { dialogChange("") },
                onConfirmClick = { dialogChange("check") }
            )
        }
        "terms" -> {
            TermsDialog(
                onClose = { dialogChange("") }
            )
        }
        "explanation" -> {
            LoginTutorialDialog(
                onClose = {
                    todayAttendance()
                    onNavigateToFirstScreen()
                }
            )
        }

    }

    Box {

        JustImage(
            filePath = "etc/background.webp",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        JustImage(
            filePath = "etc/sparkle.json",
            modifier = Modifier
                .padding(start = 30.dp, top = 30.dp)
                .size(50.dp)
            ,
        )

        JustImage(
            filePath = "etc/sparkle_pink.json",
            modifier = Modifier
                .padding(start = 130.dp, top = 30.dp)
                .size(140.dp)
            ,
        )

        JustImage(
            filePath = "etc/sparkle.json",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 20.dp, top = 80.dp)
                .size(70.dp)
            ,
        )

        JustImage(
            filePath = "etc/sparkle_pink.json",
            modifier = Modifier
                .padding(start = 70.dp, top = 100.dp)
                .size(80.dp)
            ,
        )

        JustImage(
            filePath = "etc/sparkle.json",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 70.dp, top = 130.dp)
                .size(120.dp)
            ,
        )

        JustImage(
            filePath = "etc/sparkle_pink.json",
            modifier = Modifier
                .padding(start = 120.dp, top = 170.dp)
                .size(60.dp)
            ,
        )

        JustImage(
            filePath = "etc/sparkle.json",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 140.dp, top = 220.dp)
                .size(40.dp)
            ,
        )

        JustImage(
            filePath = "etc/sparkle_pink.json",
            modifier = Modifier
                .padding(start = 40.dp, top = 240.dp)
                .size(80.dp)
            ,
        )

        JustImage(
            filePath = "etc/sparkle.json",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 40.dp, top = 260.dp)
                .size(80.dp)
            ,
        )


        val isPreview = LocalInspectionMode.current // 프리뷰 감지

        // 폰트 설정
        val customFont = FontFamily(Font(R.font.fish))
        val safeFont = if (isPreview) FontFamily.SansSerif else customFont

        // 애니메이션 정의
        val infiniteTransition = rememberInfiniteTransition(label = "title_animation")

        // 1. 빛의 강도 (투명도 조절용 - 0.0 ~ 1.0 사이로 안전하게 설정)
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "glowAlpha"
        )

        // 2. 글자가 위아래로 둥실둥실 떠 있는 애니메이션
        val floatOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -12f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearEasing), // 부드러운 사인파 곡선 느낌
                repeatMode = RepeatMode.Reverse
            ), label = "floatOffset"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.Center)
                .padding(bottom = 100.dp)
            ,
            contentAlignment = Alignment.Center
        ) {
            // [Layer 1] 가장 아래쪽 깊은 그림자 (바닥 고정)
            Text(
                text = "하루마을",
                fontSize = 77.sp,
                fontFamily = safeFont,
                // alpha 값을 .coerceIn(0f, 1f)로 감싸서 에러 방지
                color = Color(0xFF2F6F62).copy(alpha = 0.15f.coerceIn(0f, 1f)),
                modifier = Modifier.offset(y = 10.dp)
            )

            // [Layer 2] 움직이는 본체 그룹
            Box(modifier = Modifier.offset(y = floatOffset.dp)) {

                // 외곽선 효과 (Stroke)
                Text(
                    text = "하루마을",
                    fontSize = 75.sp,
                    fontFamily = safeFont,
                    style = TextStyle(
                        drawStyle = Stroke(
                            width = 10f,
                            join = StrokeJoin.Round
                        ),
                        color = Color(0xFF5AA48F) // 짙은 민트 테두리
                    )
                )

                // 메인 텍스트 (그라데이션 본체)
                Text(
                    text = "하루마을",
                    fontSize = 75.sp,
                    fontFamily = safeFont,
                    style = TextStyle(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFE0F7F0)
                            )
                        ),
                        shadow = Shadow(
                            // 에러 방지를 위해 최종 alpha 값에 coerceIn 적용
                            color = Color(0xFF9FE8CC).copy(alpha = (glowAlpha * 0.6f).coerceIn(0f, 1f)),
                            offset = Offset(0f, 4f),
                            blurRadius = 25f * glowAlpha
                        )
                    )
                )

                // [Layer 3] 상단 화이트 하이라이트 (더 반짝이는 느낌)
                Text(
                    text = "하루마을",
                    fontSize = 75.sp,
                    fontFamily = safeFont,
                    style = TextStyle(
                        color = Color.Transparent,
                        shadow = Shadow(
                            color = Color.White.copy(alpha = glowAlpha.coerceIn(0f, 1f)),
                            offset = Offset(0f, -2f),
                            blurRadius = 15f * glowAlpha
                        )
                    )
                )
            }
        }

        when (loginState) {
            "unLogin" -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

//                TextFlash(
//                    text = "하루마을에 오신 것을 환영합니다!",
//                )

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = onGuestLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFFF6C1CC), // 핑크 계열 그림자
                            spotColor = Color(0xFFF6C1CC)
                        )
                        .background(
                            color = Color(0xFFFFF1F4), // 🌸 파스텔 핑크 배경
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF8A4A5C) // 톤다운된 장밋빛 텍스트
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFB56A7A) // 아이콘도 파스텔 톤
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "나중에 로그인할게요",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.3).sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.size(20.dp))

                // 구글 로그인 버튼
                Button(
                    onClick = {
                        if(termsChecked) { googleLoginClick() }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .alpha(if (termsChecked) 1f else 0.7f) // 전체 투명도 (70% 불투명)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        JustImage(
                            filePath = "etc/googleLogo.png",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("구글 로그인")
                    }
                }

                Spacer(modifier = Modifier.size(20.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                    ,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f) // 50% 투명
                ) {

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column {
                                Text(
                                    text = "  이용약관 및 개인정보 처리방침에 동의합니다.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Checkbox(
                                checked = termsChecked,
                                onCheckedChange = { termsChecked = it }
                            )

                        }


                        Text(
                            text = "이용약관 및 개인정보 처리방침 보기",
                            modifier = Modifier.clickable {
                                dialogChange("terms")
                            },
                            color = Color.Black
                        )


                    }

                }

                Spacer(modifier = Modifier.size(20.dp))
            }

            "login" -> Column(
                modifier = Modifier
                    .fillMaxSize()
                        ,
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
// 1. 버튼 내부 상태 관리를 위한 변수들
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
// 버튼이 눌렸을 때 아래로 살짝 내려가는 효과
                val offsetY by animateFloatAsState(targetValue = if (isPressed) 4f else 0f, label = "offset")

//                Box(
//                    modifier = Modifier
//                        .graphicsLayer {
//                            scaleX = scale
//                            scaleY = scale
//                        }
//                        .padding(12.dp)
//                        .clickable(
//                            interactionSource = interactionSource,
//                            indication = null
//                        ) {
//                            onNavigateToMainScreen() // 클릭 이벤트
//                            SfxPlayer.play(context, R.raw.bubble)
//                        },
//                    contentAlignment = Alignment.Center
//                ) {
//                    // [그림자 레이어] 버튼 뒤에 깔리는 짙은 바닥
//                    Surface(
//                        modifier = Modifier
//                            .fillMaxWidth(0.8f) // 원하는 너비 조절
//                            .height(64.dp)
//                            .offset(y = 4.dp), // 살짝 아래로 배치해서 입체감 부여
//                        shape = RoundedCornerShape(20.dp),
//                        color = Color(0xFF2F6F62).copy(alpha = 0.5f) // 버튼보다 진한 색
//                    ) {}
//
//                    // [메인 버튼 레이어] 실제 보이는 버튼
//                    Surface(
//                        modifier = Modifier
//                            .fillMaxWidth(0.8f)
//                            .height(64.dp)
//                            .offset(y = offsetY.dp), // 누를 때 아래로 슥 내려감
//                        shape = RoundedCornerShape(20.dp),
//                        color = Color(0xFFEAF4F1), // 배경색
//                        border = BorderStroke(2.dp, Color(0xFF9ECFC3)) // 테두리
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.Center,
//                            modifier = Modifier.padding(horizontal = 24.dp)
//                        ) {
//                            // 아이콘 (원하면 추가)
//                            Text(text = "🏡", modifier = Modifier.padding(end = 8.dp))
//
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                Text(
//                                    text = "마을로 들어가기",
//                                    style = MaterialTheme.typography.titleLarge.copy(
//                                        letterSpacing = 1.sp
//                                    ),
//                                    color = Color(0xFF2F6F62)
//                                )
//                                Text(
//                                    text = "펫들이 기다리고 있어요!",
//                                    style = MaterialTheme.typography.labelSmall,
//                                    color = Color(0xFF6FA9A0)
//                                )
//                            }
//                        }
//                    }
//                }
//                Spacer(modifier = Modifier.size(70.dp))

            }

            "loading" -> Column(
                modifier = Modifier
                    .fillMaxSize()
                ,
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "로딩 중..",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.size(70.dp))

                LoginDownloadDialog(
                    onClose = reLoading
                )

            }

//            "" -> LoginDownloadDialog(
//                onClose = reLoading
//            )

            "loginLoading" -> {
                LoginLoadingDialog(
                    downloadPhotoCount = downloadPhotoCount
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MypatTheme {
        LoginScreen(
            googleLoginClick = {},
            onNavigateToMainScreen = {},
            loginState = "unLogin"
        )
    }
}