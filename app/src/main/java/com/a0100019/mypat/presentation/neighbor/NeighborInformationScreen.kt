package com.a0100019.mypat.presentation.neighbor

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.data.room.allUser.AllUser
import com.a0100019.mypat.data.room.item.Item
import com.a0100019.mypat.data.room.pat.Pat
import com.a0100019.mypat.presentation.main.management.medalName
import com.a0100019.mypat.presentation.main.management.totalMedalCount
import com.a0100019.mypat.presentation.main.mainDialog.SimpleAlertDialog
import com.a0100019.mypat.presentation.neighbor.chat.getPastelColorForTag
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.component.TextAutoResizeSingleLine
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.image.item.WorldItemImage
import com.a0100019.mypat.presentation.ui.image.pat.PatImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun NeighborInformationScreen(
    neighborInformationViewModel: NeighborInformationViewModel = hiltViewModel(),
    onNavigateToPrivateRoomScreen: () -> Unit = {},

    popBackStack: () -> Unit = {},
) {

    val neighborInformationState : NeighborInformationState = neighborInformationViewModel.collectAsState().value

    val context = LocalContext.current
    val activity = context as Activity

    neighborInformationViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is NeighborInformationSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            NeighborInformationSideEffect.NavigateToPrivateRoomScreen -> onNavigateToPrivateRoomScreen()

            NeighborInformationSideEffect.ShowRewardAd -> {
                neighborInformationViewModel.showRewardAd(activity)
            }
        }
    }

    NeighborInformationScreen(
        clickAllUserData = neighborInformationState.clickAllUserData,
        clickAllUserWorldDataList = neighborInformationState.clickAllUserWorldDataList,
        patDataList = neighborInformationState.patDataList,
        itemDataList = neighborInformationState.itemDataList,
        allMapCount = neighborInformationState.allAreaCount,
        situation = neighborInformationState.situation,

        onClose = neighborInformationViewModel::onClose,
        popBackStack = popBackStack,
        onLikeClick = neighborInformationViewModel::onLikeClick,
        onBanClick = neighborInformationViewModel::onBanClick,
        onPrivateChatStartClick = neighborInformationViewModel::onPrivateChatStartClick,
        onSituationChange = neighborInformationViewModel::onSituationChange,
        onAdClick = neighborInformationViewModel::onAdClick,
        onRewardEarned = neighborInformationViewModel::onRewardEarned

    )
}

