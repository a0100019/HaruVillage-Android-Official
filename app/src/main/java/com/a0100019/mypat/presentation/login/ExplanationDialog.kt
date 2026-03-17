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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
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
        onDismissRequest = { onClose() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(340.dp)
                .shadow(20.dp, RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFCF8F5), // 연한 베이지
                            Color(0xFFF3E5DC)  // 따뜻한 흙색 느낌
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF4E342E).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // 배경 장식 (패턴 느낌)
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF4E342E).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(22.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 상단 아이콘 (포인트)
                Text(
                    text = "🏡",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "하루마을 설명서",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                    ),
                    color = Color(0xFF4E342E)
                )

                // 세련된 구분선
                Row(
                    modifier = Modifier.padding(vertical = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF4E342E).copy(alpha = 0.2f)))
                    Box(modifier = Modifier.padding(horizontal = 8.dp).size(6.dp).background(Color(0xFF4E342E), CircleShape))
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF4E342E).copy(alpha = 0.2f)))
                }

                // 스크롤 가능한 본문 영역
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    val contentColor = Color(0xFF3E2723)
                    val bodyStyle = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        textAlign = TextAlign.Center
                    )

                    // 불렛 포인트 스타일로 가독성 업그레이드
                    val items = listOf(
                        "매일 일기를 작성해 3 햇살을 모으고, 하루 미션과 펫을 통해 달빛을 모아보세요.",
                        "모은 햇살과 달빛으로 상점에서 귀여운 펫과 아이템을 가질 수 있어요.",
                        "마을 꾸미기에서 나만의 마을을 꾸미고, 펫을 키우며 바쁜 하루를 힐링해요",
                        "마을을 꾸미고 펫을 키우기 위한 기능을 계속 업데이트 중이니 기대해주세요!",
                    )

                    items.forEach { text ->
                        Row(modifier = Modifier.padding(bottom = 32.dp)) {
                            Text(text = text, style = bodyStyle)
                        }
                    }
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
                                            text = "자유롭게 마을을 둘러보아요!",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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

