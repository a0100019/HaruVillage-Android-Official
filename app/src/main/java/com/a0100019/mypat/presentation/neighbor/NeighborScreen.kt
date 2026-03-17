package com.a0100019.mypat.presentation.neighbor

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun NeighborScreen(
    neighborViewModel: NeighborViewModel = hiltViewModel(),

    popBackStack: () -> Unit = {},
    onChatNavigateClick: () -> Unit = {},
    onCommunityNavigateClick: () -> Unit = {},
    onBoardNavigateClick: () -> Unit = {},
    onPrivateRoomNavigateClick: () -> Unit = {},
    onMainNavigateClick: () -> Unit = {},
) {

    val neighborState : NeighborState = neighborViewModel.collectAsState().value

    val context = LocalContext.current

    neighborViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is NeighborSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
        }
    }

    NeighborScreen(

        popBackStack = popBackStack,
        onChatNavigateClick = onChatNavigateClick,
        onCommunityNavigateClick = onCommunityNavigateClick,
        onBoardNavigateClick = onBoardNavigateClick,
        onPrivateRoomNavigateClick = onPrivateRoomNavigateClick,
        onMainNavigateClick = onMainNavigateClick,

        aiText = neighborState.aiText

    )
}

@Composable
fun NeighborScreen(
    aiText: String = "하루마을 커뮤니티는 힐링과 평화로운 분위기를 지향합니다.",

    onClose : () -> Unit = {},

    popBackStack: () -> Unit = {},
    onCommunityNavigateClick: () -> Unit = {},
    onChatNavigateClick: () -> Unit = {},
    onBoardNavigateClick: () -> Unit = {},
    onPrivateRoomNavigateClick: () -> Unit = {},
    onMainNavigateClick: () -> Unit = {},

    ) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {

        BackGroundImage()

        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 12.dp)
            ,
            verticalArrangement = Arrangement.SpaceBetween
        ){

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                ,
                contentAlignment = Alignment.Center
            ) {
                // 가운데 텍스트
                Text(
                    text = "이웃",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                )

                JustImage(
                    filePath = "etc/exit.png",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(30.dp)
                        .clickable {
                            onMainNavigateClick()
                        }
                )

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // 좌우 여백을 주면 더 깔끔해요
                verticalArrangement = Arrangement.spacedBy(12.dp) // 버튼 사이 간격
            ) {


                // --- 전체 채팅 ---
                MenuButton(
                    icon = "💬",
                    title = "전체 채팅",
                    subTitle = "자유로운 대화",
                    color = Color(0xFFE3F2FD),
                    textColor = Color(0xFF1565C0),
                    borderColor = Color(0xFF2196F3),
                    onClick = onChatNavigateClick
                )

                // --- 이웃 마을 ---
                MenuButton(
                    icon = "🏡",
                    title = "이웃 마을",
                    subTitle = "마을 둘러보기",
                    color = Color(0xFFFFF3E0),
                    textColor = Color(0xFFE65100),
                    borderColor = Color(0xFFFF9800),
                    onClick = onCommunityNavigateClick
                )

                // --- 개인 채팅 (친구) ---
                MenuButton(
                    icon = "✉️",
                    title = "친구",
                    subTitle = "1:1 채팅",
                    color = Color(0xFFFCE4EC),
                    textColor = Color(0xFFC2185B),
                    borderColor = Color(0xFFE91E63),
                    onClick = onPrivateRoomNavigateClick
                )
            }

            Text(
                text = aiText,
                textAlign = TextAlign.Center,
                modifier = Modifier
            )

        }

    }
}

@Composable
fun MenuButton(
    icon: String,
    title: String,
    subTitle: String,
    color: Color,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "") // 살짝만 눌리는 느낌

    Surface(
        modifier = modifier
            .fillMaxWidth() // 가로를 가득 채움
            .height(80.dp)  // 세로형일 때는 높이를 지정해주는 게 예뻐요
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = color,
        border = BorderStroke(2.dp, borderColor.copy(0.2f))
    ) {
        Row( // 세로 배치일 때는 내부를 Row로 구성해서 가로로 넓게 씁니다
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // 아이콘 배경 (동그라미)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = subTitle,
                    fontSize = 12.sp,
                    color = textColor.copy(0.7f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 끝에 화살표 하나 넣어주면 더 "버튼" 같아요
            Text(">", color = textColor.copy(0.5f), fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NeighborScreenPreview() {
    MypatTheme {
        NeighborScreen(
            aiText = "하루마을 커뮤니티는 힐링과 평화로운 분위기를 지향합니다."
        )
    }
}