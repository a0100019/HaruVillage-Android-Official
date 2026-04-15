package com.a0100019.mypat.presentation.neighbor.privateChat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun PrivateChatGameScreen(
    privateChatGameViewModel: PrivateChatGameViewModel = hiltViewModel(),

    popBackStack: () -> Unit = {},

    ) {

    val privateChatGameState : PrivateChatGameState = privateChatGameViewModel.collectAsState().value

    val context = LocalContext.current

    privateChatGameViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PrivateChatGameSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
        }
    }

    PrivateChatGameScreen(
        situation = privateChatGameState.situation,
        currentValue = privateChatGameState.currentValue,
        targetStart = privateChatGameState.targetStart,
        targetEnd = privateChatGameState.targetEnd,
        score = privateChatGameState.score,

        onClose = privateChatGameViewModel::onClose,
        onGameStartClick = privateChatGameViewModel::onGameStartClick,
        popBackStack = popBackStack,
        onAttackClick = privateChatGameViewModel::onAttackClick
    )
}

@Composable
fun PrivateChatGameScreen(
    currentValue: Int = 500,
    targetStart: Int = 450,
    targetEnd: Int = 550,
    score: Int = 0,
    situation: String = "준비",

    onClose: () -> Unit = {},
    popBackStack: () -> Unit = {},
    onGameStartClick: () -> Unit = {},
    onAttackClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        BackGroundImage()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.size(36.dp))

            //  점수
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "점수",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF888888)
                )

                Spacer(modifier = Modifier.size(24.dp))

                Text(
                    text = score.toString(),
                    fontSize = 70.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            when(score / 10 % 4){
                0 -> JustImage(
                    filePath = "monster/monster1.json",
                    modifier = Modifier
                        .size(150.dp)
                )

                1 -> JustImage(
                    filePath = "monster/monster2.json",
                    modifier = Modifier
                        .size(150.dp)
                )

                2 -> JustImage(
                    filePath = "monster/monster3.json",
                    modifier = Modifier
                        .size(150.dp)
                )

                else -> JustImage(
                    filePath = "monster/monster4.json",
                    modifier = Modifier
                        .size(150.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            //  게이지 카드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF8FAFF),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE1E7F5),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {

                Text(
                    text = "타이밍을 맞추세요",
                    fontSize = 13.sp,
                    color = Color(0xFF555555),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                PrivateChatGameHorizontalLine(
                    currentValue = currentValue,
                    targetStart = targetStart,
                    targetEnd = targetEnd,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ⚔️ 상태별 액션 버튼
            when (situation) {

                "준비" -> MainButton(
                    text = "\n🎮 게임 시작\n",
                    onClick = onGameStartClick,
                    modifier = Modifier.fillMaxWidth()
                )

                "진행중" -> MainButton(
                    text = "\n⚔️ 공격\n",
                    onClick = onAttackClick,
                    modifier = Modifier.fillMaxWidth()
                )

                "성공" -> MainButton(
                    text = "\n다음 라운드\n",
                    onClick = onGameStartClick,
                    modifier = Modifier.fillMaxWidth()
                )

                "종료" -> MainButton(
                    text = "\n나가기\n",
                    onClick = popBackStack,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PrivateChatGameScreenPreview() {
    MypatTheme {
        PrivateChatGameScreen(
            situation = ""
        )
    }
}