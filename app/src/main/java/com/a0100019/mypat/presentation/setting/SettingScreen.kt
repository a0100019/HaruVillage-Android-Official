package com.a0100019.mypat.presentation.setting

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.a0100019.mypat.R
import com.a0100019.mypat.data.room.letter.Letter
import com.a0100019.mypat.domain.AppBgmManager
import com.a0100019.mypat.presentation.login.ExplanationDialog
import com.a0100019.mypat.presentation.main.mainDialog.SimpleAlertDialog
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun SettingScreen(
    settingViewModel: SettingViewModel = hiltViewModel(),
    onSignOutClick: () -> Unit,
    popBackStack: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingState = settingViewModel.collectAsState().value

    // 🔹 SideEffect 처리
    settingViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SettingSideEffect.Toast ->
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()

            SettingSideEffect.NavigateToLoginScreen ->
                onSignOutClick()

            is SettingSideEffect.OpenUrl -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sideEffect.url))
                context.startActivity(intent)
            }
        }
    }

    // 🔹 Google 로그인 launcher (LoginScreen과 동일)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                settingViewModel.onGoogleLoginChangeClick(idToken)
            }
        } catch (e: Exception) {
            Log.e("setting", "구글 로그인 실패", e)
        }
    }

    SettingScreen(
        userData = settingState.userDataList,
        settingSituation = settingState.settingSituation,
        editText = settingState.editText,
        clickLetterData = settingState.clickLetterData,
        letterDataList = settingState.letterDataList,
        recommending = settingState.recommending,
        recommended = settingState.recommended,
        donationList = settingState.donationList,

        onClose = settingViewModel::onCloseClick,
        onSignOutClick = settingViewModel::dataSave,
        onSituationChange = settingViewModel::onSituationChange,
        onAccountDeleteClick = settingViewModel::onAccountDeleteClick,
        onEditTextChange = settingViewModel::onEditTextChange,
        onCouponConfirmClick = settingViewModel::onCouponConfirmClick,
        onSettingTalkConfirmClick = settingViewModel::onSettingTalkConfirmClick,
        clickLetterDataChange = settingViewModel::clickLetterDataChange,
        onLetterConfirmClick = settingViewModel::onLetterConfirmClick,
        onLetterLinkClick = settingViewModel::onLetterLinkClick,
        onLetterCloseClick = settingViewModel::onLetterCloseClick,
        onRecommendationClick = settingViewModel::onRecommendationClick,
        onRecommendationSubmitClick = settingViewModel::onRecommendationSubmitClick,
        onMedal19Click = settingViewModel::onMedal19Click,
        popBackStack = popBackStack,
        onReviewClick = settingViewModel::onReviewClick,

        // 🔥 여기서 연결
        onGoogleLoginChangeClick = {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            googleSignInClient.signOut().addOnCompleteListener {
                launcher.launch(googleSignInClient.signInIntent)
            }
        }
    )
}

