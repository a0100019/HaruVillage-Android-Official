package com.a0100019.mypat.presentation.activity.daily.walk

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.BackGroundImage
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
    saveSteps: Int = 0,
    stepsRaw: String = "2001-01-01.1",
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

            // 🔥 last가 오늘 날짜일 때만 steps 적용
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

                // 오른쪽 버튼
                MainButton(
                    text = "닫기",
                    onClick = popBackStack,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
//
//                // 오른쪽 버튼
//                MainButton(
//                    text = "끄기",
//                    onClick = {
//                        context.stopService(intent)
//                    },
//                    modifier = Modifier
//                        .align(Alignment.TopStart)
//                        .padding(8.dp)
//                )

            }

            if (saveSteps <= 5000) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.scrim,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "5000보를 모아 햇살을 획득하세요!  현재 걸음 수 : $saveSteps",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val progress = (saveSteps.coerceAtMost(5000) / 5000f)

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = Color(0xFF81D4FA),      // 연한 하늘색 (Pastel Sky Blue)
                        trackColor = Color(0xFFE1F5FE)  // 아주 연한 하늘색 (Ice Blue)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 퍼센트 텍스트
                    Text(
                        text = String.format("%.1f%%", progress * 100),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.End)
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
                            .background(
                                MaterialTheme.colorScheme.scrim,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {

                        val goalStatus = getWalkGoalStatus(totalSteps, walkGoals)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "목표 : ${goalStatus.currentGoal.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // 🔥 전체 거리 + 남은 거리 표시
                            Text(
                                text = String.format(
                                    "전체 %.3f km / 남은 거리 %.3f km",
                                    goalStatus.currentGoal.distanceKm,   // 전체 거리
                                    goalStatus.remainKm               // 남은 거리
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // 🔥 프로그레스바 + 퍼센트
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                LinearProgressIndicator(
                                    progress = goalStatus.progress.toFloat(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    color = Color(0xFFFFB74D),   // 오렌지
                                    trackColor = Color(0xFFFFECB3)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = String.format("%.2f%%", goalStatus.progress * 100),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = "📊 걸음 수 통계",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(bottom = 12.dp, top = 12.dp)
                                .align(Alignment.CenterHorizontally),
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text("오늘 이동 거리 : ", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(String.format("%.3f km", todayDistance), style = MaterialTheme.typography.bodyLarge)

//                            Spacer(modifier = Modifier.weight(1f))
//
//                            Text("오늘 칼로리 소모 : ", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
//                            Text(String.format("%.1f kcal", todayCalories), style = MaterialTheme.typography.bodyLarge)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text("일주일 평균 걸음 수 : ", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(weekAverageSteps.toString(), style = MaterialTheme.typography.bodyLarge)

//                            Spacer(modifier = Modifier.weight(1f))
//                          s
//                            Text("전체 평균 걸음 수 : ", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
//                            Text(averageSteps.toString(), style = MaterialTheme.typography.bodyLarge)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "총 이동 거리 : ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = String.format("%.3f km", totalDistance),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "총 걸음 수 : ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = totalSteps.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
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
