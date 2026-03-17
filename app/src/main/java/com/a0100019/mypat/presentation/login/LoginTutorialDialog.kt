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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme


@Composable
fun LoginTutorialDialog(
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(340.dp) // 너비를 고정하여 비율 안정화
                .wrapContentHeight()
                .shadow(20.dp, RoundedCornerShape(32.dp))
                .border(
                    width = 1.5.dp, // 테두리를 살짝 얇게 하여 세련미 추가
                    color = Color(0xFFD7CCC8),
                    shape = RoundedCornerShape(32.dp)
                )
                .background(
                    // 단순 단색보다 포근한 미색 적용
                    color = Color(0xFFFDFBF9),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "하루마을 초대장",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp, // 자간을 넓혀 고급스럽게
                        fontFamily = FontFamily.Serif
                    ),
                    color = Color(0xFF4E342E)
                )

                // 구분선 디자인 변경 (도트 느낌)
                Row(
                    modifier = Modifier.padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) {
                        Box(modifier = Modifier.size(4.dp).background(Color(0xFFD7CCC8), CircleShape))
                    }
                }

                // 텍스트 영역: 행간과 폰트 크기 미세 조정
                Text(
                    text = "하루마을에 오신 것을 환영합니다!\n\n" +
                            "이곳에는 이웃들과 소통하는 [게시판], 하루의 기록을 남기는 [일기장], \n" +
                            "나만의 귀여운 마을을 꾸미는 [마을 키우기]가 준비되어 있어요.\n" +
                            "\n" +
                            "모든 기능을 다 쓰지 않아도 괜찮아요.\n당신이 머물고 싶은 방식 그대로,\n" +
                            "하루마을에서 잠시 쉬어가세요.\n자, 이제 함께 시작해 볼까요?",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif
                    ),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF5D4037)
                )

                Spacer(modifier = Modifier.height(52.dp))

                // --- 버튼 영역 (애니메이션 로직은 유지) ---
                val infiniteTransition = rememberInfiniteTransition(label = "daily_btn_anim")
                val floatingOffset by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = -6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "floating"
                )

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")
                val shimmerX by infiniteTransition.animateFloat(
                    initialValue = -0.4f, targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
                    label = "shimmer"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp) // 조금 더 도톰하게
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationY = floatingOffset.dp.toPx()
                        }
                        .clickable(interactionSource = interactionSource, indication = null) { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    // 버튼 그림자 (바닥면)
                    Surface(
                        modifier = Modifier.fillMaxSize().offset(y = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFD54F).copy(alpha = 0.3f)
                    ) {}

                    // 버튼 본체 (노란색 젤리 느낌)
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFF9C4),
                        border = BorderStroke(2.dp, Color(0xFFFFD54F))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // 반짝임 효과
                            Box(
                                modifier = Modifier.matchParentSize().background(
                                    brush = Brush.linearGradient(
                                        colorStops = arrayOf(
                                            (shimmerX - 0.2f) to Color.Transparent,
                                            shimmerX to Color.White.copy(alpha = 0.5f),
                                            (shimmerX + 0.2f) to Color.Transparent
                                        )
                                    )
                                )
                            )

                            Text(
                                text = "시작하기",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginTutorialDialogPreview() {
    MypatTheme {
        LoginTutorialDialog(
            onClose = {},
        )
    }
}