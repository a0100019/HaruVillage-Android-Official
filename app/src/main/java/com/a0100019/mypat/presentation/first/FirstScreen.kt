package com.a0100019.mypat.presentation.first

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.presentation.diary.DiarySideEffect
import com.a0100019.mypat.presentation.main.mainDialog.SimpleAlertDialog
import com.a0100019.mypat.presentation.main.management.loading.LoadingSideEffect
import com.a0100019.mypat.presentation.main.management.loading.LoadingState
import com.a0100019.mypat.presentation.main.management.loading.LoadingViewModel
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun FirstContainerScreen(
    firstViewModel: FirstViewModel = hiltViewModel(),

    popBackStack: () -> Unit = {},
    onBoardNavigateClick: () -> Unit = {},
    onSettingNavigateClick: () -> Unit = {},
    onDiaryNavigateClick: () -> Unit = {},
    onMainNavigateClick: () -> Unit = {}

    ) {

    val firstState : FirstState = firstViewModel.collectAsState().value

    val context = LocalContext.current

    firstViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is FirstSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()

            FirstSideEffect.ExitApp ->
                (context as? Activity)?.finish()
        }
    }

    FirstScreen(
        situation = firstState.situation,

        onClose = firstViewModel::onClose,
        popBackStack = popBackStack,
        onBoardNavigateClick = onBoardNavigateClick,
        onSituationChangeClick = firstViewModel::onSituationChange,
        onExitClick = firstViewModel::onExitClick,
        onSettingNavigateClick = onSettingNavigateClick,
        onDiaryNavigateClick = onDiaryNavigateClick,
        onMainNavigateClick = onMainNavigateClick
    )
}

@Composable
fun FirstScreen(
    situation: String = "",

    onBoardNavigateClick: () -> Unit = {},
    onDiaryNavigateClick: () -> Unit = {},
    onMainNavigateClick: () -> Unit = {},

    onClose : () -> Unit = {},
    popBackStack: () -> Unit = {},
    onSituationChangeClick: (String) -> Unit = {},
    onExitClick: () -> Unit = {},
    onSettingNavigateClick: () -> Unit = {},
) {

    when(situation) {

    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {

        BackGroundImage()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 10.dp)
                ,
                contentAlignment = Alignment.Center
            ) {

                JustImage(
                    filePath = "etc/cog.png",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            onSettingNavigateClick()
                        }
                        .align(Alignment.CenterStart)
                    ,
                )

                // [오른쪽] 종료 버튼 (🚪 나가기 아이콘 스타일)
                JustImage(
                    filePath = "etc/switch.png",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            onExitClick()
                        }
                        .align(Alignment.CenterEnd)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {// 자유게시판 (사진 기능 추가 강조 버전)
                val interaction2 = remember { MutableInteractionSource() }
                val isPressed2 by interaction2.collectIsPressedAsState()
                val scale2 by animateFloatAsState(if (isPressed2) 0.96f else 1f, label = "")

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp) // 정보를 더 담기 위해 높이를 살짝 키움
                        .graphicsLayer {
                            scaleX = scale2
                            scaleY = scale2
                        }
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color(0xFF4CAF50),
                            spotColor = Color(0xFF4CAF50)
                        )
                        .clickable(
                            interactionSource = interaction2,
                            indication = null,
                            onClick = onBoardNavigateClick
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFB9F6CA))
                                )
                            )
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 아이콘 영역 (핀 이모지 + 우측 하단 작은 카메라 배지로 업데이트 암시)
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📌", fontSize = 36.sp)
                                }
                                // ⭐ 신규 기능 표시 (작은 카메라 아이콘 배지)
                                Box(
                                    modifier = Modifier
                                        .offset(x = 4.dp, y = 4.dp)
                                        .size(28.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📸", fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "자유게시판",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1B5E20),
                                        letterSpacing = (-0.5).sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "당신의 이야기가 궁금해요", // ⭐ 문구 변경
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "이웃들과 나누는 따뜻한 이야기",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2E7D32).copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32).copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // --- 일기장 버튼 로직 ---
                val interactionDiary = remember { MutableInteractionSource() }
                val isPressedDiary by interactionDiary.collectIsPressedAsState()
                val scaleDiary by animateFloatAsState(
                    targetValue = if (isPressedDiary) 0.96f else 1f,
                    label = "DiaryScaleAnimation"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .graphicsLayer {
                            scaleX = scaleDiary
                            scaleY = scaleDiary
                        }
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color(0xFFFF9800), // 오렌지/노란색 계열 그림자
                            spotColor = Color(0xFFFF9800)
                        )
                        .clickable(
                            interactionSource = interactionDiary,
                            indication = null,
                            onClick = onDiaryNavigateClick // 일기장 이동 함수 연결
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFFF3E0),
                                        Color(0xFFFFE0B2)
                                    ) // 따뜻한 일기장 테마
                                )
                            )
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 아이콘 영역 (책 이모지 + 연필 배지)
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📖", fontSize = 36.sp)
                                }
                                // 기록을 상징하는 연필 배지
                                Box(
                                    modifier = Modifier
                                        .offset(x = 4.dp, y = 4.dp)
                                        .size(28.dp)
                                        .background(Color(0xFFFF9800), CircleShape)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✏️", fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column {
                                Text(
                                    text = "일기장",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE65100),
                                    letterSpacing = (-0.5).sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "오늘 하루는 어떠셨나요?",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF6C00)
                                )
                                Text(
                                    text = "나만의 소중한 기록들을 남겨보세요",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFEF6C00).copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFFEF6C00).copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

// --- 마을 키우기 버튼 로직 ---
                val interactionTown = remember { MutableInteractionSource() }
                val isPressedTown by interactionTown.collectIsPressedAsState()
                val scaleTown by animateFloatAsState(
                    targetValue = if (isPressedTown) 0.96f else 1f,
                    label = "TownScaleAnimation"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .graphicsLayer {
                            scaleX = scaleTown
                            scaleY = scaleTown
                        }
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color(0xFF2196F3), // 하늘색/파란색 계열 그림자
                            spotColor = Color(0xFF2196F3)
                        )
                        .clickable(
                            interactionSource = interactionTown,
                            indication = null,
                            onClick = { onMainNavigateClick() } // 마을 키우기 이동 함수 연결
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFE1F5FE),
                                        Color(0xFFB3E5FC)
                                    ) // 활기찬 하늘색 테마
                                )
                            )
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 아이콘 영역 (집 이모지 + 새싹 배지)
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏡", fontSize = 36.sp)
                                }
                                // 성장을 상징하는 새싹 배지
                                Box(
                                    modifier = Modifier
                                        .offset(x = 4.dp, y = 4.dp)
                                        .size(28.dp)
                                        .background(Color(0xFF03A9F4), CircleShape)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌱", fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column {
                                Text(
                                    text = "내 마을",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF01579B),
                                    letterSpacing = (-0.5).sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "나만의 마을을 꾸며보아요",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0277BD)
                                )
                                Text(
                                    text = "마을 관련 기능이 계속 업데이트 중이에요!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0277BD).copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF0277BD).copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

        }

    }
}

@Preview(showBackground = true)
@Composable
fun FirstScreenPreview() {
    MypatTheme {
        FirstScreen(
            situation = ""
        )
    }
}