@Composable
fun SettingScreen(

    userData: List<User>,
    settingSituation: String,
    editText: String,
    letterDataList: List<Letter>,
    clickLetterData: Letter,
    recommending : String = "-1",
    recommended : String = "-1",
    donationList: List<Donation> = emptyList(),

    onSignOutClick: () -> Unit,
    onClose: () -> Unit,
    onSituationChange: (String) -> Unit,
    onAccountDeleteClick: () -> Unit,
    onEditTextChange: (String) -> Unit,
    onCouponConfirmClick: () -> Unit,
    onSettingTalkConfirmClick: () -> Unit,
    clickLetterDataChange: (Int) -> Unit,
    onLetterLinkClick: () -> Unit,
    onLetterConfirmClick: () -> Unit = {},
    onLetterCloseClick: () -> Unit = {},
    popBackStack: () -> Unit = {},
    onRecommendationClick: () -> Unit = {},
    onRecommendationSubmitClick: () -> Unit = {},
    onMedal19Click: () -> Unit = {},
    onReviewClick: () -> Unit = {},
    onGoogleLoginChangeClick: () -> Unit = {},

) {

    when (settingSituation) {
        "terms" -> TermsDialog(
            onClose = onClose,
            onMedal19Click = onMedal19Click
        )

        "accountDelete" -> AccountDeleteDialog(
            onClose = onClose,
            onAccountDeleteTextChange = onEditTextChange,
            accountDeleteString = editText,
            onConfirmClick = onAccountDeleteClick
        )

        "coupon" -> CouponDialog(
            onClose = onClose,
            onCouponTextChange = onEditTextChange,
            couponText = editText,
            onConfirmClick = onCouponConfirmClick
        )

        "settingTalk" -> SettingTalkDialog(
            onClose = onClose,
            onSettingTalkTextChange = onEditTextChange,
            settingTalkText = editText,
            onConfirmClick = onSettingTalkConfirmClick
        )

        "letter" -> LetterDialog(
            onClose = onClose,
            onLetterClick = clickLetterDataChange,
            letterDataList = letterDataList
        )

        "recommendation" -> RecommendationDialog(
            onClose = onClose,
            onRecommendationTextChange = onEditTextChange,
            recommending = recommending,
            recommended = recommended,
            recommendationText = editText,
            userData = userData,
            onRecommendationSubmitClick = onRecommendationSubmitClick
        )

        "explanation" -> ExplanationDialog(
            onClose = onClose
        )

        "donation" -> DonationDialog(
            onClose = onClose,
            donationList = donationList
        )

        "review" -> SimpleAlertDialog(
            onConfirmClick = onReviewClick,
            onDismissClick = onClose,
            text = "리뷰는 개발자에게 큰 힘이 됩니다..! 리뷰를 작성하고 하루마을 응원단 칭호 및 10 햇살을 획득하겠습니까?",
        )

        "font" -> FontChangeDialog(
            onClose = onClose
        )
    }

    if (clickLetterData.id != 0) {
        LetterViewDialog(
            onClose = onLetterCloseClick,
            clickLetterData = clickLetterData,
            onLetterLinkClick = onLetterLinkClick,
            onLetterConfirmClick = onLetterConfirmClick
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {

        BackGroundImage()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // 상단 제목
                Text(
                    text = "설정",
                    style = MaterialTheme.typography.displayMedium,
                )

                JustImage(
                    filePath = "etc/exit.png",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(30.dp)
                        .clickable {
                            popBackStack()
                        }
                )

            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()

            // 기능 관련
            SettingButton(
                text = "우체통",
                onClick = { onSituationChange("letter") },
                modifier = Modifier.fillMaxWidth()
            )

            SettingButton(
                text = "대나무 숲",
                onClick = { onSituationChange("settingTalk") },
                modifier = Modifier.fillMaxWidth()
            )

            SettingButton(
                text = "응원하고 보상받기",
                onClick = { onSituationChange("review") },
                modifier = Modifier.fillMaxWidth()
            )

            SettingButton(
                text = "글씨체 변경하기",
                onClick = { onSituationChange("font") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(3.dp))
            Divider()
            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                // 기타 정보
                SettingButton(
                    text = "이용약관",
                    onClick = { onSituationChange("terms") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.size(12.dp))

                SettingButton(
                    text = "쿠폰 코드",
                    onClick = { onSituationChange("coupon") },
                    modifier = Modifier.weight(1f)
                )

            }

            val context = LocalContext.current
            val prefs = context.getSharedPreferences("bgm_prefs", Context.MODE_PRIVATE)
            var bgmOn = prefs.getBoolean("bgmOn", true)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                SettingButton(
                    text = "추천인 확인",
                    onClick = onRecommendationClick,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.size(12.dp))

                SettingButton(
                    text = "방명록",
                    onClick = { onSituationChange("donation") },
                    modifier = Modifier.weight(1f)
                )

//                MainButton(
//                    text = "만보기 일시 정지",
//                    onClick = {
//                        val intent = Intent(context, StepForegroundService::class.java)
//                        context.stopService(intent)
//                    },
//                    modifier = Modifier.weight(1f)
//                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                SettingButton(
                    text = "bgm 켜기 / 끄기",
                    onClick = {
                        if (bgmOn) {
                            AppBgmManager.pause()
                            prefs.edit().putBoolean("bgmOn", false).apply()
                            bgmOn = prefs.getBoolean("bgmOn", true)
                        } else {
                            AppBgmManager.play()
                            prefs.edit().putBoolean("bgmOn", true).apply()
                            bgmOn = prefs.getBoolean("bgmOn", true)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.size(12.dp))

                SettingButton(
                    text = "설명서",
                    onClick = { onSituationChange("explanation") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ){

                if(userData.find { it.id == "selectPat" }?.value3 == "0"){
                    SettingButton(
                        text = "계정삭제",
                        onClick = { onSituationChange("accountDelete") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.size(12.dp))

                    SettingButton(
                        text = "로그아웃",
                        onClick = onSignOutClick,
                        modifier = Modifier.weight(1f)
                    )
                } else {

                    // ✅ 구글 로그인 버튼
                    Button(
                        onClick = onGoogleLoginChangeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.LightGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            JustImage(
                                filePath = "etc/googleLogo.png",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("구글 로그인으로 데이터를 보호하세요")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Divider()

            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = "하루마을을 이용해주셔서 감사합니다",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun SettingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White, // 깔끔한 화이트
    contentColor: Color = Color(0xFF212529), // 깊이감 있는 다크 그레이
    iconPath: String? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        // 미세한 그림자(Elevation)를 추가해 버튼임을 명확히 함
        shadowElevation = 2.dp,
        // 테두리를 아주 연하게 주어 고급스러움 추가
        border = BorderStroke(1.dp, Color(0xFFF1F3F5)),
        modifier = modifier
            .fillMaxWidth() // 보통 설정 버튼은 가득 차는 게 예뻐요
            .height(60.dp)  // 살짝 더 시원하게 높이 조절
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start // 텍스트는 보통 왼쪽 정렬이 국룰!
        ) {
            if (iconPath != null) {
                // 아이콘 배경에 원형 칩 효과를 주면 훨씬 예뻐요
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF8F9FA), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    JustImage(
                        filePath = iconPath,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = text,
                modifier = Modifier.weight(1f), // 텍스트가 공간을 다 차지하게 해서
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold, // 조금 더 두껍게
                    color = contentColor
                )
            )

            // 오른쪽에 '>' 모양의 화살표를 넣어주면 "누를 수 있음"을 직관적으로 알려줘요
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFADB5BD),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    MypatTheme {
        SettingScreen(
            onClose = {},
            userData = emptyList(),
            onSignOutClick = {},
            settingSituation = "",
            onSituationChange = {},
            onAccountDeleteClick = {},
            onEditTextChange = {},
            editText = "",
            onCouponConfirmClick = {},
            onSettingTalkConfirmClick = {},
            clickLetterData = Letter(),
            clickLetterDataChange = {},
            letterDataList = emptyList(),
            onLetterLinkClick = {},
            onLetterCloseClick = {},
            onLetterConfirmClick = {},

        )
    }
}