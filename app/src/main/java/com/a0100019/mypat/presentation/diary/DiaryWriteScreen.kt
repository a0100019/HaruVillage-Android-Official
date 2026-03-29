package com.a0100019.mypat.presentation.diary

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.time.format.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.data.room.diary.Diary
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.a0100019.mypat.data.room.photo.Photo
import com.a0100019.mypat.presentation.main.mainDialog.SimpleAlertDialog
import com.a0100019.mypat.presentation.main.management.InterstitialAdManager
import com.a0100019.mypat.presentation.main.management.ManagementViewModel
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiaryWriteScreen(
    diaryWriteViewModel: DiaryWriteViewModel = hiltViewModel(),
    managementViewModel: ManagementViewModel = hiltViewModel(),
    popBackStack: () -> Unit
) {
    val diaryWriteState: DiaryWriteState = diaryWriteViewModel.collectAsState().value
    val context = LocalContext.current

    // Activity를 안전하게 가져오는 헬퍼 함수
    fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    // UI (Compose 등)에서의 적용
    val activity = remember(context) { context.findActivity() }

    // Orbit의 collectSideEffect 사용
    diaryWriteViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
//            is DiaryWriteSideEffect.ShowInterstitialAd -> {
//                // 1. 광고 실행 시도
//                if (activity != null) {
//                    InterstitialAdManager.showAd(activity) {
//                        // 광고가 닫히거나 실패했을 때 ViewModel의 콜백 실행
//                        sideEffect.onAdClosed()
//                    }
//                } else {
//                    // Activity를 찾을 수 없는 예외 상황 처리
//                    sideEffect.onAdClosed()
//                }
//            }

            is DiaryWriteSideEffect.Toast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }

            is DiaryWriteSideEffect.ShowReviewDialog -> {
                if (activity != null) {

                    val manager = ReviewManagerFactory.create(activity)

                    val request = manager.requestReviewFlow()

                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            manager.launchReviewFlow(activity, reviewInfo)
                        }
                    }
                }
            }

            // 기타 SideEffect 처리...
        }
    }

    // 뒤로가기 다이얼로그 상태
    var showExitDialog by remember { mutableStateOf(false) }

    // ✅ 다이얼로그 뜰 때는 뒤로가기 비활성화
    BackHandler(enabled = !showExitDialog) {
        showExitDialog = true
    }

    // ✅ 다이얼로그 UI
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "작성 중인 일기가 있어요",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                    )
                    },
            text = { Text(
                text = "정말 나가시겠습니까?\n작성한 내용은 저장되지 않습니다.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
                   },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false  // 그냥 닫기
                }) {
                    Text("아니오")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    popBackStack()  // 🔥 뒤로 나가기
                }) {
                    Text("네")
                }
            }
        )
    }

    // 아래는 실제 일기 UI
    DiaryWriteScreen(
        writeDiaryData = diaryWriteState.writeDiaryData,
        writePossible = diaryWriteState.writePossible,
        dialogState = diaryWriteState.dialogState,
        writeFinish = diaryWriteState.writeFinish,
        photoDataList = diaryWriteState.photoDataList,
        clickPhoto = diaryWriteState.clickPhoto,
        isPhotoLoading = diaryWriteState.isPhotoLoading,
        diarySequence = diaryWriteState.diarySequence,
        firstWrite = diaryWriteState.firstWrite,
        aiText = diaryWriteState.aiText,

        onContentsTextChange = diaryWriteViewModel::onContentsTextChange,
        clickPhotoChange = diaryWriteViewModel::clickPhotoChange,
        onDiaryFinishClick = diaryWriteViewModel::onDiaryFinishClick,
        popBackStack = popBackStack,
        emotionChangeClick = diaryWriteViewModel::emotionChangeClick,
        onDialogStateChange = diaryWriteViewModel::onDialogStateChange,
        onImageSelected = { uri ->
            // ✅ 여기서 뷰모델 호출!
            diaryWriteViewModel.handleImageSelection(context, uri)
        },
        deleteImage = diaryWriteViewModel::deleteImage
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiaryWriteScreen(
    writeDiaryData: Diary,
    writePossible: Boolean,
    dialogState: String,
    photoDataList: List<Photo> = emptyList(),
    clickPhoto: String = "",
    isPhotoLoading: Boolean = false,
    diarySequence: Int = 0,
    firstWrite: Boolean = true,
    aiText: String = "",

    onDiaryFinishClick: () -> Unit,
    onContentsTextChange: (String) -> Unit,
    popBackStack: () -> Unit,
    emotionChangeClick: (String) -> Unit,
    onDialogStateChange: (String) -> Unit,
    writeFinish: Boolean = false,
    onLastFinishClick: () -> Unit = {},
    onImageSelected: (Uri) -> Unit = {}, // ✅ 사진 선택 콜백 추가
    deleteImage: (Photo) -> Unit = {},
    clickPhotoChange: (String) -> Unit = {}
) {

    val context = LocalContext.current

    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    // 🔹 감정 선택 다이얼로그
    if (dialogState == "emotion") {
        DiaryEmotionDialog(
            onClose = { onDialogStateChange("") },
            onEmotionClick = emotionChangeClick
        )
    } else if(dialogState == "exit") {
        SimpleAlertDialog(
            text = "정말 나가시겠습니까?\n" +
                    "작성한 내용은 저장되지 않습니다."
            ,
            onConfirmClick = {
                popBackStack()
            },
            onDismissClick = {
                onDialogStateChange("")
            }
        )
    }

    if(clickPhoto != "") {
        DiaryPhotoDialog(
            onClose = { clickPhotoChange("") },
            clickPhoto = clickPhoto
        )
    }

    // 🔹 작성 완료 다이얼로그
    if (writeFinish && firstWrite) {
        DiaryFirstFinishDialog(
            onClose = { popBackStack() },
            diarySequence = diarySequence
        )
    } else if(writeFinish) {
        DiaryFinishDialog(onClose = { popBackStack() })
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🌿 배경 이미지
//        BackGroundImage(modifier = Modifier.fillMaxSize())

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE1BEE7),
                                Color(0xFFBBDEFB)
                            )
                        )
                    )
            ) {
                // 컨텐츠 공간
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 12.dp)
                .imePadding() // ⬅️ 키보드가 점유하는 공간만큼 하단 여백을 자동으로 만듭니다.
                .bringIntoViewRequester(bringIntoViewRequester)
        ) {

            /* ───────── 상단 헤더 ───────── */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                JustImage(
                    filePath = "etc/exit.png",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(
                            onClick = { onDialogStateChange("exit") }
                        )
                )

                // 날짜
                Column {
                    val dateText = try {
                        val parsed = LocalDate.parse(writeDiaryData.date)
                        val day = parsed.dayOfWeek.getDisplayName(
                            TextStyle.SHORT,
                            Locale.KOREAN
                        )
                        val formatter = DateTimeFormatter.ofPattern("MM월 dd일")
                        "${parsed.format(formatter)} ($day)"
                    } catch (e: Exception) {
                        writeDiaryData.date
                    }

                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black // 👈 글자색을 검정으로 고정
                        )
                    )

                }

                // ✅ 갤러리 런처 정의
                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let { onImageSelected(it) }
                }

                JustImage(
                    filePath = "etc/camera.png",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            // 2. 갤러리 열기 (이미지 파일만 필터링)
                            galleryLauncher.launch("image/*")
                        }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {

                    val realSave = writePossible && !isPhotoLoading

                    // 💾 저장 버튼 (파스텔톤)
                    val backgroundColor by animateColorAsState(
                        targetValue = if (realSave) Color(0xFFB7E4C7) else Color(0xFFEAEAEA),
                        label = "buttonBackground"
                    )

                    Text(
                        text = "저장",
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(backgroundColor)
                            .clickable(
                                enabled = realSave,
                                onClick = {
                                    val prefs = context.getSharedPreferences(
                                        "diary_prefs",
                                        Context.MODE_PRIVATE
                                    )
                                    val alarm = prefs.getString("alarm", "0")
                                    if (alarm == "0") {
                                        prefs
                                            .edit()
                                            .putString("alarm", "1")
                                            .apply()
                                    }
                                    onDiaryFinishClick()
                                }
                            )
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        color = if (realSave) Color(0xFF2D6A4F) else Color(0xFF9E9E9E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // 😊 감정 버튼
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { onDialogStateChange("emotion") },
                        contentAlignment = Alignment.Center
                    ) {
                        JustImage(
                            filePath = writeDiaryData.emotion,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* ───────── 일기 입력 영역 ───────── */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF2F2F2).copy(alpha = 0.85f))
                    .padding(16.dp)

            ) {

                // 📸 사진 리스트 영역 (사진이 있거나, 혹은 업로드 중일 때 표시)
                if (photoDataList.isNotEmpty() || isPhotoLoading) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 12.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            // 1. 기존 사진 리스트 출력
                            items(photoDataList) { photo ->
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = photo.localPath,
                                        contentDescription = "일기 사진",
                                        modifier = Modifier.fillMaxSize().clickable { clickPhotoChange(photo.localPath) },
                                        contentScale = ContentScale.Crop
                                    )

                                    // 삭제 버튼
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(25.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .clickable { deleteImage(photo) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✕", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }

                            // 2. ⭐ 사진 업로드 중일 때 로딩 아이템 (첫 사진 추가 시에도 여기서 표시됨)
                            if (isPhotoLoading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .size(84.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.LightGray.copy(alpha = 0.3f))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "업로드 중..",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                BasicTextField(
                    value = writeDiaryData.contents,
                    onValueChange = onContentsTextChange,
                    modifier = Modifier
                        .fillMaxSize()
                    ,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 28.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (writeDiaryData.contents.isEmpty()) {
                            Text(
                                text = "\n\n" + aiText
                                ,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                        innerTextField()
                    }
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiaryWriteScreenPreview() {
    MypatTheme {
        DiaryWriteScreen(
            writeDiaryData = Diary(
                date = "2025-02-06",
                emotion = "emotion/smile.png",
                contents = "11",
                id = 1,
                state = "open",
            ),
            onContentsTextChange = {},
            onDiaryFinishClick = {},
            popBackStack = {},
            writePossible = false,
            emotionChangeClick = {},
            dialogState = "",
            onDialogStateChange = {}
        )
    }
}
