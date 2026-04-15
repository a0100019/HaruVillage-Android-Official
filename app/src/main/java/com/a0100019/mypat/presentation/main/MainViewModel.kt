package com.a0100019.mypat.presentation.main


import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.yml.charts.common.extensions.isNotNull
import com.a0100019.mypat.data.room.item.Item
import com.a0100019.mypat.data.room.item.ItemDao
import com.a0100019.mypat.data.room.letter.Letter
import com.a0100019.mypat.data.room.letter.LetterDao
import com.a0100019.mypat.data.room.area.Area
import com.a0100019.mypat.data.room.area.AreaDao
import com.a0100019.mypat.data.room.diary.Diary
import com.a0100019.mypat.data.room.diary.DiaryDao
import com.a0100019.mypat.data.room.english.EnglishDao
import com.a0100019.mypat.data.room.koreanIdiom.KoreanIdiomDao
import com.a0100019.mypat.data.room.pat.Pat
import com.a0100019.mypat.data.room.pat.PatDao
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.data.room.world.World
import com.a0100019.mypat.data.room.world.WorldDao
import com.a0100019.mypat.presentation.diary.DiarySideEffect
import com.a0100019.mypat.presentation.main.management.ManagementSideEffect
import com.a0100019.mypat.presentation.main.management.RewardAdManager
import com.a0100019.mypat.presentation.main.management.tryAcquireMedal
import com.a0100019.mypat.presentation.neighbor.privateChat.PrivateRoom
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDao: UserDao,
    private val worldDao: WorldDao,
    private val patDao: PatDao,
    private val itemDao: ItemDao,
    private val letterDao: LetterDao,
    private val areaDao: AreaDao,
    private val rewardAdManager: RewardAdManager,

) : ViewModel(), ContainerHost<MainState, MainSideEffect> {

    override val container: Container<MainState, MainSideEffect> = container(
        initialState = MainState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(MainSideEffect.Toast(message = throwable.message.orEmpty()))
                }
            }
        }
    )

    // 뷰 모델 초기화 시 모든 user 데이터를 로드
    init {
        loadData()
        startTenMinuteCountdown()
//        rewardAdManager.load()
    }

    fun loadData() = intent {
        // 병렬로 실행할 작업들을 viewModelScope.launch로 묶음
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 맵 데이터 가져오기
                val mapData = worldDao.getWorldDataById(1)

                //월드 데이터 가져오기
                val worldDataList = worldDao.getAllWorldData().drop(1)

                // 펫 flow 월드 데이터 리스트 가져오기
                val patFlowWorldDataList = worldDao.getFlowWorldDataListByType(type = "pat")
                    .map { list ->
                        list.map { patWorldData ->
                            patDao.getPatDataById(patWorldData.value)
                        }
                    }

                // 모든 오픈 된 데이터 가져오기
                val allPatDataList = patDao.getAllOpenPatData()
                val allItemDataList = itemDao.getAllOpenItemData()
                val allMapDataList = areaDao.getAllOpenAreaData()
                val allItemDataWithShadowList = itemDao.getAllItemDataWithShadow()

                val userFlowDataList = userDao.getAllUserDataFlow()
                val userDataList = userDao.getAllUserData()

                val allLetterData = letterDao.getAllLetterData()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val todayDate = LocalDate.now()

                val showLetterData = allLetterData.firstOrNull { item ->
                    item.state == "open" && run {
                        val itemDate = runCatching {
                            LocalDate.parse(item.date, formatter)
                        }.getOrNull() // 파싱 실패하면 null 반환
                        if (itemDate != null) {
                            val daysBetween = ChronoUnit.DAYS.between(itemDate, todayDate)
                            daysBetween in 0..30
                        } else {
                            false // 형식 안 맞으면 그냥 건너뜀
                        }
                    }
                }

                if (showLetterData != null) {
                    reduce {
                        state.copy(
                            showLetterData = showLetterData
                        )
                    }
                }

                //로그아웃 후 첫 로그인이 아닐 경우
                val authData = userDataList.find { it.id == "auth" }
                if(authData != null && authData.value3 != "0"){
                    val storedTime = authData.value3.toLong()

                    val now = System.currentTimeMillis()
                    val timeGap = now - storedTime
                    val loveCount = (timeGap / (10 * 60 * 1000)).toInt()  // 10분 단위 몇 번 지났는지
                    val patLoveList =
                        worldDataList.filter { it.type == "pat" && it.situation != "love" }

                    // 선택 가능한 최대 개수만큼 랜덤으로 고름
                    val selectedList = patLoveList.shuffled().take(loveCount)

                    // Room 업데이트 (각각 개별적으로)
                    selectedList.forEach { pat ->
                        worldDao.updateSituationById(pat.id, "love")
                    }

                    // 상태 업데이트는 딱 한 번만
                    val updatedWorldList = worldDataList.map { item ->
                        if (selectedList.any { it.id == item.id }) {
                            item.copy(situation = "love")
                        } else item
                    }
                    reduce { state.copy(worldDataList = updatedWorldList) }

                    val timestamp = System.currentTimeMillis() + 1
                    userDao.update(id = "auth", value3 = timestamp.toString())

                } else {

                    //로그아웃 후 첫 로그인
                    reduce { state.copy(worldDataList = worldDataList) }

                    val timestamp = System.currentTimeMillis() + 1
                    userDao.update(id = "auth", value3 = timestamp.toString())

                }

                // UI 상태 업데이트 (Main Dispatcher에서 실행)
                withContext(Dispatchers.Main) {
                    reduce {
                        state.copy(
                            mapData = mapData,
                            patDataList = allPatDataList,
                            userFlowDataList = userFlowDataList,
                            itemDataList = allItemDataList,
                            allAreaDataList = allMapDataList,
                            patFlowWorldDataList = patFlowWorldDataList,
                            userDataList = userDataList,
                            itemDataWithShadowList = allItemDataWithShadowList
                        )
                    }
                }

//                postSideEffect(MainSideEffect.NavigateToDailyScreen)

            } catch (e: Exception) {
                Log.e("DataLoadError", "데이터 로드 중 에러 발생: ${e.message}", e)
                postSideEffect(MainSideEffect.Toast("데이터 로드 에러"))
            }

        }
    }

    fun dialogPatIdChange(clickId : String) = intent {
        userDao.update(id = "selectPat", value = clickId)
        reduce {
            state.copy(dialogPatId = clickId)
        }
        if(clickId != "0"){
            val patData = patDao.getPatDataById(clickId)
            if (patData.love >= 10000) {
                letterDao.updateTitleAndOpenState(
                    oldTitle = "의 편지",
                    newTitle = "${patData.name}의 편지",
                    todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
            }
        }
        loadData()
    }

    fun onLovePatChange(patId: Int) = intent {
        if(patId == 0){
            reduce {
                state.copy(
                    lovePatData = Pat(url = "")
                )
            }
        } else {

            val newWorldData = state.worldDataList.find { it.value == patId.toString() && it.type == "pat" }
            if (newWorldData != null) worldDao.update(newWorldData.copy(situation = ""))

            reduce {
                state.copy(
                    lovePatData = state.patDataList.find { it.id == patId }!!,
                    situation = "lovePatOnGoing"
                )
            }
        }
    }

    fun onSituationChange(situation: String) = intent {
        reduce {
            state.copy(
                situation = situation
            )
        }
    }

    fun onLetterLinkClick() = intent {
        val url = state.showLetterData.link
        postSideEffect(MainSideEffect.OpenUrl(url))
    }

    //편지 보상받기
    fun onLetterReadClick() = intent {

        val letterData = state.showLetterData

        when (letterData.reward) {
            "money" -> {
                postSideEffect(MainSideEffect.Toast("햇살 +${letterData.amount}"))
                userDao.update(id = "money", value = (state.userDataList.find { it.id == "money" }!!.value.toInt() + letterData.amount.toInt()).toString())
            }
            "cash" -> {
                postSideEffect(MainSideEffect.Toast("달빛 +${letterData.amount}"))
                userDao.update(id = "money", value2 = (state.userDataList.find { it.id == "money" }!!.value2.toInt() + letterData.amount.toInt()).toString())
            }
            else -> {
                val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: return@intent
                val (updated, acquired) = tryAcquireMedal(currentMedals, letterData.reward.toInt())
                if (acquired) {
                    userDao.update(id = "etc", value3 = updated)
                    postSideEffect(MainSideEffect.Toast("칭호를 획득했습니다!"))
                }
            }
        }

        letterData.state = "read"
        letterDao.update(letterData)

        reduce {
            state.copy(
                showLetterData = Letter()
            )
        }

        onSituationChange("")
        loadData()

    }

    fun onLoveItemClick(itemId: String) = intent {
        if(state.situation == "lovePatOnGoing"){

            reduce {
                state.copy(
                    situation = "lovePatStop",
                    musicTrigger = 0
                )
            }
            lovePatCheck(itemId)

        }
    }

    private fun lovePatCheck(itemId : String) = intent {
        val successId = (1..3).random().toString()

        if(successId == itemId) {
            //펫이 원하는 장난감
            reduce {
                state.copy(
                    situation = "lovePatSuccess",
                    loveAmount = state.loveAmount*2,
                    cashAmount = state.cashAmount*2
                )
            }
        } else {
            reduce {
                state.copy(
                    situation = "lovePatFail"
                )
            }
        }
    }

    fun onLovePatNextClick() = intent {
        reduce {
            state.copy(
                situation = "lovePatOnGoing",
                loveItemData1 = state.loveItemData1.copy(x = 0.1f, y = 0.8f, date = "0"),
                loveItemData2 = state.loveItemData2.copy(x = 0.4f, y = 0.8f, date = "0"),
                loveItemData3 = state.loveItemData3.copy(x = 0.7f, y = 0.8f, date = "0")
            )
        }
    }

    fun onLovePatStopClick() = intent {

        patDao.update(state.lovePatData.copy(love = state.lovePatData.love + state.loveAmount))
        userDao.update(id = "money", value2 = (userDao.getValue2ById("money").toInt()+state.cashAmount).toString() )

        val totalLove = state.lovePatData.love + state.loveAmount
        var currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: ""

        if (totalLove >= 500000) {
            val (updated, acquired) = tryAcquireMedal(currentMedals, 2)
            if (acquired) {
                currentMedals = updated
                userDao.update(id = "etc", value3 = updated)
                postSideEffect(MainSideEffect.Toast("칭호를 획득했습니다!"))
            }
        }

        if (totalLove >= 1000000) {
            val (updated, acquired) = tryAcquireMedal(currentMedals, 3)
            if (acquired) {
                userDao.update(id = "etc", value3 = updated)
                postSideEffect(MainSideEffect.Toast("칭호를 획득했습니다!"))
            }
        }

        val endWorldData = state.worldDataList.find { it.value == state.lovePatData.id.toString() && it.type == "pat" }
        if (endWorldData != null) worldDao.update(endWorldData.copy(situation = ""))

        reduce {
            state.copy(
                lovePatData = Pat(url = ""),
                loveAmount = 1000,
                cashAmount = 100,
                loveItemData1 = state.loveItemData1.copy(x = 0.1f, y = 0.8f, date = "0"),
                loveItemData2 = state.loveItemData2.copy(x = 0.4f, y = 0.8f, date = "0"),
                loveItemData3 = state.loveItemData3.copy(x = 0.7f, y = 0.8f, date = "0"),
                situation = ""
            )
        }

        loadData()

    }

    //하트 타이머
    private var hasFiredThisCycle = false
    private var timerJob: Job? = null
    @SuppressLint("DefaultLocale")
    private fun startTenMinuteCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = now
                }
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)

                val elapsedInCurrentBlock = (minute % 10) * 60 + second
                val remainingSeconds = 600 - elapsedInCurrentBlock
                val showRemainingSeconds = 600 - elapsedInCurrentBlock -1

                val minutesLeft = showRemainingSeconds / 60
                val secondsLeft = showRemainingSeconds % 60

                val timeString = String.format("%01d:%02d", minutesLeft, secondsLeft)

                intent {
                    reduce { state.copy(timer = timeString) }
                }

                if (remainingSeconds <= 1 && !hasFiredThisCycle) {
                    hasFiredThisCycle = true
                    onTenMinuteTimerExpired()
                    Log.e("MainViewModel", "타이머 만료 실행")
                }

                if (remainingSeconds > 1) {
                    hasFiredThisCycle = false // 새 주기 시작
                }

                delay(1000L)
            }
        }
    }

    //하트 추가
    private fun onTenMinuteTimerExpired() {
        intent {
            val patList = state.worldDataList.filter { it.type == "pat" && it.situation != "love" }
            if (patList.isNotEmpty()) {
                val targetPat = patList.random()

                // Room 업데이트
                worldDao.updateSituationById(targetPat.id, "love")

                // 상태 업데이트
                val updatedList = state.worldDataList.map {
                    if (it.id == targetPat.id) it.copy(situation = "love") else it
                }
                reduce { state.copy(worldDataList = updatedList) }

            }

            val timestamp = System.currentTimeMillis() + 1
            userDao.update(id = "auth", value3 = timestamp.toString())

        }
    }

    fun checkNewMessage() = intent {

        val myTag = userDao.getAllUserData()
            .find { it.id == "auth" }?.value2 ?: return@intent

        Firebase.firestore
            .collection("chatting")
            .document("privateChat")
            .collection("privateChat")
            .whereArrayContains("participants", myTag)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) {
                    intent { reduce { state.copy(newMessage = false) } }
                    return@addOnSuccessListener
                }

                var checked = 0
                var hasUnread = false
                val total = snapshot.size()

                snapshot.documents.forEach { roomDoc ->

                    val roomId = roomDoc.id
                    val user1 = roomDoc.getString("user1") ?: ""
                    val user2 = roomDoc.getString("user2") ?: ""
                    val last1 = roomDoc.getLong("last1") ?: 0L
                    val last2 = roomDoc.getLong("last2") ?: 0L

                    val myLast = if (myTag == user1) last1 else last2

                    Firebase.firestore
                        .collection("chatting")
                        .document("privateChat")
                        .collection("privateChat")
                        .document(roomId)
                        .collection("message")
                        .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { dateSnap ->

                            if (!dateSnap.isEmpty) {
                                val dateDoc = dateSnap.documents.first()
                                val data = dateDoc.data ?: emptyMap()

                                for (key in data.keys) {
                                    val ts = key.toLongOrNull() ?: continue
                                    if (ts > myLast) {
                                        hasUnread = true
                                        break
                                    }
                                    Log.d("UnreadCheck", "room=$roomId myLast=$myLast ts=$ts")
                                }
                            }

                            checked++
                            if (hasUnread || checked == total) {
                                intent {
                                    reduce {
                                        state.copy(newMessage = hasUnread)
                                    }
                                }
                            }
                        }

                }
            }
            .addOnFailureListener {
                intent { reduce { state.copy(newMessage = false) } }
            }
    }

    fun onExitClick() = intent {
        postSideEffect(MainSideEffect.ExitApp)
    }

    fun onCloseClick() = intent {
        reduce {
            state.copy(
                situation = ""
            )
        }
    }

}

