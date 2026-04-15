@file:Suppress("DEPRECATION")

package com.a0100019.mypat.presentation.ui.image.item

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a0100019.mypat.presentation.ui.image.etc.LottieCache
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun DraggableItemImage(
    instanceKey: Any? = null,   // ✅ 고유키(예: worldData.id) 전달 권장
    itemUrl: String,
    surfaceWidthDp: Dp,
    surfaceHeightDp: Dp,
    xFloat: Float,              // 0..1
    yFloat: Float,              // 0..1
    sizeFloat: Float,
    onClick: () -> Unit,
    border: Boolean = true,
    newFloat: (Float, Float) -> Unit = { _, _ -> }

) {
    val key = instanceKey ?: itemUrl
    val density = LocalDensity.current

    //  내부에는 비율만 저장 (삭제/치환 시 꼬임 방지)
    var posX by rememberSaveable(key) { mutableStateOf(xFloat) }
    var posY by rememberSaveable(key) { mutableStateOf(yFloat) }

    // 외부 모델 변경 시 동기화
    LaunchedEffect(key, xFloat, yFloat) {
        posX = xFloat
        posY = yFloat
    }

    val imageSize = surfaceWidthDp * sizeFloat
    val offsetX = surfaceWidthDp * posX
    val offsetY = surfaceHeightDp * posY

    if (itemUrl.takeLast(4).equals("json", ignoreCase = true)) {
        // Lottie 아이템
        val composition by rememberLottieComposition(LottieCache.get(itemUrl))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )

        if (composition != null) {
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .offset(x = offsetX, y = offsetY)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                    .then(
                        if (border) Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) else Modifier
                    )
                    //  제스처는 고유키에 묶어서 안정화
                    .pointerInput(key, surfaceWidthDp, surfaceHeightDp) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()

                                val dxDp = with(density) { dragAmount.x.toDp() }
                                val dyDp = with(density) { dragAmount.y.toDp() }

                                posX = (posX + (dxDp / surfaceWidthDp)).coerceIn(0f, 1f)
                                posY = (posY + (dyDp / surfaceHeightDp)).coerceIn(0f, 1f)
                                //  드래그 중에는 ViewModel에 올리지 않음 (끊김 방지)
                            },
                            onDragEnd = { newFloat(posX, posY) },
                            onDragCancel = { newFloat(posX, posY) }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress }
                )
            }
        } else {
            Box(contentAlignment = Alignment.Center) { Text("Loading...") }
        }
    } else {
        // 비트맵 아이템
        val context = LocalContext.current
        var bitmap by remember(itemUrl) { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(itemUrl) {
            bitmap = try {
                context.assets.open(itemUrl).use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } catch (_: Exception) { null }
        }

        if (bitmap != null) {
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .offset(x = offsetX, y = offsetY)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                    .then(
                        if (border) Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) else Modifier
                    )
                    .pointerInput(key, surfaceWidthDp, surfaceHeightDp) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()

                                val dxDp = with(density) { dragAmount.x.toDp() }
                                val dyDp = with(density) { dragAmount.y.toDp() }

                                posX = (posX + (dxDp / surfaceWidthDp)).coerceIn(0f, 1f)
                                posY = (posY + (dyDp / surfaceHeightDp)).coerceIn(0f, 1f)
                                //  드래그 중에는 ViewModel에 올리지 않음
                            },
                            onDragEnd = { newFloat(posX, posY) },
                            onDragCancel = { newFloat(posX, posY) }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Asset Image",
                )
            }
        } else {
            Box(contentAlignment = Alignment.Center) { Text("Loading...") }
        }
    }
}
