package com.a0100019.mypat.presentation.activity.index

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.a0100019.mypat.data.room.area.Area
import com.a0100019.mypat.trash.AppBgmManager
import com.a0100019.mypat.presentation.ui.MusicPlayer
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme

@Composable
fun IndexAreaDialog(
    onClose: () -> Unit,
    areaData: Area,
    open: Boolean = true
) {

    AppBgmManager.pause()

    MusicPlayer(
        music = areaData.url
    )

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .width(340.dp)
                .padding(16.dp)
                .shadow(12.dp, shape = RoundedCornerShape(24.dp))
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🏷️ 지역 이름
                Text(
                    text = areaData.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )

                // 🖼️ 이미지 박스
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f/1.25f)
                        .background(
                            color = if (open) {
                                MaterialTheme.colorScheme.scrim
                            } else {
                                Color.LightGray
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                            ,
                    contentAlignment = Alignment.Center
                ) {
                    JustImage(
                        filePath = areaData.url,
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(16.dp))
                            .matchParentSize()
                        ,
                       contentScale = ContentScale.FillBounds
                        )
                    if(!open) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color.LightGray.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(16.dp)
                                ) // 반투명 배경
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 📅 획득 날짜
                if (open) {
                    Text(
                        text = "📅 획득 날짜 : ${areaData.date}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 닫기 버튼
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(1f))
                    MainButton(
                        text = "닫기",
                        onClick = onClose,
                        modifier = Modifier.width(100.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IndexMapDialogPreview() {
    MypatTheme {
        IndexAreaDialog(
            onClose = {},
            areaData = Area(
                url = "area/kingdom.webp",
                name = "숲",
                ),
        )
    }
}