@Immutable
data class MainState(
    val userFlowDataList: Flow<List<User>> = flowOf(emptyList()),
    val patDataList: List<Pat> = emptyList(),
    val itemDataList: List<Item> = emptyList(),
    val allAreaDataList: List<Area> = emptyList(),
    val patFlowWorldDataList: Flow<List<Pat>> = flowOf(emptyList()),
    val worldDataList: List<World> = emptyList(),
    val userDataList: List<User> = emptyList(),
    val letterDataList: List<Letter> = emptyList(),
    val letterImages: List<String> = emptyList(),
    val itemDataWithShadowList: List<Item> = emptyList(),

    val mapData: World = World(),
    val dialogPatId: String = "0",
    val showLetterData: Letter = Letter(),
    val situation: String = "",
    val lovePatData: Pat = Pat(url = ""),
    val loveItemData1: Item = Item(id = 1, name = "쓰다듬기", url = "etc/toy_car.png", x = 0.1f, y = 0.8f, sizeFloat = 0.2f),
    val loveItemData2: Item = Item(id = 2, name = "장난감", url = "etc/toy_lego.png", x = 0.4f, y = 0.8f, sizeFloat = 0.2f),
    val loveItemData3: Item = Item(id = 3, name = "비행기", url = "etc/toy_bear.png", x = 0.7f, y = 0.8f, sizeFloat = 0.2f),
    val loveAmount: Int = 1000,
    val cashAmount: Int = 100,
    val timer: String = "10:00",
    val musicTrigger: Int = 0,
    val newMessage: Boolean = false

    )

//상태와 관련없는 것
sealed interface MainSideEffect{
    class Toast(val message:String): MainSideEffect
    data class OpenUrl(val url: String) : MainSideEffect
    data object NavigateToDailyScreen: MainSideEffect

    object ExitApp : MainSideEffect   // 앱 종료용

}