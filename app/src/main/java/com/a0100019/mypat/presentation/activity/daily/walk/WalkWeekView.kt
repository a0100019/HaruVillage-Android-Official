package com.a0100019.mypat.presentation.activity.daily.walk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
@Composable
fun WalkWeekView(
    today: String = "2025-01-15",
    baseDate: String = "2025-01-15",
    stepsRaw: String
) {
    //  stepsRaw → Map<"YYYY-MM-DD", Int>
    val walkMap = stepsRaw
        .split("/")
        .mapNotNull {
            val p = it.split(".")
            if (p.size == 2) p[0] to p[1].toInt() else null
        }.toMap()

    val todayDate = LocalDate.parse(today)
    val baseLocalDate = LocalDate.parse(baseDate)

    // 🟦 baseDate가 속한 주의 월요일 계산
    val weekStart = baseLocalDate.with(DayOfWeek.MONDAY)
    val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }

    // 🟦 요일 이름 ("월", "화", "수" ...)
    val dayFormatter = DateTimeFormatter.ofPattern("E", Locale.KOREAN)
    val dayNames = weekDates.map { it.format(dayFormatter) }

    // 🟦 걸음 수 (없으면 0)
    val stepsList = weekDates.map { walkMap[it.toString()] ?: 0 }

    val maxGoal = 15000f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
    ) {


        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {

            stepsList.forEachIndexed { index, steps ->

                val date = weekDates[index]
                val isToday = date == todayDate // 오늘 표시

                val progress = (steps / maxGoal).coerceIn(0f, 1f)

                // ▣ 그래프 색상
                val barBrush = when {
                    steps >= 10000 -> Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFA726),
                            Color(0xFFFF80AB)
                        )
                    )
                    steps > 0 -> Brush.verticalGradient(
                        listOf(
                            Color(0xFF9575CD), // 파스텔 라일락 퍼플 (부드러움↑)
                            Color(0xFF7986CB)  // 차분한 블루퍼플
                        )
                    )
                    else -> Brush.verticalGradient(
                        listOf(
                            Color(0xFFE0E0E0),
                            Color(0xFFEEEEEE)
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    // 그래프 막대
                    Box(
                        modifier = Modifier
                            .height((170 * progress).dp)
                            .width(16.dp)
                            .background(barBrush, shape = RoundedCornerShape(12.dp))
                    )


                    // 요일 표시
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
                    ) {
                        // 오늘이면 배경 원 표시
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(Color(0xFFEDE7F6), CircleShape)
                            )
                        }

                        Text(
                            text = dayNames[index],
                            color = if (isToday) Color(0xFF8E24AA) else Color(0xFF555555),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                    Text(
                        text = if(steps != 0) steps.toString() else "",
                        style = MaterialTheme.typography.labelMedium
                    )

                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWalkWeekView() {
    WalkWeekView(
        today = "2025-01-15",
        baseDate = "2025-01-15",
        stepsRaw = "2025-01-01.2000/2025-01-03.8000/2025-01-14.20000/2025-01-15.5000"
    )
}
