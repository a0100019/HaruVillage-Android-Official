package com.a0100019.mypat.presentation.neighbor.board

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.a0100019.mypat.data.room.photo.Photo
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.presentation.ui.component.MainButton
import com.a0100019.mypat.presentation.ui.image.etc.JustImage
import com.a0100019.mypat.presentation.ui.theme.MypatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardSubmitDialog(
    text: String = "",
    anonymous: String = "0",
    type: String = "free",
    onClose: () -> Unit = {},
    onChangeAnonymousClick: (String) -> Unit = {},
    onChangeTypeClick: (String) -> Unit = {},
    onTextChange: (String) -> Unit = {},
    onConfirmClick: () -> Unit = {},
    onImageSelected: (Uri) -> Unit = {}, // ✅ 사진 선택 콜백 추가
    photoDataList: List<Photo> = emptyList(),
    deleteImage: (Photo) -> Unit = {},
    photoLocalPath: String = "0"
) {
    // 드롭다운 펼침 상태 관리
    var expanded by remember { mutableStateOf(false) }

    // 타입에 따른 한글 명칭 매핑
    val typeMap = mapOf(
        "free" to "자유",
        "worry" to "고민",
        "congratulation" to "축하",
        "friend" to "친구"
    )

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .width(340.dp)
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
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "게시글 작성",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp),
                )

                // --- 익명 여부 및 카테고리 선택 Row ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 익명 체크박스
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = anonymous == "1",
                            onCheckedChange = { onChangeAnonymousClick(if (it) "1" else "0") }
                        )
                        Text(text = "익명")
                    }

                    // ✅ 갤러리 런처 정의
                    val galleryLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let { onImageSelected(it) }
                    }

                    if(photoLocalPath == "0") {
                        JustImage(
                            filePath = "etc/camera.png",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    // 2. 갤러리 열기 (이미지 파일만 필터링)
                                    galleryLauncher.launch("image/*")
                                }
                        )
                    } else {
                        //사진
                        Box(
                            modifier = Modifier,
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // 1. 마지막 사진 1개만 가져오기
                            val lastPhoto = photoDataList.lastOrNull()

                            if (lastPhoto != null) {
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = lastPhoto.localPath,
                                        contentDescription = "마지막 일기 사진",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                // clickPhotoChange(lastPhoto.localPath)
                                            },
                                        contentScale = ContentScale.Crop
                                    )

                                    // 삭제 버튼
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(25.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .clickable {
                                                deleteImage(lastPhoto)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✕", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    // --- 드롭다운 영역 ---
                    Box {
                        Surface(
                            onClick = { expanded = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = typeMap[type] ?: "선택",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                // 화살표 아이콘 (이전 답변의 회전 로직 활용 가능)
                                // 드롭다운 내부의 아이콘 영역
                                JustImage(
                                    filePath = "etc/arrow.png",
                                    modifier = Modifier
                                        .size(15.dp)
                                        .graphicsLayer {
                                            // 메뉴가 열려있을(expanded) 때는 180도 회전해서 위를 보게 함
                                            rotationZ = 180f
                                        }
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            typeMap.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        onChangeTypeClick(key)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    label = { Text("내용") },
                    placeholder = { Text("내용을 입력하세요.") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(8.dp),
                    maxLines = Int.MAX_VALUE
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "부적절한 내용을 작성할 경우, 경고 없이 제제를 받을 수 있습니다",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Row {
                    MainButton(
                        text = " 취소 ",
                        onClick = onClose,
                        modifier = Modifier.padding(16.dp)
                    )
                    MainButton(
                        text = " 확인 ",
                        onClick = onConfirmClick,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BoardSubmitDialogPreview() {
    MypatTheme {
        BoardSubmitDialog(
        )
    }
}