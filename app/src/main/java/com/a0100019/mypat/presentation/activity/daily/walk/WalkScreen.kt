package com.a0100019.mypat.presentation.activity.daily.walk

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a0100019.mypat.R
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WalkScreen(
    walkViewModel: WalkViewModel = hiltViewModel(),
    popBackStack: () -> Unit = {},

) {

    val walkState: WalkState = walkViewModel.collectAsState().value

    val context = LocalContext.current

    walkViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is WalkSideEffect.Toast -> Toast.makeText(
                context,
                sideEffect.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    WalkScreen(
        userDataList = walkState.userDataList,
        today = walkState.today,
        calendarMonth = walkState.calendarMonth,
        saveSteps = walkState.saveSteps,
        stepsRaw = walkState.stepsRaw,
        situation = walkState.situation,
        baseDate = walkState.baseDate,

        onTodayWalkSubmitClick = walkViewModel::onTodayWalkSubmitClick,
        onCalendarMonthChangeClick = walkViewModel::onCalendarMonthChangeClick,
        popBackStack = popBackStack,
        onSituationChangeClick = walkViewModel::onSituationChangeClick
//        onSensorChangeClick = walkViewModel::onSensorChangeClick
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun WalkScreen(

    userDataList: List<User> = emptyList(),

    today: String = "2025-07-15",
    calendarMonth: String = "2025-07",
    saveSteps: Int = 3000,
    stepsRaw: String = "2025-07-17.1000/2025-07-14.2000/2026-04-09.7300",
    situation: String = "record",
    baseDate: String = "2025-07-15",

    onCalendarMonthChangeClick: (String)-> Unit = {},
    onTodayWalkSubmitClick: ()-> Unit = {},
    popBackStack: () -> Unit = {},
    onSituationChangeClick: (String) -> Unit = {},

) {

    val context = LocalContext.current
    val intent = Intent(context, StepForegroundService::class.java)
    context.startForegroundService(intent)

    // stepsRaw → 날짜별 걸음수 Map
    val items = stepsRaw.split("/").filter { it.isNotBlank() }

    val walkMap = items
        .mapNotNull {
            val parts = it.split(".")
            if (parts.size == 2) parts[0] to parts[1].toInt() else null
        }
        .toMap()

    // 전체 걸음 수
    val totalSteps = walkMap.values.sum()

    // 전체 평균 걸음 수
    val averageSteps = if (walkMap.isNotEmpty()) walkMap.values.average().toInt() else 0

    // 기본값 0
    var todaySteps = 0

    // 마지막 기록 가져오기
    val last = items.lastOrNull()

    if (last != null) {
        val parts = last.split(".")
        if (parts.size == 2) {

            val date = parts[0]
            val steps = parts[1].toInt()

            //  last가 오늘 날짜일 때만 steps 적용
            if (date == today) {
                todaySteps = steps
            }
        }
    }

// 보폭(0.65m) 가정
    val stride = 0.65

// 오늘 이동 거리(km)
    val todayDistance = todaySteps * stride / 1000.0

// 오늘 칼로리 (1걸음 ≈ 0.04kcal)
    val todayCalories = todaySteps * 0.04

// 최근 7일 날짜 범위
    val todayDate = LocalDate.parse(today)
    val weekStart = todayDate.minusDays(6)

// 수정된 방식: 값이 있는 날짜만 추출
    val weekSteps = (0..6)
        .map { weekStart.plusDays(it.toLong()).toString() }
        .mapNotNull { date -> walkMap[date] }   // 🔥 값 있는 날짜만 가져옴 (null 제거)

// 일주일 평균 걸음 수 (값 있는 날만 평균)
    val weekAverageSteps = if (weekSteps.isNotEmpty()) {
        weekSteps.average().toInt()
    } else 0

// 총 이동 거리(km)
    val totalDistance = totalSteps * stride / 1000.0

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {

        BackGroundImage()


        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Box(
                contentAlignment = Alignment.Center, // ✅ 내부 내용물 중앙 정렬
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {

                JustImage(
                    filePath = "etc/exit.png",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .clickable {
                            popBackStack()
                        }
                )

                StepProgressCircle(
                    steps = todaySteps,
                    strokeWidthCustom = 0.08f,
                    modifier = Modifier
                        .size(200.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "오늘 걸음 수",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = todaySteps.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "설정에서 만보기를 정지할 수 있습니다",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                )


            }

            if (saveSteps <= 5000) {
                // 상단에 이 변수를 추가하면 게이지가 스르륵 차오릅니다 (선택사항)
                val animatedProgress by animateFloatAsState(
                    targetValue = (saveSteps.coerceAtMost(5000) / 5000f),
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                    label = "stepProgress"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp) // 외부 여백 조정
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp))
                        .background(
                            color = Color(0xFFFFF9C4),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp) // 내부 패딩을 20 -> 16으로 축소
                ) {
                    // 헤더와 걸음 수를 한 줄로 합쳐서 세로 공간 절약 가능 (선택 사항)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "☀️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "햇살 모으기",
                            style = MaterialTheme.typography.titleSmall, // 타이포 크기 살짝 축소
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp)) // 간격 축소 (16 -> 8)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFBC02D))) {
                                    append("$saveSteps")
                                }
                                withStyle(SpanStyle(fontSize = 12.sp, color = Color.Gray)) {
                                    append(" / 5000")
                                }
                            }
                        )
                        Text(
                            text = String.format("%.1f%%", animatedProgress * 100),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBC02D)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp)) // 간격 축소 (10 -> 6)

                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp) // 두께 축소 (18 -> 10)
                            .clip(CircleShape), // 둥글게 깎기
                        color = Color(0xFFFFEB3B),
                        trackColor = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(6.dp)) // 간격 축소 (8 -> 6)

                    Text(
                        text = if (saveSteps >= 5000) "햇살 획득 완료! ✨" else "조금만 더 걸어봐요!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8D6E63),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

            } else {
                ShinyMissionCard(
                    onClick = onTodayWalkSubmitClick
                )
            }

            Column(
                modifier = Modifier
                    .height(380.dp)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                    ,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    MainButton(
                        text = "통계",
                        onClick = {
                            onSituationChangeClick("record")
                        },
                        backgroundColor = if(situation=="record")MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.scrim,
                        borderColor = if(situation=="record")MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primaryContainer
                    )
                    MainButton(
                        text = "주간",
                        onClick = {
                            onSituationChangeClick("week")
                        },
                        backgroundColor = if(situation=="week")MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.scrim,
                        borderColor = if(situation=="week")MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primaryContainer
                    )
                    MainButton(
                        text = "월간",
                        onClick = {
                            onSituationChangeClick("month")
                        },
                        backgroundColor = if(situation=="month")MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.scrim,
                        borderColor = if(situation=="month")MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primaryContainer
                    )

                }

                Spacer(modifier = Modifier.size(8.dp))

                if(situation == "record"){

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp)) // 은은한 그림자 추가
                            .background(Color.White, shape = RoundedCornerShape(24.dp)) // 배경은 깔끔한 화이트
                            .border(
                                width = 1.dp,
                                color = Color(0xFFF0F0F0), // 아주 연한 회색 테두리
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        val goalStatus = getWalkGoalStatus(totalSteps, walkGoals)

                        // 🚩 상단 목표 섹션
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF4E5), shape = RoundedCornerShape(16.dp)) // 목표 섹션만 연한 오렌지 배경
                                .padding(16.dp)
                            ,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🚩 현재 목표 : ${goalStatus.currentGoal.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE65100)
                            )

                            Text(
                                text = String.format(
                                    "전체 %.2f km 중 %.2f km 남았습니다",
                                    goalStatus.currentGoal.distanceKm,
                                    goalStatus.remainKm
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6D4C41),
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LinearProgressIndicator(
                                    progress = goalStatus.progress.toFloat(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFFFFB74D),
                                    trackColor = Color.White
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = String.format("%.1f%%", goalStatus.progress * 100),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        //  통계 섹션 헤더
                        Text(
                            text = "📊 걸음 수 통계",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D2D2D),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 통계 내용 (좌우 정렬 맞춤)
                        val labelStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray, fontWeight = FontWeight.Medium)
                        val valueStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF424242))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("일주일 평균 걸음", style = labelStyle)
                                Text("${weekAverageSteps} 보", style = valueStyle)
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("총 이동 거리", style = labelStyle)
                                    Text(String.format("%.2f km", totalDistance), style = valueStyle)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("총 걸음 수", style = labelStyle)
                                    Text("${totalSteps} 보", style = valueStyle)
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = "별 아이콘",
                            modifier = Modifier
                                .rotate(270f)
                                .clickable(
                                    indication = null, // ← ripple 효과 제거
                                    interactionSource = remember { MutableInteractionSource() } // ← 필수
                                ) {
                                    onCalendarMonthChangeClick("left")
                                }
                        )
                        Text(
                            text = "오늘로 이동",
                            modifier = Modifier
                                .clickable(
                                    indication = null, // ← ripple 효과 제거
                                    interactionSource = remember { MutableInteractionSource() } // ← 필수
                                ) {
                                    onCalendarMonthChangeClick("today")
                                }
                        )
                        Image(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = "별 아이콘",
                            modifier = Modifier
                                .rotate(90f)
                                .clickable(
                                    indication = null, // ← ripple 효과 제거
                                    interactionSource = remember { MutableInteractionSource() } // ← 필수
                                ) {
                                    onCalendarMonthChangeClick("right")
                                }
                        )
                    }
                }

                if(situation == "month"){
                    Text(
                        text = calendarMonth,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(top = 6.dp)
                    )

                    WalkCalendarView(
                        today = today,
                        calendarMonth = calendarMonth,
                        stepsRaw = stepsRaw
                    )
                } else if(situation == "week"){
                    Text(
                        text = getWeekLabel(today = today, baseDate = baseDate),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(top = 6.dp)
                    )

                    WalkWeekView(
                        today = today,
                        baseDate = baseDate,
                        stepsRaw = stepsRaw
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

            }

        }

    }

}

@Preview(showBackground = true)
@Composable
fun WalkScreenPreview() {
    MypatTheme {
        WalkScreen(
            stepsRaw = "2025-07-17.10000/2025-07-14.2000/2025-07-15.500",
            situation = "record",
            saveSteps = 3000
        )
    }
}

fun getWeekLabel(today: String, baseDate: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val todayDate = LocalDate.parse(today, formatter)
    val base = LocalDate.parse(baseDate, formatter)

    // baseDate가 속한 주의 월요일
    val weekStart = base.with(DayOfWeek.MONDAY)
    val weekEnd = weekStart.plusDays(6)

    // today가 그 주 안에 있으면 = "이번 주"
    if (!todayDate.isBefore(weekStart) && !todayDate.isAfter(weekEnd)) {
        return "이번 주"
    }

    // 오늘이 포함되지 않는 주면 → "MM/dd ~ MM/dd"
    val uiFormatter = DateTimeFormatter.ofPattern("MM/dd")
    val startStr = weekStart.format(uiFormatter)
    val endStr = weekEnd.format(uiFormatter)

    return "$startStr ~ $endStr"
}
