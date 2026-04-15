package com.a0100019.mypat.presentation.activity.index

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.a0100019.mypat.R
import com.a0100019.mypat.data.room.item.Item
import com.a0100019.mypat.presentation.ui.MusicPlayer
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme

@Composable
fun IndexItemDialog(
    onClose: () -> Unit,
    itemData: Item,
    open: Boolean = true
) {

    MusicPlayer(
        music = itemData.name
    )

    Dialog(
        onDismissRequest = onClose
    ) {
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
                modifier = Modifier
                    .fillMaxWidth()
                ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🏷️ 아이템 이름
                Text(
                    text = itemData.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )

                // ️ 이미지 박스
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
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
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    JustImage(
                        filePath = itemData.url,
                        modifier = Modifier
                            .align(Alignment.Center)
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


                if(open) {
                    Text(
                        text = "📅 획득 날짜 : ${itemData.date}",
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
fun IndexItemDialogPreview() {
    MypatTheme {
        IndexItemDialog(
            onClose = {},
            itemData = Item(
                url = "item/airPlane.json",
                name = "비행기",
                ),
        )
    }
}