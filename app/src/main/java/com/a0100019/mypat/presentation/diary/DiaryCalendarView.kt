package com.a0100019.mypat.presentation.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a0100019.mypat.data.room.diary.Diary
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun DiaryCalendarView(
    today: String,              // "yyyy-MM-dd"
    calendarMonth: String,      // 예: "2025-04"
    diaryList: List<Diary>,
    onDiaryDateClick: (String) -> Unit = {}
) {
    val ymFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    val yearMonth = YearMonth.parse(calendarMonth, ymFormatter)

    val year = yearMonth.year
    val month = yearMonth.monthValue

    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0

    //  Diary.date -> LocalDate 로 매핑해 둔 Map (날짜 기준으로 Diary 바로 찾기 위함)
    val diaryMap: Map<LocalDate, Diary> = diaryList.associateBy { LocalDate.parse(it.date) }

    // 캘린더에 쓸 날짜 그리드 (LocalDate?)
    val dates = mutableListOf<LocalDate?>()
    repeat(startDayOfWeek) { dates.add(null) }
    repeat(daysInMonth) { offset ->
        dates.add(firstDayOfMonth.plusDays(offset.toLong()))
    }

    // 마지막 주 7칸 맞추기
    val totalCells = ((dates.size + 6) / 7) * 7
    repeat(totalCells - dates.size) { dates.add(null) }

    val todayDate = LocalDate.parse(today)

    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
    ) {
        // 요일 헤더
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { index, day ->
                val textColor = when (index) {
                    0 -> Color(0xFFFF8A80) // 일요일
                    6 -> Color(0xFF64B5F6) // 토요일
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

        // 날짜 셀
        dates.chunked(7).forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 날짜 셀 내부
                week.forEachIndexed { index, date ->
                    val diary: Diary? = date?.let { diaryMap[it] }
                    val isToday = date != null && date == todayDate

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        // 오늘 표시
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFE1BEE7), CircleShape)
                            )
                        }

                        //  Diary.emotion 값에 따라 색 변경 (함수 X, 즉석 when 문)
                        if (diary != null) {

                            if(diary.state == "대기") {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .background(Color(0xFFB2EBF2), shape = CircleShape)
                                )
                            } else {
                                JustImage(
                                    filePath = diary.emotion,
                                    modifier = Modifier
                                        .size(26.dp)
//                                        .alpha(0.9f)
                                )
                            }
                        }

                        // 날짜 텍스트
                        Text(
                            text = date?.dayOfMonth?.toString() ?: "",
                            modifier = Modifier
                                .alpha(0.4f)
                                .clickable(enabled = date != null) {
                                if (date != null) {
                                    onDiaryDateClick(date.toString()) // "yyyy-MM-dd"
                                }
                            }
                        )
                    }
                }

            }
        }
    }
}
