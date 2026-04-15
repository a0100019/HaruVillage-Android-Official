package com.a0100019.mypat.presentation.neighbor.privateChat

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PrivateChatGameHorizontalLine(
    currentValue: Int = 500,
    targetStart: Int = 450,
    targetEnd: Int = 550,
    maxPower: Int = 1000,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFB0B0B0), // 은은한 테두리
                shape = shape
            )
            .clip(shape)
            .background(Color(0xFFE0E0E0))
    ) {
        val barWidth = maxWidth

        val currentPercent =
            (currentValue.toFloat() / maxPower).coerceIn(0f, 1f)

        val targetStartPercent =
            (targetStart.toFloat() / maxPower).coerceIn(0f, 1f)

        val targetEndPercent =
            (targetEnd.toFloat() / maxPower).coerceIn(0f, 1f)

        val isSuccess = currentValue in targetStart..targetEnd

        //  목표 구간
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(barWidth * (targetEndPercent - targetStartPercent))
                .offset(x = barWidth * targetStartPercent)
                .background(
                    if (isSuccess)
                        Color(0xFFB7E4C7) // 성공
                    else
                        Color(0xFFFFF3CD) // 기본
                )
        )

        // 🔵 현재 값 (세로 선)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .offset(x = barWidth * currentPercent)
                .background(Color(0xFF42A5F5))
        )
    }
}


@Preview(showBackground = true, widthDp = 360)
@Composable
fun PrivateChatGameHorizontalLinePreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("파워 게이지")
        PrivateChatGameHorizontalLine(
        )
    }
}