@Composable
fun NeighborInformationScreen(
    clickAllUserData: AllUser = AllUser(),
    clickAllUserWorldDataList: List<String> = emptyList(),
    patDataList: List<Pat> = emptyList(),
    itemDataList: List<Item> = emptyList(),
    allMapCount: String = "0",
    situation: String = "",

    onClose : () -> Unit = {},
    popBackStack: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onBanClick: (Int) -> Unit = {},
    onPrivateChatStartClick: () -> Unit = {},
    onSituationChange: (String) -> Unit = {},
    onAdClick: () -> Unit = {},
    onRewardEarned: () -> Unit = {}

    ) {

    var page by remember { mutableIntStateOf(0) }

    when(situation) {
        "privateChat" -> SimpleAlertDialog(
            onConfirmClick = onRewardEarned,
            onDismissClick = onClose,
            text = "친구가 되어 이야기를 나눠보세요.\n\n친구가 되겠습니까?"
        )
        "selfFriend" -> SimpleAlertDialog(
            onConfirmClick = {onSituationChange("")},
            onDismissOn = false,
            text = "나와는 친구가 될 수 없습니다 ㅠ.ㅠ\n칭호를 획득했습니다!"
        )
        "medalQuestion" -> SimpleAlertDialog(
            onConfirmClick = {onSituationChange("")},
            onDismissOn = false,
            text = "하루마을 곳곳에 숨어있는 칭호를 찾아보세요!\n내가 획득한 칭호는 클릭하여 정보를 확인할 수 있습니다."
        )
    }

    //빈 데이터일 경우
    if(situation == "loading") {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            MainButton(
                text = "닫기",
                onClick = popBackStack,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            Text(
                text = "로딩 중..",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }

    } else {
        val introduction =
            clickAllUserData
                .warning
                .split("@")
                .first()

        val medalList: List<Int> =
            clickAllUserData
                .warning
                .split("@")
                .last()
                .split("/")                  // ["1","3","12","5","0","3"]
                .mapNotNull { it.toIntOrNull() }
                .filter { it != 0 }          // "0" 제거
                .distinct()                  // 중복 제거

//        MusicPlayer(
//            music = clickAllUserData.area
//        )

        Surface (
            modifier = Modifier
                .fillMaxSize()
            ,
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF8E7),
            border = BorderStroke(2.dp, Color(0xFF5A3A22)),
            shadowElevation = 8.dp,
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 이름, 좋아요
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                    ,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = clickAllUserData.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .padding(start = 10.dp, end = 6.dp)
                        )
                        Text(
                            text = "#${clickAllUserData.tag}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    JustImage(
                        filePath = "etc/like.png",
                        modifier = Modifier
                            .size(15.dp)
                    )
                    Text(
                        text = " ${clickAllUserData.like}",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .padding(end = 10.dp)
                    )
                    JustImage(
                        filePath = "etc/exit.png",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                popBackStack()
                            }
                    )
                }

                if (page == 0) {

                    // ✨ 반짝임 애니메이션
                    val shimmerX by rememberInfiniteTransition(label = "shimmer").animateFloat(
                        initialValue = -0.4f,
                        targetValue = 1.4f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 2200, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "shimmerX"
                    )

// 🌸 빨강 파스텔 팔레트
                    val pastelTop = Color(0xFFFFE3E3)      // 아주 연한 로즈 레드
                    val pastelBottom = Color(0xFFFFC1C1)   // 부드러운 코랄 레드
                    val strongBorderColor = Color(0xFFE57373) // 쨍하지만 과하지 않은 레드
                    val shimmerColor = Color.White.copy(alpha = 0.5f)


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp) // 🎖️ 배너 높이 고정
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        pastelTop,
                                        pastelTop
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = strongBorderColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {

                        // ✨ 반짝임 레이어 (유리 느낌)
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colorStops = arrayOf(
                                            (shimmerX - 0.18f) to Color.Transparent,
                                            shimmerX to shimmerColor,
                                            (shimmerX + 0.18f) to Color.Transparent
                                        )
                                    )
                                )
                        )

                        val medal = medalList.firstOrNull()

                        Text(
                            text = when (medal) {
                                null -> "칭호 없음"
                                0 -> "칭호 없음"
                                else -> medalName(medal)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF6B1F1F),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )



                    }


                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f / 1.25f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF8E7),
                        border = BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primaryContainer
                        ),
