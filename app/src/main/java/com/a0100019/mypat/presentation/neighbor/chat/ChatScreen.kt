package com.a0100019.mypat.presentation.neighbor.chat

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.R
import com.a0100019.mypat.data.room.allUser.AllUser
import com.a0100019.mypat.data.room.item.Item
import com.a0100019.mypat.data.room.pat.Pat
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.trash.AppBgmManager
import com.a0100019.mypat.presentation.neighbor.community.CommunityUserDialog
import com.a0100019.mypat.presentation.main.mainDialog.SimpleAlertDialog
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = hiltViewModel(),
    popBackStack: () -> Unit = {},
    onNavigateToNeighborInformationScreen: () -> Unit = {},

    ) {

    val chatState : ChatState = chatViewModel.collectAsState().value

    val context = LocalContext.current

    chatViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ChatSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            ChatSideEffect.NavigateToNeighborInformationScreen -> onNavigateToNeighborInformationScreen()

        }
    }

    CommunityScreen(
        situation = chatState.situation,
        patDataList = chatState.patDataList,
        itemDataList = chatState.itemDataList,
        allUserDataList = chatState.allUserDataList,
        clickAllUserData = chatState.clickAllUserData,
        clickAllUserWorldDataList = chatState.clickAllUserWorldDataList,
        chatMessages = chatState.chatMessages,
        newChat = chatState.newChat,
        userDataList = chatState.userDataList,
        alertState = chatState.alertState,
        allAreaCount = chatState.allAreaCount,
        text2 = chatState.text2,
        text3 = chatState.text3,
        anonymous = chatState.anonymous,

        onSituationChange = chatViewModel::onSituationChange,
        onChatTextChange = chatViewModel::onChatTextChange,
        onChatSubmitClick = chatViewModel::onChatSubmitClick,
        onUserRankClick = chatViewModel::onUserRankClick,
        onBanClick = chatViewModel::onBanClick,
        alertStateChange = chatViewModel::alertStateChange,
        popBackStack = popBackStack,
        onCloseClick = chatViewModel::onCloseClick,
        onTextChange2 = chatViewModel::onTextChange2,
        onTextChange3 = chatViewModel::onTextChange3,
        onNeighborInformationClick = chatViewModel::onNeighborInformationClick,
        onChatDeleteClick = chatViewModel::onChatDeleteClick,
        onAnonymousChange = chatViewModel::onAnonymousChange

    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CommunityScreen(
    situation : String = "",
    patDataList: List<Pat> = emptyList(),
    itemDataList: List<Item> = emptyList(),
    allUserDataList: List<AllUser> = emptyList(),
    clickAllUserData: AllUser = AllUser(),
    clickAllUserWorldDataList: List<String> = emptyList(),
    chatMessages: List<ChatMessage> = emptyList(),
    newChat: String = "",
    userDataList: List<User> = emptyList(),
    alertState: String = "",
    allAreaCount: String = "0",
    dialogState: String = "",
    text2: String = "",
    text3: String = "",
    anonymous: String = "0",

    onSituationChange: (String) -> Unit = {},
    onChatTextChange: (String) -> Unit = {},
    onChatSubmitClick: () -> Unit = {},
    onUserRankClick: (Int) -> Unit = {},
    onBanClick: (Int) -> Unit = {},
    alertStateChange: (String) -> Unit = {},
    popBackStack: () -> Unit = {},
    onCloseClick: () -> Unit = {},
    onTextChange2: (String) -> Unit = {},
    onTextChange3: (String) -> Unit = {},
    onNeighborInformationClick: (String) -> Unit = {},
    onChatDeleteClick: (String) -> Unit = {},
    onAnonymousChange: (String) -> Unit = {}

    ) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("bgm_prefs", Context.MODE_PRIVATE)
    val bgmOn = prefs.getBoolean("bgmOn", true)

    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    when(situation) {
        "chatSubmitCheck" -> SimpleAlertDialog(
            onConfirmClick = {
                onChatSubmitClick()
                onCloseClick()
            },
            onDismissClick = onCloseClick,
            text = "채팅을 전송하겠습니까?\n\n부적절한 발언이나 도배 시 경고없이 제제를 받을 수 있습니다. 전체 이용가인 만큼 따뜻한 채팅 부탁드립니다.",
        )
    }

    if(clickAllUserData.tag != "0") {
        AppBgmManager.pause()
        CommunityUserDialog(
            onClose = { onUserRankClick(0) },
            clickAllUserData = clickAllUserData,
            clickAllUserWorldDataList = clickAllUserWorldDataList,
            patDataList = patDataList,
            itemDataList = itemDataList,
            onLikeClick = {
            },
            onBanClick = {
                alertStateChange("-1")
            },
            allUserDataList = allUserDataList,
            allMapCount = allAreaCount,
            onPrivateChatClick = {
            }
        )
    } else {
        if (bgmOn) {
            AppBgmManager.play()
        }
    }

    if(alertState != "") {
        SimpleAlertDialog(
            onConfirmClick = {
                onBanClick(alertState.toInt())
                alertStateChange("")
            },
            onDismissClick = { alertStateChange("") },
            text = "신고하시겠습니까?"
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {

        BackGroundImage()

        Box(
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp)
                    ,
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "채팅",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
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

                Text(
                    text = "친구를 만들어보세요! 이름을 클릭하면 정보를 볼 수 있어요",
                    style = MaterialTheme.typography.labelMedium
                )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp)
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

                        val authTag = userDataList.find { it.id == "auth" }?.value2

                        if(chatMessages.isNotEmpty()){
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(start = 6.dp, end = 6.dp, top = 12.dp),
                                reverseLayout = true,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                itemsIndexed(chatMessages.reversed()) { index, message ->

                                    val isAsk = message.tag == "2"
                                    val isNotice = message.tag == "3"

                                    // 공지 여부 확인
                                    val isMine = authTag != null &&
                                            !isNotice && !isAsk &&
                                            message.tag == authTag

                                    val alignment = when {
                                        isNotice -> Arrangement.Center // 공지는 가운데 정렬
                                        isAsk -> Arrangement.Center
                                        isMine -> Arrangement.End
                                        else -> Arrangement.Start
                                    }

                                    val bubbleColor = if(message.anonymous == "0") getPastelColorForTag(message.tag) else Color(
                                        0xFFFFFFFF
                                    )

                                    val textColor = Color.Black

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

                                    // 공지일 경우 전체 Row
                                    when (message.tag) {
                                        "2" -> {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp)
                                                    .background(
                                                        Color(0xFFFFA8A8),
                                                        RoundedCornerShape(12.dp)
                                                    ) // 파스텔 레드 배경
                                                    .padding(2.dp)
                                            ) {

                                                // 상단 공지 배너
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Color(0xFFFF6F6F),
                                                            RoundedCornerShape(
                                                                topStart = 12.dp,
                                                                topEnd = 12.dp
                                                            )
                                                        )
                                                        .padding(vertical = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "📢 공지사항",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = Color.White
                                                    )
                                                }

                                                // 실제 메시지 박스
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Color.White,
                                                            RoundedCornerShape(
                                                                bottomStart = 12.dp,
                                                                bottomEnd = 12.dp
                                                            )
                                                        )
                                                        .padding(12.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = message.uid,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                        textAlign = TextAlign.Center,
                                                        color = Color(0xFF7A0000) // 진한 레드 글씨
                                                    )
                                                }
                                            }

                                        }
                                        "3" -> {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp)
                                                    .background(
                                                        Color(0xFFAED9FF),
                                                        RoundedCornerShape(12.dp)
                                                    ) // 파스텔 파랑 테두리 느낌
                                                    .padding(2.dp)
                                            ) {

                                                // 상단 도란도란 타이틀 영역
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Color(0xFF7CC8FF),
                                                            RoundedCornerShape(
                                                                topStart = 12.dp,
                                                                topEnd = 12.dp
                                                            )
                                                        )
                                                        .padding(vertical = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "💬 도란도란",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = Color.White
                                                    )
                                                }

                                                // 메시지 본문 박스
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Color.White,
                                                            RoundedCornerShape(
                                                                bottomStart = 12.dp,
                                                                bottomEnd = 12.dp
                                                            )
                                                        )
                                                        .padding(
                                                            top = 12.dp,
                                                            start = 12.dp,
                                                            end = 12.dp,
                                                            bottom = 3.dp
                                                        ),
                                                ) {
                                                    Text(
                                                        text = message.uid,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(bottom = 6.dp),
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                        textAlign = TextAlign.Center,
                                                        color = Color(0xFF004E7A) // 진한 파랑
                                                    )

                                                    // 하단 안내문
                                                    Text(
                                                        text = "채팅으로 자유롭게 답변해주세요!",
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 6.dp),
                                                        textAlign = TextAlign.Center,
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF4A6FA5))
                                                    )
                                                }

                                            }

                                        }
                                        else -> {
                                            // 일반 채팅
                                            if (
                                                message.ban == "0" ||
                                                message.tag == authTag
                                            ) {
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
                                                        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                                                    ) {
                                                        Row(

                                                        ) {
                                                            Row(
                                                                modifier = Modifier.clickable {
                                                                    if(message.anonymous == "0"){
                                                                        onNeighborInformationClick(
                                                                            message.tag
                                                                        )
                                                                    }
                                                                }
                                                            ) {
                                                                if(message.anonymous == "0"){
                                                                    Text(
                                                                        text = message.name,
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        modifier = Modifier.padding(
                                                                            start = 4.dp,
                                                                            bottom = 2.dp
                                                                        )
                                                                    )
                                                                    Text(
                                                                        text = "#" + message.tag,
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        modifier = Modifier.padding(
                                                                            start = 4.dp,
                                                                            bottom = 2.dp
                                                                        )
                                                                    )
                                                                } else {
                                                                    Text(
                                                                        text = "익명",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        modifier = Modifier.padding(
                                                                            start = 4.dp,
                                                                            bottom = 2.dp
                                                                        )
                                                                    )
                                                                }
                                                            }

                                                            val time = remember(message.timestamp) {
                                                                SimpleDateFormat(
                                                                    "MM/dd HH:mm",
                                                                    Locale.getDefault()
                                                                )
                                                                    .format(Date(message.timestamp))
                                                            }

                                                            Text(
                                                                text = time,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                modifier = Modifier.padding(
                                                                    start = 4.dp,
                                                                    bottom = 2.dp,
                                                                    end = 4.dp
                                                                )
                                                            )

                                                            if (isMine) {
                                                                Image(
                                                                    painter = painterResource(id = R.drawable.cancel),
                                                                    contentDescription = "별 아이콘",
                                                                    modifier = Modifier
                                                                        .rotate(270f)
                                                                        .size(10.dp)
                                                                        .clickable(
                                                                            indication = null, // ← ripple 효과 제거
                                                                            interactionSource = remember { MutableInteractionSource() } // ← 필수
                                                                        ) {
                                                                            onChatDeleteClick(
                                                                                message.timestamp.toString()
                                                                            )
                                                                        }
                                                                )
                                                            } else {
                                                                JustImage(
                                                                    filePath = "etc/ban.png",
                                                                    modifier = Modifier
                                                                        .size(10.dp)
                                                                        .clickable {
                                                                            alertStateChange(
                                                                                index.toString()
                                                                            )
                                                                        }
                                                                )
                                                            }
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    bubbleColor,
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .padding(8.dp)
                                                        ) {
                                                            Text(text = message.message)
                                                        }
                                                    }
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
                            Box (
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = "익명",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                
                                Checkbox(
                                    checked = anonymous == "1",
                                    onCheckedChange = {
                                        onAnonymousChange(if (it) "1" else "0")
                                    }
                                )

                            }

                            TextField(
                                value = newChat,
                                onValueChange = onChatTextChange,
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
                                    )
                                ,
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
                                        onSituationChange("chatSubmitCheck")
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
fun CommunityScreenPreview() {
    MypatTheme {
        CommunityScreen(
            userDataList = listOf(User(id = "auth", value2 = "1")),
            situation = "",
//            chatMessages = emptyList()
            chatMessages = listOf(ChatMessage(10202020, "a", "a", tag = "13", ban = "0", uid = "hello", anonymous = "1"), ChatMessage(10202020, "a", "a", tag = "1", ban = "0", uid = "hello"), ChatMessage(10202020, "a11", "a11", tag = "2", ban = "0", uid = "assssssssssssssssssssssssssssssssssssssds".repeat(5)), ChatMessage(10202020, "a11", "a11", tag = "3", ban = "0", uid = "adssssssssssssssssssssssssssssssssssssssssssssssssssss".repeat(5)))
        )
    }
}

fun getPastelColorForTag(tag: String): Color {
    val hash = abs(tag.hashCode())

    // Hue: 0~360도 사이 값 생성 (hash 기반)
    val hue = (hash % 360).toFloat()

    // Pastel 톤 유지: Saturation 낮게, Value 높게
    val saturation = 0.35f   // 부드러운 파스텔
    val value = 0.95f        // 밝은 느낌 유지

    val hsv = floatArrayOf(hue, saturation, value)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

