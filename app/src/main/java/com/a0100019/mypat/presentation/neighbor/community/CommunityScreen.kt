package com.a0100019.mypat.presentation.neighbor.community

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CommunityScreen(
    communityViewModel: CommunityViewModel = hiltViewModel(),
    popBackStack: () -> Unit = {},
    onNavigateToNeighborInformationScreen: () -> Unit = {},

) {

    val communityState : CommunityState = communityViewModel.collectAsState().value

    val context = LocalContext.current

    communityViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CommunitySideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            CommunitySideEffect.NavigateToNeighborInformationScreen -> onNavigateToNeighborInformationScreen()
        }
    }

    CommunityScreen(
        situation = communityState.situation,
        patDataList = communityState.patDataList,
        itemDataList = communityState.itemDataList,
        allUserDataList = communityState.allUserDataList,
        allUserData1 = communityState.allUserData1,
        allUserData2 = communityState.allUserData2,
        allUserData3 = communityState.allUserData3,
        allUserData4 = communityState.allUserData4,
        allUserWorldDataList1 = communityState.allUserWorldDataList1,
        allUserWorldDataList2 = communityState.allUserWorldDataList2,
        allUserWorldDataList3 = communityState.allUserWorldDataList3,
        allUserWorldDataList4 = communityState.allUserWorldDataList4,
        chatMessages = communityState.chatMessages,
        newChat = communityState.newChat,
        userDataList = communityState.userDataList,
        allAreaCount = communityState.allAreaCount,
        text2 = communityState.text2,
        text3 = communityState.text3,
        firstGameRankList = communityState.firstGameRankList,
        secondGameRankList = communityState.secondGameRankList,
        thirdGameEasyRankList = communityState.thirdGameEasyRankList,
        thirdGameNormalRankList = communityState.thirdGameNormalRankList,
        thirdGameHardRankList = communityState.thirdGameHardRankList,

        onRandomGetAllUser = communityViewModel::randomGetAllUser,
        onSituationChange = communityViewModel::onSituationChange,
        onChatTextChange = communityViewModel::onChatTextChange,
        popBackStack = popBackStack,
        onDialogChangeClick = communityViewModel::onDialogChangeClick,
        onNeighborInformationClick = communityViewModel::onNeighborInformationClick,
        onCloseClick = communityViewModel::onCloseClick,
        onTextChange2 = communityViewModel::onTextChange2,
        onTextChange3 = communityViewModel::onTextChange3,
        onUserWorldClick = communityViewModel::onUserWorldClick,
        onPageUpClick = communityViewModel::opPageUpClick

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    situation : String = "world",
    patDataList: List<Pat> = emptyList(),
    itemDataList: List<Item> = emptyList(),
    allUserDataList: List<AllUser> = emptyList(),
    allUserData1: AllUser = AllUser(),
    allUserData2: AllUser = AllUser(),
    allUserData3: AllUser = AllUser(),
    allUserData4: AllUser = AllUser(),
    allUserWorldDataList1: List<String> = emptyList(),
    allUserWorldDataList2: List<String> = emptyList(),
    allUserWorldDataList3: List<String> = emptyList(),
    allUserWorldDataList4: List<String> = emptyList(),
    chatMessages: List<ChatMessage> = emptyList(),
    newChat: String = "",
    userDataList: List<User> = emptyList(),
    alertState: String = "",
    allAreaCount: String = "0",
    dialogState: String = "",
    text2: String = "",
    text3: String = "",
    firstGameRankList: List<Rank> = emptyList(),
    secondGameRankList: List<Rank> = emptyList(),
    thirdGameEasyRankList: List<Rank> = emptyList(),
    thirdGameNormalRankList: List<Rank> = emptyList(),
    thirdGameHardRankList: List<Rank> = emptyList(),

    onRandomGetAllUser: () -> Unit = {},
    onSituationChange: (String) -> Unit = {},
    onChatTextChange: (String) -> Unit = {},
    alertStateChange: (String) -> Unit = {},
    popBackStack: () -> Unit = {},
    onDialogChangeClick: (String) -> Unit = {},
    onCloseClick: () -> Unit = {},
    onNeighborInformationClick: (String) -> Unit = {},
    onTextChange2: (String) -> Unit = {},
    onTextChange3: (String) -> Unit = {},
    onUserWorldClick: (Int) -> Unit = {},
    onPageUpClick: () -> Unit = {}
    ) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("bgm_prefs", Context.MODE_PRIVATE)
    val bgmOn = prefs.getBoolean("bgmOn", true)

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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 6.dp, bottom = 6.dp, top = 5.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = when (situation) {
                            "world" -> "이웃 마을"
                            "firstGame" -> "컬링"
                            "secondGame" -> "1to50"
                            "thirdGameEasy" -> "스도쿠 - 쉬움"
                            "thirdGameNormal" -> "스도쿠 - 보통"
                            "thirdGameHard" -> "스도쿠 - 어려움"
                            else -> "로딩중.."
                        },
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
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

                when (situation) {
                    "world" -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //  위쪽 두 Row: 남는 공간 꽉 채우기
                        Column(
                            modifier = Modifier
                                .weight(1f) // 남는 공간 전부 차지
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.SpaceEvenly // 위아래 균등 분배
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                CommunityWorldCard(
                                    modifier = Modifier.weight(1f),
                                    userData = allUserData1,
                                    worldDataList = allUserWorldDataList1,
                                    patDataList = patDataList,
                                    itemDataList = itemDataList,
                                    onClick = { onUserWorldClick(1) }
                                )
                                CommunityWorldCard(
                                    modifier = Modifier.weight(1f),
                                    userData = allUserData2,
                                    worldDataList = allUserWorldDataList2,
                                    patDataList = patDataList,
                                    itemDataList = itemDataList,
                                    onClick = { onUserWorldClick(2) }
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CommunityWorldCard(
                                    modifier = Modifier.weight(1f),
                                    userData = allUserData3,
                                    worldDataList = allUserWorldDataList3,
                                    patDataList = patDataList,
                                    itemDataList = itemDataList,
                                    onClick = { onUserWorldClick(3) }
                                )
                                CommunityWorldCard(
                                    modifier = Modifier.weight(1f),
                                    userData = allUserData4,
                                    worldDataList = allUserWorldDataList4,
                                    patDataList = patDataList,
                                    itemDataList = itemDataList,
                                    onClick = { onUserWorldClick(4) }
                                )
                            }
                        }

                        //  맨 아래 Row: 항상 고정
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 40.dp)
                        ) {
                            Text("   ")

                            Text(
                                text = "로딩 중..",
                                textAlign = TextAlign.Center
                            )

                            MainButton(
                                onClick = onPageUpClick,
                                text = "다음"
                            )
                        }
                    }

                    else -> LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp) // 카드 간 간격
                    ) {

                        item {
                            Text(
                                text = "순위는 하루에 한 번 업데이트 됩니다.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        val rankList = when (situation) {
                            "firstGame" -> firstGameRankList
                            "secondGame" -> secondGameRankList
                            "thirdGameEasy" -> thirdGameEasyRankList
                            "thirdGameNormal" -> thirdGameNormalRankList
                            "thirdGameHard" -> thirdGameHardRankList
                            else -> emptyList()
                        }

                        itemsIndexed(rankList) { index, user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(0.2f)
                                )

                                CommunityRankingCard(
                                    name = user.name,
                                    tag = user.tag,
                                    score = user.score,
                                    rank = index + 1,
                                    situation = situation,
                                    onClick = { onNeighborInformationClick(user.tag) },
                                    modifier = Modifier
                                        .weight(0.9f)
                                )
                            }
                        }
                    }

                }

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val thirdGameKeys = listOf("thirdGameEasy", "thirdGameNormal", "thirdGameHard")

                if (situation in thirdGameKeys) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.5f),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        MainButton(
                            onClick = { onSituationChange("thirdGameEasy") },
                            text = "쉬움",
                            imageSize = 16.dp,
                            iconResId = if (situation == "thirdGameEasy") R.drawable.check else null,
                            modifier = Modifier.weight(1f)
                        )

                        MainButton(
                            onClick = { onSituationChange("thirdGameNormal") },
                            text = "보통",
                            imageSize = 16.dp,
                            iconResId = if (situation == "thirdGameNormal") R.drawable.check else null,
                            modifier = Modifier.weight(1f)
                        )

                        MainButton(
                            onClick = { onSituationChange("thirdGameHard") },
                            text = "어려움",
                            imageSize = 16.dp,
                            iconResId = if (situation == "thirdGameHard") R.drawable.check else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val buttons = listOf(
                    "마을" to "world",
                    "컬링" to "firstGame",
                    "1to50" to "secondGame",
                    "스도쿠" to "thirdGameEasy", // 대표 키만 지정
                )

//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.Bottom
//                ) {
//                    buttons.forEach { (label, key) ->
//                        val isSelected = when (key) {
//                            "thirdGameEasy" -> false // situation in thirdGameKeys
//                            else -> situation == key
//                        }
//
//                        Surface(
//                            modifier = Modifier.weight(1f),
//                            color = Color.Transparent, // ✅ 배경 투명
//                        ) {
//                            MainButton(
//                                onClick = { onSituationChange(key) },
//                                text = label,
//                                iconResId = if (isSelected) R.drawable.check else null,
//                                imageSize = 16.dp,
//                                modifier = Modifier.fillMaxWidth()
//                            )
//                        }
//                    }
//                }
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
            situation = "world",
            firstGameRankList = listOf(Rank()),
//            chatMessages = emptyList()
            chatMessages = listOf(ChatMessage(10202020, "a", "a", tag = "1", ban = "0", uid = "hello"), ChatMessage(10202020, "a11", "a11", tag = "2", ban = "0", uid = "assssssssssssssssssssssssssssssssssssssds".repeat(5)), ChatMessage(10202020, "a11", "a11", tag = "3", ban = "0", uid = "adssssssssssssssssssssssssssssssssssssssssssssssssssss".repeat(5)))
        )
    }
}