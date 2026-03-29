package com.a0100019.mypat.presentation.diary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a0100019.mypat.data.room.area.AreaDao
import com.a0100019.mypat.data.room.diary.Diary
import com.a0100019.mypat.data.room.diary.DiaryDao
import com.a0100019.mypat.data.room.item.ItemDao
import com.a0100019.mypat.data.room.letter.LetterDao
import com.a0100019.mypat.data.room.pat.PatDao
import com.a0100019.mypat.data.room.photo.Photo
import com.a0100019.mypat.data.room.photo.PhotoDao
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.data.room.world.WorldDao
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class DiaryWriteViewModel @Inject constructor(
    private val diaryDao: DiaryDao,
    private val userDao: UserDao,
    private val worldDao: WorldDao,
    private val patDao: PatDao,
    private val itemDao: ItemDao,
    private val letterDao: LetterDao,
    private val areaDao: AreaDao,
    private val photoDao: PhotoDao,
    @ApplicationContext private val context: Context
) : ViewModel(), ContainerHost<DiaryWriteState, DiaryWriteSideEffect> {

    override val container: Container<DiaryWriteState, DiaryWriteSideEffect> = container(
        initialState = DiaryWriteState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(DiaryWriteSideEffect.Toast(message = throwable.message.orEmpty()))
                }
            }
        }
    )

    init {
        loadData()
        generateAiGreeting()
    }

    fun loadData() = intent {

        // 1. suspend로 바로 가져오는 유저 정보
        val userDataList = userDao.getAllUserData()
        val userDataEtc2Value = userDao.getValueById("etc2")
        val photoDataList = photoDao.getSyncedPhotosByDate(userDataEtc2Value)

        // 1. 데이터 가져오기 (이미 하신 부분)
        val allDiaryData = diaryDao.getAllDiaryData()

        // 2. 날짜 형식 지정을 위한 포맷터
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // 3. 오늘을 제외한 어제부터의 연속 일수 계산
        val diarySequence = run {
            val yesterday = LocalDate.now().minusDays(1)

            // DB의 날짜 문자열들을 LocalDate 세트로 변환 (검색 속도를 위해 Set 사용)
            val writtenDates = allDiaryData.map {
                LocalDate.parse(it.date, formatter)
            }.toSet()

            var count = 0
            var checkDate = yesterday

            // 어제부터 하루씩 뒤로 가면서 기록이 있는지 확인
            while (writtenDates.contains(checkDate)) {
                count++
                checkDate = checkDate.minusDays(1) // 하루 더 과거로
            }

            count // 최종 연속 일수 반환
        }

        val prefs = context.getSharedPreferences("diary_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("diarySequence", diarySequence)
            .apply()

        if(!userDataEtc2Value.startsWith("0")){
            //0있으면 하루 미션아닌 일기?
            val clickDiaryData = allDiaryData.find { it.date == userDataEtc2Value }
            if (clickDiaryData!!.state == "대기") {
                reduce {
                    state.copy(
                        firstWrite = true,
                        writeDiaryData = Diary(
                            id = clickDiaryData.id,
                            date = clickDiaryData.date,
                            state = "완료",
                            contents = "",
                            emotion = "emotion/smile.png"
                        )
                    )
                }
            } else {
                reduce {
                    state.copy(
                        firstWrite = false,
                        writeDiaryData = clickDiaryData
                    )
                }
            }
        } else {
            //하루미션 아닌 일기

            val lastId = allDiaryData.maxOfOrNull { it.id } ?: 0   // 리스트 비어도 안전

            val newId = if (lastId < 10000) 10000 else lastId + 1

            reduce {
                state.copy(
                    firstWrite = true,
                    writeDiaryData = Diary(
                        id = newId,
                        date = userDataEtc2Value.drop(1),
                        state = "완료",
                        contents = "",
                        emotion = "emotion/smile.png"
                    )
                )
            }
        }

        reduce {
            state.copy(
                dialogState = "",
                writeFinish = false,
                userDataList = userDataList,
                photoDataList = photoDataList,
                diarySequence = diarySequence
            )
        }

    }

    private fun generateAiGreeting() = intent {
        reduce { state.copy(aiText = "AI가 분석하는중...") }

        val diaryCount = diaryDao.getAllDiaryData().count()

        val result = withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                // 유빈님이 넣으신 OpenAI 키
                val url = "https://api.openai.com/v1/chat/completions"

                val json = JSONObject().apply {
                    put("model", "gpt-3.5-turbo")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "당신은 일기를 쓰도록 용기를 주는 사람입니다. 이때까지 $diaryCount 개 일기를 적었다고 꼭 알려주고, 오늘 일기를 적을 수 있도록 응원 한마디 해주세요. 길지않게 두줄만 해주세요. 반말말고 존댓말로 해주세요")
                        })
                    })
                }

                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
