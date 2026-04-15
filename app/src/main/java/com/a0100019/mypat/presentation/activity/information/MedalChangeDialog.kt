package com.a0100019.mypat.presentation.activity.information

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.presentation.main.management.medalName
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.component.TextAutoResizeSingleLine
import com.a0100019.mypat.presentation.ui.theme.MypatTheme

@Composable
fun MedalChangeDialog(
    onClose: () -> Unit,
    onMedalClick: (Int) -> Unit = {},
    userDataList: List<User> = emptyList()
) {

    Dialog(
        onDismissRequest = onClose
    ) {
        Box(
            modifier = Modifier
                .width(340.dp)
//                .fillMaxHeight(0.8f)
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

                Text(
                    text = "대표 칭호 변경",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp),
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f / 1.25f)
                        .padding(6.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF8E7),
                    border = BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primaryContainer
                    ),
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // 한 줄에 3개
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        val myMedalString =
                            userDataList.find { it.id == "etc" }?.value3 ?: ""

                        val myMedalList: List<Int> =
                            myMedalString
                                .split("/")
                                .mapNotNull { it.toIntOrNull() }
                                .drop(1)

                        //  가진 메달만 Grid에 표시
                        items(myMedalList) { medalId ->

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f / 0.4f)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onMedalClick(medalId)
                                    }
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 12.dp)
                                    ,
                                contentAlignment = Alignment.Center
                            ) {
                                TextAutoResizeSingleLine(
                                    text = medalName(medalId),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // 추가로 원하는 Composable 요소

                MainButton(
                    text = " 취소 ",
                    onClick = onClose,
                    modifier = Modifier
                        .padding(16.dp)
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedalChangeDialogPreview() {
    MypatTheme {
        MedalChangeDialog(
            onClose = {},
        )
    }
}