package com.a0100019.mypat.presentation.activity.daily.walk

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun StepProgressCircle(
    steps: Int,
    strokeWidthCustom: Float = 0.1f,
    goal: Int = 10000,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
        .fillMaxSize()
        .aspectRatio(1f)
) {
    val progress = (steps.toFloat() / goal).coerceIn(0f, 1f)
    val sweepAngle = progress * 360f

    Canvas(modifier = modifier) {
        val minSize = size.minDimension
        val strokeWidth = minSize * strokeWidthCustom
        val halfStroke = strokeWidth / 2f
        val radius = minSize / 2f - halfStroke

        // ▣ 배경 원
        drawArc(
            color = Color(0xFFECEFF1),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // ▣ 기본 파스텔 그라데이션
        val normalGradient = Brush.sweepGradient(
            listOf(
                Color(0xFF82B1FF), // 파스텔 블루
                Color(0xFFB39DDB), // 라벤더 퍼플
                Color(0xFF9575CD), // 보라
                Color(0xFF82B1FF)
            )
        )


        // ▣ 100% 완료 시 특별한 그라데이션
        val fullGradient = Brush.sweepGradient(
            listOf(
                Color(0xFFFFD54F),  // 골드
                Color(0xFFFFA726),  // 오렌지
                Color(0xFFFF80AB),  // 핑크
                Color(0xFFFFD54F),
            )
        )

        //  100% 여부에 따라 색 선택
        val ringBrush = if (progress >= 1f) fullGradient else normalGradient

        // ▣ 메인 링만 그리기 (Glow 제거됨)
        drawArc(
            brush = ringBrush,
            startAngle = 270f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewStepProgress() {
        StepProgressCircle(
            steps = 8000
        ) // ✅ 5000 걸음 (반 채워짐)
}