//                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()

                    if (!response.isSuccessful) {
                        // 실패 시 로그 (401: 키 문제, 429: 한도 초과 등)
                        Log.e("OpenAI", "통신 실패 코드: ${response.code}")
                        Log.e("OpenAI", "에러 내용: $responseBody")
                        return@withContext "가볍게 하루를 정리해볼까요?"
                    }

                    Log.d("OpenAI", "통신 성공: $responseBody")

                    val jsonResponse = JSONObject(responseBody ?: "")
                    val aiMessage = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    Log.d("OpenAI", "추출된 메시지: $aiMessage")
                    aiMessage
                }
            } catch (e: Exception) {
                Log.e("OpenAI", "예외 발생: ${e.message}")
                e.printStackTrace()
                "가볍게 하루를 정리해볼까요?"
            }
        }

        reduce { state.copy(aiText = result.trim()) }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val isAvailable = capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                )

        Log.d("NetworkCheck", "Is Network Available: $isAvailable") // 로그 추가
        return isAvailable
    }

    fun handleImageSelection(context: Context, uri: Uri) = intent {
        // 1. 상태 초기화
        reduce { state.copy(isPhotoLoading = true) }

        val isUploadFinished = AtomicBoolean(false)
        val isAdClosed = AtomicBoolean(false)

        // 로딩 종료 여부를 판단하는 함수
        fun tryFinishLoading() = intent {
            if (isUploadFinished.get() && isAdClosed.get()) {
                reduce { state.copy(isPhotoLoading = false) }
            }
        }

        // --- 광고 분기 처리 로직 ---
        // 기존에 사진이 하나라도 있으면(photoDataList가 비어있지 않으면) 광고를 띄움
        val shouldShowAd = state.photoDataList.isNotEmpty()

        if (false) {
            // [광고를 보여주는 경우]
//            postSideEffect(DiaryWriteSideEffect.ShowInterstitialAd {
//                isAdClosed.set(true)
//                tryFinishLoading()
//            })
        } else {
            // [첫 사진이라 광고를 안 보여주는 경우]
            // 광고가 이미 닫힌 것으로 간주하여 true로 설정
            isAdClosed.set(true)
            // tryFinishLoading은 호출할 필요 없음 (업로드 끝나면 알아서 종료됨)
        }

        // 2. [병렬 실행] 이미지 처리 및 업로드 (백그라운드)
        viewModelScope.launch(Dispatchers.IO) {
            if (!isNetworkAvailable(context)) {
                postSideEffect(DiaryWriteSideEffect.Toast("인터넷 연결을 확인해주세요."))
                isUploadFinished.set(true)
                tryFinishLoading()
                return@launch
            }

            val localPath = saveImageToInternalStorage(context, uri)
            if (localPath != null) {
                uploadToFirebase(localPath) { firebaseUrl ->
                    intent {
                        if (firebaseUrl != null) {
                            viewModelScope.launch(Dispatchers.IO) {
                                val photoEntry = Photo(
                                    date = state.writeDiaryData.date,
                                    localPath = localPath,
                                    firebaseUrl = firebaseUrl,
                                    isSynced = true
                                )
                                photoDao.insert(photoEntry)
                                val updatedPhotos = photoDao.getSyncedPhotosByDate(state.writeDiaryData.date)

                                intent {
                                    reduce { state.copy(photoDataList = updatedPhotos) }
                                    isUploadFinished.set(true)
                                    tryFinishLoading()
                                }
                            }
                        } else {
                            File(localPath).delete()
                            postSideEffect(DiaryWriteSideEffect.Toast("업로드 실패"))
                            isUploadFinished.set(true)
                            tryFinishLoading()
                        }
                    }
                }
            } else {
                postSideEffect(DiaryWriteSideEffect.Toast("저장 오류"))
                isUploadFinished.set(true)
                tryFinishLoading()
            }
        }
    }

    private fun uploadToFirebase(localPath: String, onComplete: (String?) -> Unit) {
        val user = Firebase.auth.currentUser ?: return onComplete(null)

        val file = File(localPath)
        if (!file.exists()) return onComplete(null)

        val storageRef = Firebase.storage.reference
        // 보안을 위해 파일명 앞에 UID를 섞거나 랜덤값을 추가하는 것이 좋습니다.
        val imageRef = storageRef.child("users/${user.uid}/${file.name}")

        // 파일 읽기 -> XOR 뒤섞기 -> 업로드
        try {
            val compressedBytes = file.readBytes()
            val scrambledBytes = togglePrivacy(compressedBytes)

            imageRef.putBytes(scrambledBytes)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        onComplete(uri.toString())
                    }
                }
                .addOnFailureListener {
                    onComplete(null)
                }
        } catch (e: Exception) {
            onComplete(null)
        }
    }

    private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val fileName = "haru_photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            // 1. InputStream으로 비트맵 불러오기
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // 2. 파일 출력 스트림 준비
                FileOutputStream(file).use { outputStream ->
                    // 3. 압축하기 (JPEG, 품질 70~80% 권장)
                    // 품질을 100에서 80으로만 낮춰도 용량이 획기적으로 줄어듭니다.
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    private fun togglePrivacy(data: ByteArray): ByteArray {
        val key = 0xAF.toByte()
        return ByteArray(data.size) { i -> (data[i].toInt() xor key.toInt()).toByte() }
    }

    fun deleteImage(photo: Photo) = intent {
        // 1. 비동기 작업으로 파일과 DB 데이터 삭제
        val isDeleted = withContext(Dispatchers.IO) {
            try {
                // (1) 내부 저장소에서 실제 파일 삭제
                val file = File(photo.localPath)
                if (file.exists()) {
                    file.delete()
                }

                // (2) DB에서 해당 포토 엔티티 삭제
                photoDao.delete(photo) // DAO에 delete 메서드가 있다고 가정합니다.
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        if (isDeleted) {
            // 2. 최신 리스트로 UI 상태 갱신
            val updatedList = photoDao.getSyncedPhotosByDate(state.writeDiaryData.date)
            reduce {
                state.copy(
                    photoDataList = updatedList
                )
            }
        } else {
            // 실패 시 에러 처리 (선택 사항)
            // postSideEffect(DiaryWriteSideEffect.ShowToast("사진 삭제에 실패했습니다."))
        }

    }

    fun onCloseClick() = intent {
        reduce {
            state.copy(
                dialogState = "",
            )
        }
    }

    fun onDiaryFinishClick() = intent {
        println("내용 길이: ${state.writeDiaryData.contents.length}")

        if(state.writeDiaryData.contents.isNotEmpty()){

            //보상
            if(state.firstWrite && state.writeDiaryData.id < 10000){
                Log.e("DiaryViewModel", state.userDataList.find { it.id == "money" }!!.value)
                userDao.update(
                    id = "money",
                    value = (state.userDataList.find { it.id == "money" }!!.value.toInt() + 3).toString()
                )
                postSideEffect(DiaryWriteSideEffect.Toast("+3 햇살"))
            }

            diaryDao.update(state.writeDiaryData)
            reduce {
                state.copy(
                    writeFinish = true
                )
            }

            if(state.photoDataList.isNotEmpty()) {
                postSideEffect(DiaryWriteSideEffect.ShowReviewDialog)
            }

        }
    }

    @OptIn(OrbitExperimental::class)
    fun onContentsTextChange(contentsText: String) = blockingIntent {

        reduce {
            state.copy(writeDiaryData = state.writeDiaryData.copy(contents = contentsText))
        }

        if (contentsText.length > 0) {
            reduce {
                state.copy(
                    writePossible = true,
                )
            }
        } else {
            reduce {
                state.copy(
                    writePossible = false,
                )
            }
        }
    }

    fun onDialogStateChange(string: String) = intent {
        reduce {
            state.copy(dialogState = string)
        }
    }

    fun emotionChangeClick(emotion: String) = intent {
        val newWriteDiaryData = state.writeDiaryData
        newWriteDiaryData.emotion = emotion
        reduce {
            state.copy(
                writeDiaryData = newWriteDiaryData,
                dialogState = ""
            )
        }
    }

    fun clickPhotoChange(path: String) = intent {
        reduce {
            state.copy(
                clickPhoto = path
            )
        }
    }

}

@Immutable
data class DiaryWriteState(
    val userDataList: List<User> = emptyList(),
    val diaryDataList: List<Diary> = emptyList(),
    val diaryFilterDataList: List<Diary> = emptyList(),
    val photoDataList: List<Photo> = emptyList(),
    val diarySequence: Int = 0,

    val aiText: String = "",
    val clickPhoto: String = "",
    val writeDiaryData: Diary = Diary(date = "", contents = "", emotion = ""),
    val writePossible: Boolean = false,
    val searchText: String = "",
    val dialogState: String = "",
    val emotionFilter: String = "emotion/allEmotion.png",
    val firstWrite: Boolean = true,
    val writeFinish: Boolean = false,
    val isPhotoLoading: Boolean = false, // 로딩 상태 추가

)

//상태와 관련없는 것
sealed interface DiaryWriteSideEffect{
    class Toast(val message:String): DiaryWriteSideEffect

    object ShowReviewDialog : DiaryWriteSideEffect // ⬅️ 추가

    // 광고 시청을 요청하는 SideEffect (광고 종료 후 실행할 액션을 넘겨줌)
//    data class ShowInterstitialAd(val onAdClosed: () -> Unit) : DiaryWriteSideEffect
}