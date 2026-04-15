package com.a0100019.mypat.presentation.activity.daily.walk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun WalkCalendarView(
    today: String,
    calendarMonth: String,
    stepsRaw: String = ""
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
    val yearMonth = YearMonth.parse(calendarMonth, formatter)

    val year = yearMonth.year
    val month = yearMonth.monthValue

    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    val items = stepsRaw.split("/").filter { it.isNotBlank() }

    val walkMap = items
        .mapNotNull {
            val parts = it.split(".")
            if (parts.size == 2) parts[0] to parts[1].toInt() else null
        }
        .toMap()

    val dates = mutableListOf<LocalDate?>()
    repeat(startDayOfWeek) { dates.add(null) }
    repeat(daysInMonth) { dates.add(firstDayOfMonth.plusDays(it.toLong())) }

    val totalCells = ((dates.size + 6) / 7) * 7
    repeat(totalCells - dates.size) { dates.add(null) }

    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { index, day ->
                val textColor = when (index) {
                    0 -> Color(0xFFFF8A80)
                    6 -> Color(0xFF64B5F6)
                    else -> Color.Unspecified
                }
                Text(
                    text = day,
                    color = textColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        val todayDate = LocalDate.parse(today)

        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ){
            dates.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    week.forEach { date ->
                        val dateString = date?.toString()
                        val count = walkMap[dateString]

                        // 오늘인가?
                        val isToday = date != null && date == todayDate

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            // 🌸 오늘 날짜 배경 원 (살짝 크고 파스텔톤)
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.tertiary,
                                            shape = CircleShape
                                        )
                                )
                            }

                            //  걸음 원 (StepProgressCircle)
                            if (date != null) {
                                StepProgressCircle(
                                    steps = count ?: 0,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            // 날짜 텍스트
                            Text(
                                text = date?.dayOfMonth?.toString() ?: "",
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCalendarView() {
    WalkCalendarView(
        today = "2025-01-15",
        calendarMonth = "2025-01",
        stepsRaw = "2025-01-01.2000/2025-01-03.8000/2025-01-10.15000/2025-01-15.5000"
    )
}
