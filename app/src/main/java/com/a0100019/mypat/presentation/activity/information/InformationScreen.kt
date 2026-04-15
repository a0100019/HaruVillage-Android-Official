package com.a0100019.mypat.presentation.activity.information

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.data.room.item.Item
import com.a0100019.mypat.data.room.area.Area
import com.a0100019.mypat.data.room.pat.Pat
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.world.World
import com.a0100019.mypat.presentation.main.mainDialog.SimpleAlertDialog
import com.a0100019.mypat.presentation.main.management.medalName
import com.a0100019.mypat.presentation.main.management.totalMedalCount
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
fun InformationScreen(
    informationViewModel: InformationViewModel = hiltViewModel(),
    popBackStack: () -> Unit = {}

) {

    val informationState : InformationState = informationViewModel.collectAsState().value

    val context = LocalContext.current

    informationViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is InformationSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
        }
    }

    InformationScreen(
        patDataList = informationState.patDataList,
        itemDataList = informationState.itemDataList,
        areaUrl = informationState.areaData?.value ?: "",
        allPatDataList = informationState.allPatDataList,
        allItemDataList = informationState.allItemDataList,
        allAreaDataList = informationState.allAreaDataList,
        userDataList = informationState.userData,
        gameRankList = informationState.gameRankList,
        worldDataList = informationState.worldDataList,
        text = informationState.text,
        situation = informationState.situation,
        medalExplain = informationState.medalExplain,

        popBackStack = popBackStack,
        onTextChange = informationViewModel::onTextChange,
        onSituationChange = informationViewModel::onSituationChange,
        onClose = informationViewModel::onClose,
        onIntroductionChangeClick = informationViewModel::onIntroductionChangeClick,
        onMedalChangeClick = informationViewModel::onMedalChangeClick,
        onMedalExplainClick = informationViewModel::onMedalExplainClick

        )
}

