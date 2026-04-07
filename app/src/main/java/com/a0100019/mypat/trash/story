package com.a0100019.mypat.presentation.login

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.a0100019.mypat.data.room.letter.Letter
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import kotlin.random.Random

@Composable
fun ExplanationDialog(
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight(0.8f),
            contentAlignment = Alignment.Center
        ) {
            // 1. 배경 이미지
            JustImage(
                filePath = "etc/story.webp",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(15.dp, shape = RoundedCornerShape(8.dp))
            )

            // 2. 가독성을 위한 은은한 덮개 (0.3f 정도가 적당합니다)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            // 3. 내부 콘텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 35.dp, vertical = 35.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "스토리",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold, // 제목은 더 강조
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Serif // 세리프체 적용
                    ),
                    color = Color(0xFF2E1A16)
                )

                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 20.dp)
                        .width(60.dp)
                        .height(2.dp)
                        .background(Color(0xFF4E342E))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "어느 날, 당신은 버려진 마을의 관리인으로 임명되었습니다.\n" +
                                "\n당신에게 주어진 건 낡은 펜 한 자루와 빈 일기장뿐.. 하지만 이 마을에는 오래된 전설이 하나 전해져 내려옵니다.\n" +
                                "\n" +
                                "\"누군가의 진심 어린 하루가 기록될 때마다, 마을의 시간이 다시 흐르기 시작한다.\"\n\n" +
                                "마을을 다시 살리는 방법은 거창하지 않습니다. 그저 하루를 꾸준히 기록하는 것.\n그것뿐입니다.\n"

                        ,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold, // 두껍게 하여 가독성 확보
                            fontFamily = FontFamily.Serif // 아까 좋아하셨던 세리프체 적용
                        ),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1B0C0A)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 1. 애니메이션 변수 정의 (기존 shimmer 코드 위에 추가)
                val infiniteTransition = rememberInfiniteTransition(label = "daily_btn_anim")

                // 둥실둥실 뜨는 효과
                val floatingOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "floating"
                )

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

// 눌렀을 때 내려가는 깊이 (isPressed일 때 floating 효과를 상쇄하며 바닥으로 붙음)
                val pressOffset by animateFloatAsState(
                    targetValue = if (isPressed) 4f else 0f,
                    label = "pressOffset"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.97f else 1f,
                    label = "daily_mission_scale"
                )

                // ✨ 반짝임 애니메이션 (기존 유지)
                val shimmerX by infiniteTransition.animateFloat(
                    initialValue = -0.4f,
                    targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "shimmerX"
                )

                val shimmerColor = Color.White.copy(alpha = 0.4f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp) // 버튼 높이 고정
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                // 둥실둥실 효과 + 누를 때 바닥으로 내려가는 효과 합산
                                translationY = (floatingOffset + pressOffset).dp.toPx()
                            }
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onClose()
                            }
                    ) {
                        // [Layer 1] 하단 그림자/바닥 (입체감 부여)
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = 6.dp),
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF2F6F62).copy(alpha = 0.2f)
                        ) {}

                        // [Layer 2] 메인 버튼 바디
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFFEAF4F1),
                            border = BorderStroke(2.dp, Color(0xFF9ECFC3))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {

                                // 🌿 버튼 내부 내용
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "나의 위대한 역사 시작하기",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF2F6F62)
                                        )
                                    }
                                }

                                // ✨ 반짝임 레이어 (유리 스윕 효과)
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            brush = Brush.linearGradient(
                                                colorStops = arrayOf(
                                                    (shimmerX - 0.2f) to Color.Transparent,
                                                    shimmerX to shimmerColor,
                                                    (shimmerX + 0.2f) to Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExplanationDialogPreview() {
    MypatTheme {
        ExplanationDialog(
            onClose = {},
        )
    }
}