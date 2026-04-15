package com.a0100019.mypat.presentation.neighbor.privateChat

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.R
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.presentation.main.mainDialog.SimpleAlertDialog
import com.a0100019.mypat.presentation.neighbor.chat.ChatSideEffect
import com.a0100019.mypat.presentation.neighbor.chat.getPastelColorForTag
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.image.etc.KoreanIdiomImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun PrivateChatInScreen(
    privateChatInViewModel: PrivateChatInViewModel = hiltViewModel(),
    popBackStack: () -> Unit = {},
    onNavigateToPrivateRoomScreen: () -> Unit = {},
    onNavigateToNeighborInformationScreen: () -> Unit = {},
    onNavigateToPrivateChatGameScreen: () -> Unit = {}
) {

    val privateChatInState : PrivateChatInState = privateChatInViewModel.collectAsState().value

    val context = LocalContext.current

    privateChatInViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PrivateChatInSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            PrivateChatInSideEffect.NavigateToPrivateRoomScreen -> onNavigateToPrivateRoomScreen()
            PrivateChatInSideEffect.NavigateToNeighborInformationScreen -> onNavigateToNeighborInformationScreen()

        }
    }

    PrivateChatInScreen(
        userDataList = privateChatInState.userDataList,
        chatMessages = privateChatInState.chatMessages,
        text = privateChatInState.text,
        yourName = privateChatInState.yourName,
        yourTag = privateChatInState.yourTag,
        situation = privateChatInState.situation,
        privateChatData = privateChatInState.privateChatData,

        popBackStack = popBackStack,
        onTextChange = privateChatInViewModel::onTextChange,
        onChatSubmitClick = privateChatInViewModel::onChatSubmitClick,
        onNavigateToPrivateRoomScreen = onNavigateToPrivateRoomScreen,
        onNeighborInformationClick = privateChatInViewModel::onNeighborInformationClick,
        onSituationChange = privateChatInViewModel::onSituationChange,
        onPrivateRoomDelete = privateChatInViewModel::onPrivateRoomDelete,
        onNavigateToPrivateChatGameScreen = onNavigateToPrivateChatGameScreen,
        onClose = privateChatInViewModel::onClose
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PrivateChatInScreen(
    userDataList: List<User> = emptyList(),
    chatMessages: List<PrivateChatMessage> = emptyList(),
    text: String = "",
    yourName: String = "이웃",
    yourTag: String = "0",
    situation: String = "",
    privateChatData: PrivateChatData = PrivateChatData(),

    popBackStack: () -> Unit = {},
    onTextChange: (String) -> Unit = {},
    onChatSubmitClick: () -> Unit = {},
    onNavigateToPrivateRoomScreen: () -> Unit = {},
    onNeighborInformationClick: () -> Unit = {},
    onSituationChange: (String) -> Unit = {},
    onPrivateRoomDelete: () -> Unit = {},
    onNavigateToPrivateChatGameScreen: () -> Unit = {},
    onClose: () -> Unit = {},
) {

    val today =
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    val todayScore1 = if(privateChatData.lastGame == today) privateChatData.todayScore1 else 0
    val todayScore2 = if(privateChatData.lastGame == today) privateChatData.todayScore2 else 0
    val todayScore = if(privateChatData.lastGame == today) privateChatData.todayScore1 + privateChatData.todayScore2 else 0
    val totalScore = if(privateChatData.lastGame == today) privateChatData.totalScore + todayScore else privateChatData.totalScore

    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    when(situation) {
        "roomDelete" -> SimpleAlertDialog(
            onConfirmClick = onPrivateRoomDelete,
            onDismissClick = {
                onClose()
            },
            text = "친구 삭제하겠습니까?\n되돌릴 수 없습니다. 신중하게 생각하세요"
        )
        "deleteCheck" -> SimpleAlertDialog(
            onDismissOn = false,
            onConfirmClick = onNavigateToPrivateRoomScreen,
            text = "삭제되었습니다."
        )
        "gameQuestion" -> SimpleAlertDialog(
            onConfirmClick = {onSituationChange("")},
            onDismissOn = false,
            text = "친구와 보스를 공격하세요!\n\n1. 서로 번갈아 가며 공격할 수 있습니다.\n\n2. 친구와 함께 높은 점수를 차지하여 순위에 올라보세요! 하루마을 최고의 친구가 되어보세요!"
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {

        BackGroundImage()

        Box(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                    ,
                    contentAlignment = Alignment.Center
                ) {

                    // 오른쪽 버튼
                    MainButton(
                        text = "삭제",
                        onClick = {
                            onSituationChange("roomDelete")
                        },

                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                onNeighborInformationClick()
                            }
                    ) {

                        Text(
                            text = yourName,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "#$yourTag",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                        )
                    }

                    JustImage(
                        filePath = "etc/exit.png",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(30.dp)
                            .clickable {
                                onNavigateToPrivateRoomScreen()
                            }
                    )
                }


//                    // ── 💬 총 메시지 수 ──
//                    Text(
//                        text = "대화 수 ${privateChatData.messageCount}",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = Color(0xFF4A6CF7)
//                    )
//
//                    Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(
                            color = Color(0xFFF8FAFF),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE1E7F5),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(12.dp)
                ) {

                    // ── 1️⃣ 상단 한 줄: 최고 / 타이틀 / 누적 ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "미니게임",
                                fontSize = 24.sp,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            JustImage(
                                filePath = "etc/question.png",
                                modifier = Modifier
                                    .size(15.dp)
                                    .clickable {
                                        onSituationChange("gameQuestion")
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))


                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        val bgColor1 = lerp(
                            getPastelColorForTag(privateChatData.user1),
                            Color.White,
                            0.4f   // 연하면 0.3f / 진하면 0.2f
                        )

//                        Box(
//                            modifier = Modifier
//                                .background(
//                                    color = bgColor1,
//                                    shape = RoundedCornerShape(8.dp) // 네모지만 살짝 둥글게
//                                )
//                                .padding(horizontal = 8.dp, vertical = 4.dp)
//                        ) {
//                            Text(
//                                text = "${privateChatData.name1} $todayScore1",
//                                fontSize = 13.sp,
//                            )
//                        }
//                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "우리의 점수 : $totalScore 점",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                            ,
                            textAlign = TextAlign.Center
                        )

//                        Spacer(modifier = Modifier.weight(1f))

//                        val bgColor2 = lerp(
//                            getPastelColorForTag(privateChatData.user2),
//                            Color.White,
//                            0.4f   // 연하면 0.3f / 진하면 0.2f
//                        )
//
//                        Box(
//                            modifier = Modifier
//                                .background(
//                                    color = bgColor2,
//                                    shape = RoundedCornerShape(8.dp) // 네모지만 살짝 둥글게
//                                )
//                                .padding(horizontal = 8.dp, vertical = 4.dp)
//                        ) {
//                            Text(
//                                text = "${privateChatData.name2} $todayScore2",
//                                fontSize = 13.sp,
//                            )
//                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // 액션
                        if (privateChatData.attacker != yourTag) {
                            MainButton(
                                text = "⚔️ 도전",
                                onClick = {
                                    onNavigateToPrivateChatGameScreen()
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFEDEDED), // 연한 회색
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFD6D6D6),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(4.dp)
                                ,
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⏳ 대기 중",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9A9A9A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 12.dp, bottom = 8.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .imePadding() // ⬅️ 키보드가 점유하는 공간만큼 하단 여백을 자동으로 만듭니다.
                        .bringIntoViewRequester(bringIntoViewRequester)
                ) {

                    if (chatMessages.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(start = 6.dp, end = 6.dp, top = 12.dp),
                            reverseLayout = true,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(chatMessages.reversed()) { index, message ->

                                val myTag = userDataList.find { it.id == "auth" }!!.value2
                                val isMine = message.tag == myTag
                                val isSystem = message.tag == "0"

                                val alignment = when {
                                    isMine -> Arrangement.End
                                    else -> Arrangement.Start
                                }

                                val bubbleColor = getPastelColorForTag(message.tag)

                                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                                val today = dateFormat.format(Date())

                                val prevDate = chatMessages.reversed().getOrNull(index - 1)
                                val currentDate = dateFormat.format(Date(message.timestamp))
                                val previousDate = prevDate?.let { dateFormat.format(Date(it.timestamp)) }

                                // 📅 날짜 구분선
                                if (currentDate != previousDate && currentDate != today) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = SimpleDateFormat("MM월 dd일 E요일", Locale.KOREA)
                                                .format(Date(message.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // ==========================
                                // 📢 시스템 공지 (tag == 0)
                                // ==========================
                                if (isSystem) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = Color(0xFFEAF2FF),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = Color(0xFFB6CCF5),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = message.message,
                                                color = Color(0xFF2F5FB3),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                } else {
                                    // ==========================
                                    //  일반 채팅
                                    // ==========================
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp),
                                        horizontalArrangement = alignment
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .widthIn(max = 280.dp)
                                                .padding(horizontal = 8.dp),
                                            horizontalAlignment =
                                            if (isMine) Alignment.End else Alignment.Start
                                        ) {

                                            Row {
                                                Row(
                                                    modifier = Modifier.clickable {
                                                        if (!isMine) onNeighborInformationClick()
                                                    }
                                                ) {
                                                    Text(
                                                        text = message.name,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                                    )
                                                    Text(
                                                        text = "#${message.tag}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                                    )
                                                }

                                                val time = remember(message.timestamp) {
                                                    SimpleDateFormat(
                                                        "MM/dd HH:mm",
                                                        Locale.getDefault()
                                                    ).format(Date(message.timestamp))
                                                }

                                                Text(
                                                    text = time,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        bubbleColor,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = message.message,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f), // 화면 전체 채우기
                            contentAlignment = Alignment.Center // 가로+세로 가운데 정렬
                        ) {
                            Text(
                                text = "첫 대화를 시작해보세요",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth(1f)
                            )
                        }

                    }

                    // 입력창 + 전송버튼
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = text,
                            onValueChange = onTextChange,
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            placeholder = { Text("메시지를 입력하세요") },
                            maxLines = 4,
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,// 배경색 필요 시 조정
                                focusedIndicatorColor = Color.Transparent, // 포커스 상태 밑줄 제거
                                unfocusedIndicatorColor = Color.Transparent, // 비포커스 상태 밑줄 제거
                                disabledIndicatorColor = Color.Transparent // 비활성화 상태 밑줄 제거
                            )
                        )

                        Image(
                            painter = painterResource(id = R.drawable.forwarding),
                            contentDescription = "회전된 이미지",
                            modifier = Modifier
                                .size(40.dp)
                                .rotate(90f)
                                .padding(8.dp)
                                .clickable {
                                    onChatSubmitClick()
                                }
                        )

                    }

                }


            }

        }

    }
}

@Preview(showBackground = true)
@Composable
fun PrivateChatScreenInPreview() {
    MypatTheme {
        PrivateChatInScreen(

            userDataList = listOf(
                User(id = "auth", value2 = "1")
            ),
            chatMessages = listOf(
                PrivateChatMessage(tag = "0", message = "aaaa"),
                PrivateChatMessage(tag = "1", message = "aaaa"), PrivateChatMessage(tag = "2", message = "aaaa")
            )

        )
    }
}