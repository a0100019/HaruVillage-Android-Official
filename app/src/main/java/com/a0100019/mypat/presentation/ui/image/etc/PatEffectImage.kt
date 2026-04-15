package com.a0100019.mypat.presentation.ui.image.etc

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun PatEffectImage(
    surfaceWidthDp: Dp,
    surfaceHeightDp: Dp,
    effect: Int = 0,
    xFloat: Float,
    yFloat: Float,
    sizeFloat: Float,
    isPlaying: Boolean = true   // ✅ 추가
) {
    if (effect == 0) return

    val imageUrl = patEffectIndexToUrl(effect)
    if (imageUrl.isEmpty()) return

    val composition by rememberLottieComposition(LottieCache.get(imageUrl))

    val imageSize = remember(surfaceWidthDp, sizeFloat) {
        surfaceWidthDp * sizeFloat
    }

    val modifier = remember(
        surfaceWidthDp,
        surfaceHeightDp,
        xFloat,
        yFloat,
        imageSize
    ) {
        Modifier
            .size(imageSize)
            .offset(
                x = surfaceWidthDp * xFloat,
                y = surfaceHeightDp * yFloat
            )
    }

    //  애니메이션 진행 중일 때만 계산
    val animatedProgress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = Int.MAX_VALUE
    )

    //  멈춘 순간의 progress 고정
    val frozenProgress = remember { mutableStateOf(0f) }

    if (isPlaying) {
        frozenProgress.value = animatedProgress
    }

    LottieAnimation(
        composition = composition,
        progress = {
            if (isPlaying) animatedProgress else frozenProgress.value
        },
        modifier = modifier
    )
}


fun patEffectIndexToUrl(index: Int): String {
    return when(index) {
        1 -> "patEffect/starfall.json"
        2 -> "patEffect/effect_snow.json"
        3 -> "patEffect/aurora.json"
        4 -> "patEffect/lightning_effect.json"
        5 -> "patEffect/laugh_effect.json"
        else -> ""
    }
}