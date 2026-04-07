package com.a0100019.mypat.presentation.main.operator

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.data.room.allUser.AllUser
import com.a0100019.mypat.data.room.item.Item
import com.a0100019.mypat.data.room.pat.Pat
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.trash.AppBgmManager
import com.a0100019.mypat.presentation.neighbor.community.CommunityUserDialog
import com.a0100019.mypat.presentation.main.mainDialog.SimpleAlertDialog
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun OperatorScreen(
    operatorViewModel: OperatorViewModel = hiltViewModel(),
    popBackStack: () -> Unit = {}

) {

    val operatorState : OperatorState = operatorViewModel.collectAsState().value

    val context = LocalContext.current

    operatorViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is OperatorSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
        }
    }

    CommunityScreen(
        situation = operatorState.situation,
        patDataList = operatorState.patDataList,
        itemDataList = operatorState.itemDataList,
        allUserDataList = operatorState.allUserDataList,
        clickAllUserData = operatorState.clickAllUserData,
        clickAllUserWorldDataList = operatorState.clickAllUserWorldDataList,
        allUserRankDataList = operatorState.allUserRankDataList,
        text1 = operatorState.text1,
        userDataList = operatorState.userDataList,
        alertState = operatorState.alertState,
        allAreaCount = operatorState.allAreaCount,
        dialogState = operatorState.dialogState,
        text2 = operatorState.text2,
        text3 = operatorState.text3,
        askMessages = operatorState.askMessages,
        text4 = operatorState.text4,
        text5 = operatorState.text5,

        onSituationChange = operatorViewModel::onSituationChange,
        onChatTextChange = operatorViewModel::onTextChange,
        onUserRankClick = operatorViewModel::onUserRankClick,
        alertStateChange = operatorViewModel::alertStateChange,
        popBackStack = popBackStack,
        onAskChatWrite = operatorViewModel::onAskChatWrite,
        onDialogChangeClick = operatorViewModel::onDialogChangeClick,
        onNoticeChatWrite = operatorViewModel::onNoticeChatWrite,
        onCloseClick = operatorViewModel::onCloseClick,
        onTextChange2 = operatorViewModel::onTextChange2,
        onTextChange3 = operatorViewModel::onTextChange3,
        onTextChange4 = operatorViewModel::onTextChange4,
        onTextChange5 = operatorViewModel::onTextChange5,
        onOperatorChatSubmitClick = operatorViewModel::onOperatorChatSubmitClick,
        onAskClick = operatorViewModel::onAskClick,
        onLetterSubmitClick = operatorViewModel::onOperatorLetterSubmitClick,
        onRankUpdate = operatorViewModel::onRankUpdate,
        onAllUserDataGet = operatorViewModel::onAllUserDataGet

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    situation : String = "chat",
    patDataList: List<Pat> = emptyList(),
    itemDataList: List<Item> = emptyList(),
    allUserDataList: List<AllUser> = emptyList(),
    clickAllUserData: AllUser = AllUser(),
    clickAllUserWorldDataList: List<String> = emptyList(),
    allUserRankDataList: List<AllUser> = listOf(AllUser(), AllUser()),
    askMessages: List<OperatorMessage> = emptyList(),
    text1: String = "",
    userDataList: List<User> = emptyList(),
    alertState: String = "",
    allAreaCount: String = "0",
    dialogState: String = "",
    text2: String = "",
    text3: String = "",
    text4: String = "",
    text5: String = "",

    onPageUpClick: () -> Unit = {},
    onSituationChange: (String) -> Unit = {},
    onChatTextChange: (String) -> Unit = {},
    onUserRankClick: (Int) -> Unit = {},
    alertStateChange: (String) -> Unit = {},
    popBackStack: () -> Unit = {},
    onDialogChangeClick: (String) -> Unit = {},
    onAskChatWrite: () -> Unit = {},
    onNoticeChatWrite: () -> Unit = {},
    onCloseClick: () -> Unit = {},
    onTextChange2: (String) -> Unit = {},
    onTextChange3: (String) -> Unit = {},
    onTextChange4: (String) -> Unit = {},
    onTextChange5: (String) -> Unit = {},
    onOperatorChatSubmitClick: () -> Unit = {},
    onAskClick: (String) -> Unit = {},
    onLetterSubmitClick: () -> Unit = {},
    onRankUpdate: () -> Unit = {},
    onAllUserDataGet: () -> Unit = {}

    ) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("bgm_prefs", Context.MODE_PRIVATE)
    val bgmOn = prefs.getBoolean("bgmOn", true)

    when(dialogState) {
        "askView" -> OperatorAskViewDialog(
            onClose = onCloseClick,
            onAskClick = onAskClick,
            askMessages = askMessages
        )
        "askWrite" -> OperatorAskWriteDialog(
            onClose = onCloseClick,
            onTextChange = onChatTextChange,
            text = text1,
            onConfirmClick = onAskChatWrite,
        )
        "notice" -> OperatorNoticeDialog(
            onClose = onCloseClick,
            onTextChange = onChatTextChange,
            text = text1,
            onConfirmClick = onNoticeChatWrite,
        )
        "operatorChat" -> OperatorChatDialog(
            onClose = onCloseClick,
            onTextChange = onChatTextChange,
            onTextChange2 = onTextChange2,
            onTextChange3 = onTextChange3,
            text2 = text2,
            text3 = text3,
            text = text1,
            onOperatorChatSubmitClick = onOperatorChatSubmitClick
        )
        "letter" -> OperatorLetterDialog(
            onClose = onCloseClick,
            onTextChange = onChatTextChange,
            onTextChange2 = onTextChange2,
            onTextChange3 = onTextChange3,
            onTextChange4 = onTextChange4,
            onTextChange5 = onTextChange5,
            text2 = text2,
            text3 = text3,
            text4 = text4,
            text5 = text5,
            text = text1,
            onConfirmClick = onLetterSubmitClick
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
            allMapCount = allAreaCount
        )
    } else {
        if (bgmOn) {
            AppBgmManager.play()
        }
    }

    if(alertState != "") {
        SimpleAlertDialog(
            onConfirmClick = {
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
            modifier = Modifier.padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 오른쪽 버튼
                MainButton(
                    text = "닫기",
                    onClick = popBackStack,
                )

                Spacer(modifier = Modifier.size(30.dp))

                Column {
                    MainButton(
                        text = "도란도란",
                        onClick = { onDialogChangeClick("askView") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                    MainButton(
                        text = "공지",
                        onClick = { onDialogChangeClick("notice") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                    MainButton(
                        text = "채팅",
                        onClick = { onDialogChangeClick("operatorChat") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                    MainButton(
                        text = "편지",
                        onClick = { onDialogChangeClick("letter") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                    MainButton(
                        text = "모든 유저 데이터 가져오기",
                        onClick = { onAllUserDataGet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                    MainButton(
                        text = "랭킹 업데이트",
                        onClick = { onRankUpdate() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )

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
            userDataList = listOf(User(id = "auth")),
            situation = "chat",
//            chatMessages = emptyList()
        )
    }
}