@SuppressLint("RememberReturnType")
@Composable
fun InformationScreen(
    areaUrl : String,
    patDataList : List<Pat>,
    itemDataList : List<Item>,
    allPatDataList: List<Pat>,
    allItemDataList: List<Item>,
    allAreaDataList: List<Area>,
    worldDataList : List<World> = emptyList(),
    userDataList: List<User>,
    gameRankList: List<String> = listOf("-", "-", "-", "-", "-"),
    text: String = "",
    situation: String = "",
    medalExplain: String = "",

    popBackStack: () -> Unit = {},
    onTextChange: (String) -> Unit = {},
    onSituationChange: (String) -> Unit = {},
    onClose: () -> Unit = {},
    onIntroductionChangeClick: () -> Unit = {},
    onMedalChangeClick: (Int) -> Unit = {},
    onMedalExplainClick: (Int) -> Unit = {}

    ) {

    var page by remember { mutableIntStateOf(0) }

    val myMedalString = userDataList.find { it.id == "etc" }?.value3 ?: ""

    val myMedalList: List<Int> =
        myMedalString
            .split("/")              // ["1","3","12","5"]
            .mapNotNull { it.toIntOrNull() } // [1,3,12,5]

    when(situation) {
        "medal" -> {
            MedalChangeDialog(
                onClose = onClose,
                onMedalClick = onMedalChangeClick,
                userDataList = userDataList
            )
        }
        "introduction" -> {
            IntroductionChangeDialog(
                onClose = onClose,
                onTextChange = onTextChange,
                text = text,
                onConfirmClick = onIntroductionChangeClick
            )
        }
        "medalExplain" -> {
            SimpleAlertDialog(
                onConfirmClick = onClose,
                text = medalExplain,
                onDismissOn = false,
                title = "칭호 설명"
            )
        }
        "medalQuestion" -> SimpleAlertDialog(
            onConfirmClick = {onSituationChange("")},
            onDismissOn = false,
            text = "하루마을 곳곳에 숨어있는 칭호를 찾아보세요!\n내가 획득한 칭호는 클릭하여 정보를 확인할 수 있습니다.\n모든 칭호를 모아 하루마을의 영웅이 되어보세요!"
        )
    }

    Surface (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBCE8E3))
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
                        text = "${userDataList.find { it.id == "name" }?.value}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(start = 10.dp, end = 6.dp)
                    )
                    Text(
                        text = "#${userDataList.find { it.id == "auth" }?.value2}",
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
                    text = " ${userDataList.find { it.id == "community" }?.value}",
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

            if(page == 0) {
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

                    val medal = myMedalList.firstOrNull()

                    Text(
                        text = when (medal) {
                            null -> ""
                            0 -> "칭호 없음"
                            else -> medalName(medal)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF6B1F1F),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )



                }


                // 미니맵 뷰
                Surface(
                    modifier = Modifier
                        .aspectRatio(1f / 1.25f)
                        .padding(start = 6.dp, end = 6.dp)
                    ,
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF8E7),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White), // Optional: Set background color
                        contentAlignment = Alignment.Center // Center content
                    ) {
                        JustImage(
                            filePath = areaUrl,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val density = LocalDensity.current

                            // Surface 크기 가져오기 (px → dp 변환)
                            val surfaceWidth = constraints.maxWidth
                            val surfaceHeight = constraints.maxHeight

                            val surfaceWidthDp = with(density) { surfaceWidth.toDp() }
                            val surfaceHeightDp = with(density) { surfaceHeight.toDp() }

                            worldDataList.forEachIndexed { index, worldData ->
                                key("${worldData.id}_${worldData.type}") {
                                    if (worldData.type == "pat") {
                                        patDataList.find { it.id.toString() == worldData.value }
                                            ?.let { patData ->

                                                PatImage(
                                                    patUrl = patData.url,
                                                    surfaceWidthDp = surfaceWidthDp,
                                                    surfaceHeightDp = surfaceHeightDp,
                                                    xFloat = patData.x,
                                                    yFloat = patData.y,
                                                    sizeFloat = patData.sizeFloat,
                                                    effect = patData.effect,
                                                    onClick = { }
                                                )

                                            }

                                    } else {
                                        itemDataList.find { it.id.toString() == worldData.value }
                                            ?.let { itemData ->
                                                WorldItemImage(
                                                    itemUrl = itemData.url,
                                                    surfaceWidthDp = surfaceWidthDp,
                                                    surfaceHeightDp = surfaceHeightDp,
                                                    xFloat = itemData.x,
                                                    yFloat = itemData.y,
                                                    sizeFloat = itemData.sizeFloat
                                                )

                                            }

                                    }
                                }
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
                            val rawText = userDataList.find { it.id == "etc" }?.value

                            Text(
                                text = if (rawText == null || rawText == "0") {
                                    "안녕하세요 :)"
                                } else {
                                    rawText
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {// 접속 정보
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "탄생일",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(end = 6.dp)
                        )
                        Text(
                            text = userDataList.find { it.id == "date" }?.value3 ?: "2015-03-12",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "칭호 ${myMedalList.size-1}/${totalMedalCount()}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(end = 6.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "접속일",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(end = 6.dp)
                        )
                        Text(
                            text = "${userDataList.find { it.id == "date" }?.value2 ?: "-"}일",
                            style = MaterialTheme.typography.bodyMedium
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
                        text = "칭호 ${myMedalList.size-1}/${totalMedalCount()}"
                        ,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    JustImage(
                        filePath = "etc/question.png",
                        modifier = Modifier
                            .size(20.dp)
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
                            val isOwned = myMedalList.contains(medalType)

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
                                    .clickable(
                                        enabled = isOwned,
                                        indication = null, // ✨ 클릭 효과(리플) 제거
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        onMedalExplainClick(index + 1)
                                    }
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
                                        text = "${allPatDataList.count { it.date != "0" }}/${allPatDataList.size}",
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
                                        text = "${allItemDataList.count { it.date != "0" } - 20}/${allItemDataList.size - 20}",
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
                                        text = "${allAreaDataList.count { it.date != "0" }}/${allAreaDataList.size}",
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
                                        text = userDataList.find { it.id == "firstGame" }?.value + "점",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                    )

                                }

                                Row {
                                    Text(
                                        text = "1to50",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                    )

                                    val secondGameTime =
                                        userDataList.find { it.id == "secondGame" }?.value

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
                                        text = userDataList.find { it.id == "thirdGame" }?.value + "개",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                    )

                                }

                                Row {
                                    Text(
                                        text = "보통",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                    )
                                    Text(
                                        text = userDataList.find { it.id == "thirdGame" }?.value2 + "개",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                    )

                                }

                                Row {
                                    Text(
                                        text = "어려움",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                    )
                                    Text(
                                        text = userDataList.find { it.id == "thirdGame" }?.value3 + "개",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                    )

                                }

                            }

                        }
                    }

                }

                Spacer(modifier = Modifier.size(12.dp))

            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                ,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                MainButton(
                    text = "칭호",
                    onClick = {
                        onSituationChange("medal")
                    }
                )
                MainButton(
                    text = "인삿말",
                    onClick = {
                        onSituationChange("introduction")
                    }
                )
                MainButton(
                    text = if(page == 0) "상세" else "메인",
                    onClick = {
                        if(page == 0) page = 1 else page = 0
                    },
                    modifier = Modifier
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InformationScreenPreview() {
    MypatTheme {
        InformationScreen(
            areaUrl = "area/beach.jpg",
            patDataList = listOf(Pat(url = "pat/cat.json")),
            itemDataList = listOf(Item(url = "item/airplane.json")),
            allPatDataList = listOf(Pat(url = "pat/cat.json")),
            allItemDataList = listOf(Item(url = "item/airplane.json")),
            allAreaDataList = listOf(Area(url = "area/forest.png")),
            userDataList = listOf(User(id = "etc", value3 = "1/1/12/3/4/5/6/7/21")),
        )
    }
}