//                    shadowElevation = 8.dp,
                    ) {

                        JustImage(
                            filePath = clickAllUserData.area,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            val density = LocalDensity.current

                            // Surface 크기 가져오기 (px → dp 변환)
                            val surfaceWidth = constraints.maxWidth
                            val surfaceHeight = constraints.maxHeight

                            val surfaceWidthDp = with(density) { surfaceWidth.toDp() }
                            val surfaceHeightDp = with(density) { surfaceHeight.toDp() }

                            clickAllUserWorldDataList.forEach { data ->

                                val parts = data.split("@")

                                //  최소 필요한 개수 체크 (0~5 → 총 6개)
                                if (parts.size < 6) return@forEach

                                val id = parts.getOrNull(0) ?: return@forEach
                                val size = parts.getOrNull(1)?.toFloatOrNull() ?: return@forEach
                                val type = parts.getOrNull(2) ?: return@forEach
                                val x = parts.getOrNull(3)?.toFloatOrNull() ?: return@forEach
                                val y = parts.getOrNull(4)?.toFloatOrNull() ?: return@forEach
                                val effect = parts.getOrNull(5)?.toIntOrNull() ?: 0

                                if (type == "pat") {
                                    // 🐾 pat 처리
                                    val patData = patDataList.find { it.id.toString() == id } ?: return@forEach

                                    PatImage(
                                        patUrl = patData.url,
                                        surfaceWidthDp = surfaceWidthDp,
                                        surfaceHeightDp = surfaceHeightDp,
                                        xFloat = x,
                                        yFloat = y,
                                        sizeFloat = size,
                                        effect = effect,
                                        onClick = { }
                                    )

                                } else {
                                    //  item 처리
                                    val itemData = itemDataList.find { it.id.toString() == id } ?: return@forEach

                                    WorldItemImage(
                                        itemUrl = itemData.url,
                                        surfaceWidthDp = surfaceWidthDp,
                                        surfaceHeightDp = surfaceHeightDp,
                                        xFloat = x,
                                        yFloat = y,
                                        sizeFloat = size,
                                    )
                                }
                            }

                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp), // ⭐ 3줄 정도 들어가는 고정 높이
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 2.dp,
                            color = Color(0xFFEAF2FF), // 💠 연한 파스텔 블루 배경
                            border = BorderStroke(
                                width = 2.dp,
                                color = Color(0xFF6FA8DC) // 🔷 선명하지만 부드러운 블루
                            )
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = if (introduction == "0" || introduction == "") {
                                        "안녕하세요 :)"
                                    } else {
                                        introduction
                                    },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF1F4E79), // 🌊 가독성 좋은 딥블루
                                    maxLines = 3
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row {
                            Text(
                                text = "시작일",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                            )
                            Text(
                                text = clickAllUserData.firstDate,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                            )
                        }

                        Text(
                            text = "칭호 ${medalList.size}/${totalMedalCount()}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(end = 6.dp)
                        )

                        Row {
                            Text(
                                text = "접속일",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                            )
                            Text(
                                text = "${clickAllUserData.totalDate}일",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                            )
                        }

                    }


                } else {
                    // 상세 페이지 aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa

                    Spacer(modifier = Modifier.size(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.size(20.dp))
                        Text(
                            text = "칭호 ${medalList.size}/${totalMedalCount()}",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        JustImage(
                            filePath = "etc/question.png",
                            modifier = Modifier
                                .size(15.dp)
                                .clickable {
                                    onSituationChange("medalQuestion")
                                }
                        )
                    }

                    Spacer(modifier = Modifier.size(12.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(start = 6.dp, end = 6.dp, bottom = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFFFF9ED),
                        border = BorderStroke(2.dp, Color(0xFFE6D7B9)),
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            items(totalMedalCount()) { index ->

                                val medalType = index + 1
                                val isOwned = medalList.contains(medalType)

                                val bubbleColor = getPastelColorForTag((index * 16).toString())

                                // ✨ 반짝임 애니메이션
                                val shimmerX by rememberInfiniteTransition(label = "shimmer").animateFloat(
                                    initialValue = -0.4f,
                                    targetValue = 1.4f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(durationMillis = 2200, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "shimmerX"
                                )

//  획득용 파스텔 베이스
                                val pastelBase = lerp(
                                    bubbleColor,
                                    Color.White,
                                    0.6f
                                )

//  테두리용 "쨍한" 컬러 (핵심 포인트)
                                val strongBorderColor = lerp(
                                    bubbleColor,
                                    Color.Black,
                                    0.15f        // 살짝만 어둡게 → 채도 유지 + 선명
                                )

// ✨ 반짝임 색
                                val shimmerColor = Color.White.copy(alpha = 0.45f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f / 0.47f)
                                        .background(
                                            brush = if (isOwned) {
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        pastelBase.copy(alpha = 0.95f),
                                                        pastelBase.copy(alpha = 0.75f)
                                                    )
                                                )
                                            } else {
                                                //  미획득 → 완전 회색 통일
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFFF1F1F1),
                                                        Color(0xFFF1F1F1)
                                                    )
                                                )
                                            },
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .border(
                                            width = if (isOwned) 0.8.dp else 0.4.dp,
                                            color = if (isOwned)
                                                strongBorderColor      // ⭐ 쨍한 테두리
                                            else
                                                Color(0xFFD0D0D0),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .padding(horizontal = 2.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {

                                    // ✨ 반짝임 (획득만)
                                    if (isOwned) {
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colorStops = arrayOf(
                                                            (shimmerX - 0.18f) to Color.Transparent,
                                                            shimmerX to shimmerColor,
                                                            (shimmerX + 0.18f) to Color.Transparent
                                                        )
                                                    )
                                                )
                                        )
                                    }

                                    TextAutoResizeSingleLine(
                                        text = medalName(medalType),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }

                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(12.dp))

                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 2.dp,
                            color = MaterialTheme.colorScheme.scrim
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "도감",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier
                                        .padding(bottom = 6.dp)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Row {
                                        Text(
                                            text = "펫",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
                                        Text(
                                            text = "${clickAllUserData.openPat}/${patDataList.size}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Row {
                                        Text(
                                            text = "아이템",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
                                        Text(
                                            text = "${clickAllUserData.openItem.toInt() - 20}/${itemDataList.size - 20}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Row {
                                        Text(
                                            text = "맵",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
                                        Text(
                                            text = "${clickAllUserData.openArea}/${allMapCount}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Divider(
                                    color = Color.LightGray,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(
                                        start = 8.dp,
                                        end = 8.dp,
                                        top = 8.dp,
                                        bottom = 8.dp
                                    )
                                )

                                Text(
                                    text = "게임",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier
                                        .padding(bottom = 6.dp)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Row {
                                        Text(
                                            text = "컬링",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
                                        Text(
                                            text = clickAllUserData.firstGame + "점",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
//                                        val firstGameRank = allUserDataList
//                                            .map { it.firstGame }        // 점수만 추출
//                                            .sortedDescending()          // 높은 점수 순으로 정렬
//                                            .count { it.toInt() > clickAllUserData.firstGame.toInt() } + 1  // myScore보다 작거나 같은 첫 점수의 순위
//                                        Text(
//                                            text = firstGameRank.toString() + "등",
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
                                    }

                                    Row {
                                        Text(
                                            text = "1to50",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )

                                        val secondGameTime = clickAllUserData.secondGame

                                        Text(
                                            text = if (secondGameTime != "100000") {
                                                secondGameTime
                                            } else {
                                                "-"
                                            } + "초",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
//                                        val secondGameRank = allUserDataList
//                                            .map { it.secondGame }        // 점수만 추출
//                                            .sortedDescending()          // 높은 점수 순으로 정렬
//                                            .count { it.toDouble() < clickAllUserData.secondGame.toDouble() } + 1  // myScore보다 작거나 같은 첫 점수의 순위
//                                        Text(
//                                            text = secondGameRank.toString() + "등",
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
                                    }

                                }

                                Divider(
                                    color = Color.LightGray,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
                                )

                                Text(
                                    text = "스도쿠",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .padding(top = 8.dp, bottom = 6.dp)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Row {
                                        Text(
                                            text = "쉬움",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
                                        Text(
                                            text = clickAllUserData.thirdGameEasy + "개",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
//                                        val thirdGameEasyRank = allUserDataList
//                                            .map { it.thirdGameEasy }        // 점수만 추출
//                                            .sortedDescending()          // 높은 점수 순으로 정렬
//                                            .count { it.toInt() > clickAllUserData.thirdGameEasy.toInt() } + 1  // myScore보다 작거나 같은 첫 점수의 순위
//                                        Text(
//                                            text = thirdGameEasyRank.toString() + "등",
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
                                    }

                                    Row {
                                        Text(
                                            text = "보통",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
                                        Text(
                                            text = clickAllUserData.thirdGameNormal + "개",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
//                                        val thirdGameNormalRank = allUserDataList
//                                            .map { it.thirdGameNormal }        // 점수만 추출
//                                            .sortedDescending()          // 높은 점수 순으로 정렬
//                                            .count { it.toInt() > clickAllUserData.thirdGameNormal.toInt() } + 1  // myScore보다 작거나 같은 첫 점수의 순위
//                                        Text(
//                                            text = thirdGameNormalRank.toString() + "등",
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
                                    }

                                    Row {
                                        Text(
                                            text = "어려움",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
                                        Text(
                                            text = clickAllUserData.thirdGameHard + "개",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
//                                        val thirdGameHardRank = allUserDataList
//                                            .map { it.thirdGameHard }        // 점수만 추출
//                                            .sortedDescending()          // 높은 점수 순으로 정렬
//                                            .count { it.toInt() > clickAllUserData.thirdGameHard.toInt() } + 1  // myScore보다 작거나 같은 첫 점수의 순위
//                                        Text(
//                                            text = thirdGameHardRank.toString() + "등",
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
                                    }

                                }

                            }
                        }

                    }

                    Spacer(modifier = Modifier.size(12.dp))

                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 6.dp, top = 6.dp)
                ) {
                    // --- 신고 버튼 ---
                    JustImage(
                        filePath = "etc/ban.png",
                        modifier = Modifier
                            .clickable { onBanClick(-1) }
                            .size(15.dp)
                    )

                    // --- 좋아요 버튼 (하트) ---
                    val interactionLike = remember { MutableInteractionSource() }
                    val isPressedLike by interactionLike.collectIsPressedAsState()
                    val scaleLike by animateFloatAsState(if (isPressedLike) 0.85f else 1f, label = "")

                    Box(
                        modifier = Modifier
                            .graphicsLayer { scaleX = scaleLike; scaleY = scaleLike }
                            .size(50.dp) // 버튼 전체 크기 고정
                            .background(Color(0xFFFFF0F3), CircleShape) // 연한 분홍색 원형 배경
                            .border(1.5.dp, Color(0xFFFFC1CC), CircleShape) // 조금 더 진한 테두리
                            .clickable(
                                interactionSource = interactionLike,
                                indication = null,
                                onClick = onLikeClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        JustImage(
                            filePath = "etc/like.png",
                            modifier = Modifier.size(20.dp) // 아이콘 크기 살짝 조정
                        )
                    }

                    // --- 친구하기 버튼 ---
                    val interactionFriend = remember { MutableInteractionSource() }
                    val isPressedFriend by interactionFriend.collectIsPressedAsState()
                    val scaleFriend by animateFloatAsState(if (isPressedFriend) 0.85f else 1f, label = "")

                    Box(
                        modifier = Modifier
                            .graphicsLayer { scaleX = scaleFriend; scaleY = scaleFriend }
                            .size(50.dp) // 버튼 전체 크기 고정
                            .background(Color(0xFFF0F7FF), CircleShape) // 연한 하늘색 원형 배경
                            .border(1.5.dp, Color(0xFFD0E3FF), CircleShape) // 조금 더 진한 테두리
                            .clickable(
                                interactionSource = interactionFriend,
                                indication = null,
                                onClick = { onSituationChange("privateChat") }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        JustImage(
                            filePath = "etc/friend.png",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // --- 상세/메인 버튼 ---
                    MainButton(
                        text = if (page == 0) "상세" else "메인",
                        onClick = {
                            if (page == 0) page = 1 else page = 0
                        },
                    )
                }

            }

        }
    }

}

@Preview(showBackground = true)
@Composable
fun NeighborInformationScreenPreview() {
    MypatTheme {
        NeighborInformationScreen(
            clickAllUserData = AllUser(
                tag = "22",
                lastLogin = 342112,
                ban = "0",
                like = "54",
                warning = "0",
                firstDate = "1",
                openItem = "30",
                area = "area/forest.jpg",
                name = "이222유빈",
                openPat = "20",
                totalDate = "134",
                worldData = "1@0.2@pat@0.25@0.69/2@0.2@pat@0.25@0.569/1@0.2@pat@0.125@0.69/1@0.2@item@0.25@0.69/2@0.2@item@0.125@0.769/1@0.2@item@0.225@0.1691@0.2@pat@0.25@0.669/2@0.2@pat@0.25@0.369/2@0.3@pat@0.325@0.69/1@0.2@pat@0.725@0.769/1@0.2@item@0.425@0.669",
            ),
        )
    }
}