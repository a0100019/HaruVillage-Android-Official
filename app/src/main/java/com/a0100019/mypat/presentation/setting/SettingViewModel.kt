package com.a0100019.mypat.presentation.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a0100019.mypat.data.remote.NoticeService
import com.a0100019.mypat.data.room.area.Area
import com.a0100019.mypat.data.room.diary.Diary
import com.a0100019.mypat.data.room.diary.DiaryDao
import com.a0100019.mypat.data.room.diary.getDiaryInitialData
import com.a0100019.mypat.data.room.english.English
import com.a0100019.mypat.data.room.english.EnglishDao
import com.a0100019.mypat.data.room.english.getEnglishInitialData
import com.a0100019.mypat.data.room.item.Item
import com.a0100019.mypat.data.room.item.ItemDao
import com.a0100019.mypat.data.room.item.getItemInitialData
import com.a0100019.mypat.data.room.koreanIdiom.KoreanIdiom
import com.a0100019.mypat.data.room.koreanIdiom.KoreanIdiomDao
import com.a0100019.mypat.data.room.koreanIdiom.getKoreanIdiomInitialData
import com.a0100019.mypat.data.room.letter.Letter
import com.a0100019.mypat.data.room.letter.LetterDao
import com.a0100019.mypat.data.room.letter.getLetterInitialData
import com.a0100019.mypat.data.room.area.AreaDao
import com.a0100019.mypat.data.room.area.getAreaInitialData
import com.a0100019.mypat.data.room.knowledge.Knowledge
import com.a0100019.mypat.data.room.knowledge.KnowledgeDao
import com.a0100019.mypat.data.room.knowledge.getKnowledgeInitialData
import com.a0100019.mypat.data.room.pat.Pat
import com.a0100019.mypat.data.room.pat.PatDao
import com.a0100019.mypat.data.room.pat.getPatInitialData
import com.a0100019.mypat.data.room.photo.PhotoDao
import com.a0100019.mypat.data.room.sudoku.Sudoku
import com.a0100019.mypat.data.room.sudoku.SudokuDao
import com.a0100019.mypat.data.room.sudoku.getSudokuInitialData
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.data.room.user.getUserInitialData
import com.a0100019.mypat.data.room.walk.Walk
import com.a0100019.mypat.data.room.walk.WalkDao
import com.a0100019.mypat.data.room.walk.getWalkInitialData
import com.a0100019.mypat.data.room.world.World
import com.a0100019.mypat.data.room.world.WorldDao
import com.a0100019.mypat.data.room.world.getWorldInitialData
import com.a0100019.mypat.presentation.main.management.addMedalAction
import com.a0100019.mypat.presentation.main.management.getMedalActionCount
import com.a0100019.mypat.presentation.main.management.tryAcquireMedal
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import java.time.LocalDateTime
import javax.annotation.concurrent.Immutable
import javax.inject.Inject


@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userDao: UserDao,
    private val patDao: PatDao,
    private val itemDao: ItemDao,
    private val diaryDao: DiaryDao,
    private val englishDao: EnglishDao,
    private val koreanIdiomDao: KoreanIdiomDao,
    private val sudokuDao: SudokuDao,
    private val walkDao: WalkDao,
    private val worldDao: WorldDao,
    private val letterDao: LetterDao,
    private val areaDao: AreaDao,
    private val knowledgeDao: KnowledgeDao,
    private val photoDao: PhotoDao,
    private val noticeService: NoticeService, // Hilt로 주입받은 서비스

) : ViewModel(), ContainerHost<SettingState, SettingSideEffect> {

    override val container: Container<SettingState, SettingSideEffect> = container(
        initialState = SettingState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(SettingSideEffect.Toast(message = throwable.message.orEmpty()))
                }
            }
        }
    )

    // 뷰 모델 초기화 시 모든 user 데이터를 로드
    init {
        loadData()
        loadDonationList()
    }

    //room에서 데이터 가져옴
    private fun loadData() = intent {
        val userDataList = userDao.getAllUserData()
        val itemDataList = itemDao.getAllItemDataWithShadow()
        val patDataList = patDao.getAllPatData()
        val worldDataList = worldDao.getAllWorldData()
        val letterDataList = letterDao.getNotWaitingLetterData()
        val sortedLetterList = letterDataList.sortedByDescending { letter ->
            LocalDate.parse(letter.date)
        }
        val walkDataList = walkDao.getAllWalkData()
        val englishDataList = englishDao.getOpenEnglishData()
        val koreanIdiomDataList = koreanIdiomDao.getOpenKoreanIdiomData()
        val diaryDataList = diaryDao.getAllDiaryData()
        val sudokuDataList = sudokuDao.getAllSudokuData()
        val areaDataList = areaDao.getAllAreaData()
        val knowledgeDataList = knowledgeDao.getAllKnowledgeData()

        reduce {
            state.copy(
                userDataList = userDataList,
                itemDataList = itemDataList,
                patDataList = patDataList,
                worldDataList = worldDataList,
                letterDataList = sortedLetterList,
                walkDataList = walkDataList,
                englishDataList = englishDataList,
                koreanIdiomDataList = koreanIdiomDataList,
                diaryDataList = diaryDataList,
                sudokuDataList = sudokuDataList,
                areaDataList = areaDataList,
                knowledgeDataList = knowledgeDataList
            )
        }
    }

    fun onCloseClick() = intent {
        reduce {
            state.copy(
                settingSituation = "",
                editText = "",
                clickLetterData = Letter(),
                recommending = "-1",
                recommended = "-1"
            )
        }
    }

    fun onMedal19Click() = intent {
        //매달, medal, 칭호19
        val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: return@intent
        val (updated, acquired) = tryAcquireMedal(currentMedals, 19)
        if (acquired) {
            userDao.update(id = "etc", value3 = updated)
            postSideEffect(SettingSideEffect.Toast("칭호를 획득했습니다!"))
        }

    }

    fun onSituationChange(situation: String) = intent {

        if(situation == "explanation") {
            var medalData = userDao.getAllUserData().find { it.id == "name" }!!.value2
            medalData = addMedalAction(medalData, actionId = 18)
            userDao.update(
                id = "name",
                value2 = medalData
            )

            if (getMedalActionCount(medalData, actionId = 18) >= 3) {
                //매달, medal, 칭호18
                val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: ""
                val (updated, acquired) = tryAcquireMedal(currentMedals, 18)
                if (acquired) {
                    userDao.update(id = "etc", value3 = updated)
                    postSideEffect(SettingSideEffect.Toast("칭호를 획득했습니다!"))
                }
            }
        }

        reduce {
            state.copy(
                settingSituation = situation
            )
        }
    }

    private fun onSignOutClick() = intent {

        FirebaseAuth.getInstance().signOut()
        // 현재 사용자 null이면 로그아웃 성공
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // 로그아웃 성공
            // 사용자 데이터 삭제
//            userDao.update(id = "auth", value = "0")
            roomDataClear()
            postSideEffect(SettingSideEffect.Toast("로그아웃 되었습니다"))
            postSideEffect(SettingSideEffect.NavigateToLoginScreen)
        } else {
            // 로그아웃 실패
            postSideEffect(SettingSideEffect.Toast("로그아웃에 실패했습니다"))
        }

    }

    fun dataSave() = intent {

        try {
        val db = Firebase.firestore
        val userId = state.userDataList.find { it.id == "auth" }?.value ?: return@intent
        val userDataList = state.userDataList
        val itemDataList = state.itemDataList
        val patDataList = state.patDataList
        val worldDataList = state.worldDataList
        val walkDataList = state.walkDataList
        val englishDataList = state.englishDataList
        val diaryDataList = state.diaryDataList
        val koreanIdiomDataList = state.koreanIdiomDataList
        val letterDataList = state.letterDataList
        val sudokuDataList = state.sudokuDataList
        val areaDataList = state.areaDataList
        val knowledgeDataList = state.knowledgeDataList

        val batch = db.batch()

        val userData = mapOf(
            "cash" to userDataList.find { it.id == "money"}!!.value2,
            "money" to userDataList.find { it.id == "money"}!!.value,
            "stepsRaw" to userDataList.find { it.id == "etc2" }!!.value2,
            "pay" to userDataList.find { it.id == "name"}!!.value3,

            "community" to mapOf(
                "ban" to userDataList.find { it.id == "community"}!!.value3,
//                "like" to userDataList.find { it.id == "community"}!!.value,
                "warning" to userDataList.find {it.id == "community"}!!.value2,
                "medal" to userDataList.find { it.id == "etc"}!!.value3,
                "introduction" to userDataList.find { it.id == "etc"}!!.value,
                "medalQuest" to userDataList.find { it.id == "name"}!!.value2,
                "medalCount" to userDataList.find { it.id == "etc"}!!.value3.count { it == '/' },
            ),

            "date" to mapOf(
                "firstDate" to userDataList.find { it.id == "date"}!!.value3,
                "totalDate" to userDataList.find { it.id == "date"}!!.value2,
                "lastDate" to userDataList.find { it.id == "date"}!!.value
            ),

            "game" to mapOf(
                "firstGame" to userDataList.find { it.id == "firstGame"}!!.value,
                "secondGame" to userDataList.find { it.id == "secondGame"}!!.value,
                "thirdGameEasy" to userDataList.find { it.id == "thirdGame"}!!.value,
                "thirdGameNormal" to userDataList.find { it.id == "thirdGame"}!!.value2,
                "thirdGameHard" to userDataList.find { it.id == "thirdGame"}!!.value3,
                ),

            "item" to mapOf(
                "openItem" to itemDataList.count { it.date != "0"}.toString(),
                "openItemSpace" to userDataList.find { it.id == "item"}!!.value2,
                "useItem" to userDataList.find { it.id == "item"}!!.value3
            ),

            "pat" to mapOf(
                "openPat" to patDataList.count { it.date != "0"}.toString(),
                "openPatSpace" to userDataList.find { it.id == "pat"}!!.value2,
                "usePat" to userDataList.find { it.id == "pat"}!!.value3
            ),

            "area" to worldDataList.find { it.id == 1}!!.value,
            "name" to userDataList.find { it.id == "name"}!!.value,
            "lastLogin" to userDataList.find { it.id == "auth"}!!.value3,
            "tag" to userDataList.find { it.id == "auth"}!!.value2,
            "openArea" to areaDataList.count { it.date != "0"}.toString(),

            "online" to "0",

            "walk" to mapOf(
                "saveWalk" to userDataList.find { it.id == "walk"}!!.value,
                "totalWalk" to userDataList.find { it.id == "walk"}!!.value3,
            )

        )

            // 월드 데이터 만들기
            val worldMap = worldDataList.drop(1)
                .mapIndexed { index, data ->
                    if (data.type == "pat") {
                        val patData = patDataList.find { it.id == data.value.toInt() }
                        index.toString() to mapOf(
                            "id" to data.value,
                            "size" to patData!!.sizeFloat.toString(),
                            "type" to data.type,
                            "x" to patData.x.toString(),
                            "y" to patData.y.toString(),
                            "effect" to patData.effect.toString()
                        )
                    } else {
                        val itemData = itemDataList.find { it.id == data.value.toInt() }
                        index.toString() to mapOf(
                            "id" to data.value,
                            "size" to itemData!!.sizeFloat.toString(),
                            "type" to data.type,
                            "x" to itemData.x.toString(),
                            "y" to itemData.y.toString(),
                            "effect" to "0"
                        )
                    }
                }
                .toMap()

            val userDocRef = Firebase.firestore.collection("users").document(userId)

            // 1) 문서 보장 (없으면 생성)
            batch.set(userDocRef, emptyMap<String, Any>(), SetOptions.merge())

            // 2) 기존 world 필드 제거
            batch.update(userDocRef, mapOf("world" to FieldValue.delete()))

            // 3) userData + 새 world 필드 병합 저장
            val finalData = userData + mapOf("world" to worldMap)
            batch.set(userDocRef, finalData, SetOptions.merge())

        //펫 데이터 저장
        val patCollectionRef = db.collection("users")
            .document(userId)
            .collection("dataCollection")

        val combinedPatData = mutableMapOf<String, Any>()
        patDataList
        .filter { it.date != "0" }
        .forEach { patData ->
            val patMap = mapOf(
                "date" to patData.date,
                "love" to patData.love.toString(),
                "size" to patData.sizeFloat.toString(),
                "x" to patData.x.toString(),
                "y" to patData.y.toString(),
                "gameCount" to patData.gameCount.toString(),
                "effect" to patData.effect.toString()
            )
            combinedPatData[patData.id.toString()] = patMap
        }
        batch.set(patCollectionRef.document("pats"), combinedPatData)

        val itemCollectionRef = db.collection("users")
            .document(userId)
            .collection("dataCollection")

        val combinedItemData = mutableMapOf<String, Any>()
        itemDataList
            .filter { it.date != "0" }
            .forEach { itemData ->
                val itemMap = mapOf(
                    "date" to itemData.date,
                    "size" to itemData.sizeFloat.toString(),
                    "x" to itemData.x.toString(),
                    "y" to itemData.y.toString()
                )
                combinedItemData[itemData.id.toString()] = itemMap
            }
        batch.set(itemCollectionRef.document("items"), combinedItemData)

        val areaCollectionRef = db.collection("users")
            .document(userId)
            .collection("dataCollection")

        val combinedMapData = mutableMapOf<String, Any>()
        areaDataList
            .filter { it.date != "0" }
            .forEach { areaData ->
                val areaMap = mapOf(
                    "date" to areaData.date,
                )
                combinedMapData[areaData.id.toString()] = areaMap
            }
        batch.set(areaCollectionRef.document("areas"), combinedMapData)

        val letterCollectionRef = db.collection("users")
        .document(userId)
        .collection("dataCollection")

        val combinedLetterData = mutableMapOf<String, Any>()
        letterDataList.forEach { letterData ->
            val letterMap = mapOf(
                "date" to letterData.date,
                "title" to letterData.title,
                "message" to letterData.message,
                "link" to letterData.link,
                "reward" to letterData.reward,
                "amount" to letterData.amount,
                "state" to letterData.state,
            )
            combinedLetterData[letterData.id.toString()] = letterMap
        }
        // 하나의 문서에 전체 데이터를 저장
        batch.set(letterCollectionRef.document("letters"), combinedLetterData)

        val sudokuCollectionRef = db.collection("users")
            .document(userId)
            .collection("dataCollection")
            .document("sudoku")

        val sudokuData = mapOf(
            "sudokuBoard" to sudokuDataList.find {it.id == "sudokuBoard"}!!.value,
            "sudokuFirstBoard" to sudokuDataList.find {it.id == "sudokuFirstBoard"}!!.value,
            "sudokuMemoBoard" to sudokuDataList.find {it.id == "sudokuMemoBoard"}!!.value,
            "time" to sudokuDataList.find {it.id == "time"}!!.value,
            "level" to sudokuDataList.find {it.id == "level"}!!.value,
            "state" to sudokuDataList.find {it.id == "state"}!!.value
            )
        batch.set(sudokuCollectionRef, sudokuData)

        val dailyCollectionRef = db.collection("users")
            .document(userId)
            .collection("daily")

            val photosByDate = photoDao.getAllPhotoData().filter { it.isSynced }.groupBy { it.date }

        diaryDataList.forEach { diary ->
            val docRef = dailyCollectionRef.document(diary.id.toString())
            val date = diary.date

            // 1. 해당 일기의 날짜와 일치하는 모든 사진 가져오기
            // photoList는 Room 등에서 미리 전체 리스트를 뽑아온 것으로 가정합니다.
            val photosForDate = photosByDate[date] ?: emptyList()

            // 2. 사진 데이터를 Map 형태로 변환 (키값은 1, 2, 3... 순서대로)
            val photoMap = mutableMapOf<String, Any>()
            photosForDate.forEachIndexed { index, photo ->
                photoMap[(index + 1).toString()] = mapOf(
                    "firebaseUrl" to photo.firebaseUrl,
                    "localPath" to photo.localPath
                )
            }

            val data = mutableMapOf<String, Any>(
                "date" to date,
                "diary" to mapOf(
                    "emotion" to diary.emotion,
                    "state" to diary.state,
                    "contents" to diary.contents
                )
            )

            // 3. 사진 데이터가 있으면 추가
            if (photoMap.isNotEmpty()) {
                data["photo"] = photoMap
            }

            // --- 기존의 walk, state, knowledge 처리 ---
            val walk = walkDataList.find { it.id == diary.id }?.success
            if (walk != null) data["walk"] = walk

            val englishState = englishDataList.find { it.id == diary.id }?.state
            val idiomState = koreanIdiomDataList.find { it.id == diary.id }?.state
            if (englishState != null && idiomState != null) {
                data["state"] = mapOf("english" to englishState, "koreanIdiom" to idiomState)
            }

            val knowledgeState = knowledgeDataList.find { it.date == date }?.state
            if (knowledgeState != null) data["knowledge"] = knowledgeState

            batch.set(docRef, data)
        }

        Log.d("Firestore", "batch.commit() 직전")

        // 전체 커밋 실행
        batch.commit()
            .addOnSuccessListener {
                onSignOutClick()
            }
            .addOnFailureListener {
                Log.e("Firestore", "저장 실패", it)
            }

        } catch (e: Exception) {
            Log.e("Firestore", "예외 발생", e)
        }
    }

    fun onAccountDeleteClick() = intent {
        if(state.editText == "계정삭제"){
            reduce {
                state.copy(
                    settingSituation = ""
                )
            }

            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            val userDocRef =
                db.collection("users").document(state.userDataList.find { it.id == "auth" }?.value ?: return@intent)
            val subCollections =
                listOf("daily", "dataCollection", "community", "code")

            try {
                // 1. 서브컬렉션 안의 문서 삭제
                for (sub in subCollections) {
                    val subColRef = userDocRef.collection(sub)
                    val documents = subColRef.get().await().documents
                    for (doc in documents) {
                        doc.reference.delete().await()
                    }
                }

                // 2. 마지막으로 사용자 문서 삭제
                userDocRef.delete().await()

                // 2. Authentication 계정 삭제
                auth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        viewModelScope.launch {
                            roomDataClear()
                            postSideEffect(SettingSideEffect.Toast("계정이 삭제되었습니다."))
                            postSideEffect(SettingSideEffect.NavigateToLoginScreen)
                        }
                        Log.d("Auth", "계정 삭제 완료")
                    }
                    ?.addOnFailureListener {
                        Log.e("Auth", "계정 삭제 실패", it)
                        viewModelScope.launch {
                            postSideEffect(SettingSideEffect.Toast("다시 로그인 후 재시도 해주세요."))
                        }
                    }
                Log.d("Firestore", "사용자 전체 삭제 완료")
            } catch (e: Exception) {
                Log.e("Firestore", "삭제 실패", e)
            }

        } else {
            postSideEffect(SettingSideEffect.Toast("[계정삭제]를 입력해주세요."))
        }

    }

    private fun roomDataClear() = intent {
        // 모든 유저 데이터 삭제
        userDao.deleteAllUsers()
        // 초기 데이터 삽입
        val initialUserData = getUserInitialData()
        userDao.insertAll(initialUserData)

        diaryDao.deleteAllDiary()
        diaryDao.resetDiaryPrimaryKey()
        val initialDiaryData = getDiaryInitialData()
        diaryDao.insertAll(initialDiaryData)

        englishDao.deleteAllEnglish()
        englishDao.resetEnglishPrimaryKey()
        val initialEnglishData = getEnglishInitialData()
        englishDao.insertAll(initialEnglishData)

        itemDao.deleteAllItems()
        itemDao.resetItemPrimaryKey()
        val initialItemData = getItemInitialData()
        itemDao.insertAll(initialItemData)

        koreanIdiomDao.deleteAllKoreanIdioms()
        koreanIdiomDao.resetKoreanIdiomPrimaryKey()
        val initialKoreanIdiomData = getKoreanIdiomInitialData()
        koreanIdiomDao.insertAll(initialKoreanIdiomData)

        letterDao.deleteAllLetters()
        letterDao.resetLetterPrimaryKey()
        val initialLetterData = getLetterInitialData()
        letterDao.insertAll(initialLetterData)

        patDao.deleteAllPats()
        patDao.resetPatPrimaryKey()
        val initialPatData = getPatInitialData()
        patDao.insertAll(initialPatData)

        sudokuDao.deleteAllSudoku()
        val initialSudokuData = getSudokuInitialData()
        sudokuDao.insertAll(initialSudokuData)

        walkDao.deleteAllWalks()
        walkDao.resetWalkPrimaryKey()
        val initialWalkData = getWalkInitialData()
        walkDao.insertAll(initialWalkData)

        worldDao.deleteAllWorlds()
        worldDao.resetWorldPrimaryKey()
        val initialWorldData = getWorldInitialData()
        worldDao.insertAll(initialWorldData)

        areaDao.deleteAllAreas()
        areaDao.resetAreaPrimaryKey()
        val initialAreaData = getAreaInitialData()
        areaDao.insertAll(initialAreaData)

        knowledgeDao.deleteAllKnowledge()
        knowledgeDao.resetKnowledgePrimaryKey()
        val initialKnowledgeData = getKnowledgeInitialData()
        knowledgeDao.insertAll(initialKnowledgeData)

        photoDao.deleteAllPhoto()
        photoDao.resetPhotoPrimaryKey()

    }

    fun onCouponConfirmClick() = intent {
        val db = Firebase.firestore
        val couponText = state.editText // 사용자가 입력한 쿠폰 코드
        val userId = state.userDataList.find { it.id == "auth" }?.value ?: return@intent

        if (couponText.length <= 1) return@intent

        db.collection("users").document(userId).collection("code").document("coupon")
            .get()
            .addOnSuccessListener { couponDocument ->
                if (couponDocument != null && couponDocument.contains(couponText)) {
                    // 이미 사용한 쿠폰
                    viewModelScope.launch {
                        postSideEffect(SettingSideEffect.Toast("이미 사용한 쿠폰 번호입니다."))
                    }
                } else {
                    // 아직 사용하지 않은 쿠폰이므로, 유효한 쿠폰인지 확인
                    db.collection("code").document("coupon")
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.contains(couponText)) {
                                val couponData = document.get(couponText) as? Map<*, *>
                                val reward = couponData?.get("reward") as? String
                                val type = couponData?.get("type") as? String
                                val amount = couponData?.get("amount") as? String

                                Log.d("Coupon", "내용: $reward, 금액: $amount")

                                val newCouponMap = mapOf(
                                    couponText to mapOf(
                                        "reward" to reward,
                                        "type" to type,
                                        "amount" to amount
                                    )
                                )

                                if(type == "all"){

                                    db.collection("users").document(userId)
                                        .collection("code").document("coupon")
                                        .set(newCouponMap, SetOptions.merge())
                                        .addOnSuccessListener {
                                            viewModelScope.launch {
                                                if (reward == "money") {
                                                    userDao.update(
                                                        id = "money",
                                                        value = (state.userDataList.find { it.id == "money" }!!.value.toInt() + amount!!.toInt()).toString()
                                                    )
                                                } else {
                                                    userDao.update(
                                                        id = "money",
                                                        value2 = (state.userDataList.find { it.id == "money" }!!.value2.toInt() + amount!!.toInt()).toString()
                                                    )
                                                }
                                                postSideEffect(SettingSideEffect.Toast("쿠폰 사용 : $reward +$amount"))
                                                onCloseClick()
                                                loadData()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Coupon", "쿠폰 저장 실패", e)
                                        }

                                } else if(type == "one"){

                                    db.collection("users").document(userId)
                                        .collection("code").document("coupon")
                                        .set(newCouponMap, SetOptions.merge())
                                        .addOnSuccessListener {
                                            // 필드 삭제 먼저 수행
                                            val deleteMap = mapOf<String, Any>(
                                                couponText to FieldValue.delete()
                                            )

                                            db.collection("code").document("coupon")
                                                .update(deleteMap)
                                                .addOnSuccessListener {
                                                    Log.d("Coupon", "기존 필드 $couponText 삭제 성공")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("Coupon", "기존 필드 $couponText 삭제 실패", e)
                                                }

                                            viewModelScope.launch {
                                                if (reward == "money") {
                                                    userDao.update(
                                                        id = "money",
                                                        value = (state.userDataList.find { it.id == "money" }!!.value.toInt() + amount!!.toInt()).toString()
                                                    )
                                                } else {
                                                    userDao.update(
                                                        id = "money",
                                                        value2 = (state.userDataList.find { it.id == "money" }!!.value2.toInt() + amount!!.toInt()).toString()
                                                    )
                                                }
                                                postSideEffect(SettingSideEffect.Toast("쿠폰 사용 : $reward +$amount"))
                                                onCloseClick()
                                                loadData()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Coupon", "쿠폰 저장 실패", e)
                                        }


                                }

                            } else {
                                // 존재하지 않는 쿠폰
                                viewModelScope.launch {
                                    postSideEffect(SettingSideEffect.Toast("존재하지 않는 쿠폰 번호입니다."))
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Coupon", "쿠폰 조회 실패", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Coupon", "사용자 쿠폰 조회 실패", e)
            }

        loadData()

    }

    fun onSettingTalkConfirmClick() = intent {
        val now = LocalDateTime.now().toString()

        val messageMap = mapOf(
            "message" to state.editText,
            "name" to state.userDataList.find { it.id == "name" }?.value.orEmpty(),
            "tag" to state.userDataList.find { it.id == "auth" }?.value2.orEmpty(),
        )

        Firebase.firestore
            .collection("code")
            .document("settingTalk")
            .collection("settingTalk")
            .document(state.userDataList.find { it.id == "auth" }?.value ?: return@intent)
            .set(
                mapOf(now to messageMap),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                viewModelScope.launch {
                    postSideEffect(SettingSideEffect.Toast("전송되었습니다."))
                    onCloseClick()
                }
            }

        //매달, medal, 칭호17
        val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: ""
        val (updated17, acquired17) = tryAcquireMedal(currentMedals, 17)
        if (acquired17) {
            userDao.update(id = "etc", value3 = updated17)
            postSideEffect(SettingSideEffect.Toast("칭호를 획득했습니다!"))
        }

        loadData()
    }


    fun clickLetterDataChange(letterId: Int) = intent {
        if(letterId != 0) {

            val clickLetterData = state.letterDataList.find { it.id == letterId }!!


            reduce {
                state.copy(
                    clickLetterData = clickLetterData
                    )
            }

        } else {

            reduce {
                state.copy(
                    clickLetterData = Letter(),
                )
            }
        }

    }

    fun onLetterLinkClick() = intent {
        val url = state.clickLetterData.link
        postSideEffect(SettingSideEffect.OpenUrl(url))
    }

    fun onLetterCloseClick() = intent {
        clickLetterDataChange(0)
        loadData()
    }

    fun onLetterConfirmClick() = intent {

        val letterData = state.clickLetterData

        if(letterData.state == "open"){
            when (letterData.reward) {
                "money" -> {
                    postSideEffect(SettingSideEffect.Toast("햇살 +${letterData.amount}"))
                    userDao.update(id = "money", value = (state.userDataList.find { it.id == "money" }!!.value.toInt() + letterData.amount.toInt()).toString())
                }
                "cash" -> {
                    postSideEffect(SettingSideEffect.Toast("달빛 +${letterData.amount}"))
                    userDao.update(id = "money", value2 = (state.userDataList.find { it.id == "money" }!!.value2.toInt() + letterData.amount.toInt()).toString())
                }
                else -> {
                    //매달, medal, 칭호
                    val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: return@intent
                    val (updated, acquired) = tryAcquireMedal(currentMedals, letterData.reward.toInt())
                    if (acquired) {
                        userDao.update(id = "etc", value3 = updated)
                        postSideEffect(SettingSideEffect.Toast("칭호를 획득했습니다!"))
                    }
                }
            }

            letterData.state = "read"
            letterDao.update(letterData)
            clickLetterDataChange(0)
            loadData()
        } else {
            clickLetterDataChange(0)
            loadData()
        }

    }

    fun onRecommendationClick() = intent {

        val recommendationDocRef = Firebase.firestore
            .collection("code")
            .document("recommendation")

        val tag = userDao.getValue2ById("auth")

        try {
            val snapshot = recommendationDocRef.get().await()
            val map = snapshot.data as? Map<String, String>

            var recommending = "0"
            var recommended = "0"

            map?.let {
                // tag == key 체크
                if (it.containsKey(tag)) {
                    recommending = it[tag] ?: "0"
                }

                // tag == value 체크
                val matchedEntry = it.entries.find { entry -> entry.value == tag }
                if (matchedEntry != null) {
                    recommended = matchedEntry.key
                }
            }

            Log.d("recommendation", "recommending=$recommending, recommended=$recommended")

            reduce {
                state.copy(
                    recommending = recommending,
                    recommended = recommended,
                    settingSituation = "recommendation"
                )
            }

        } catch (e: Exception) {
            Log.e("recommendation", "가져오기 실패", e)
            postSideEffect(SettingSideEffect.Toast("인터넷 오류"))
        }

    }

    fun onRecommendationSubmitClick() = intent {
        val myTag = userDao.getValue2ById("auth")
        val forTag = state.editText.trim()

        if (myTag == forTag) {
            postSideEffect(SettingSideEffect.Toast("본인을 추천할 수 없습니다."))
            return@intent
        }

        val tagDocRef  = Firebase.firestore.collection("tag").document("tag")
        val recoDocRef = Firebase.firestore.collection("code").document("recommendation")
        val letterDocRef = Firebase.firestore.collection("code").document("letter")

        try {
            // 1) forTag가 태그 문서의 "키"인지 확인
            val tagSnapshot = tagDocRef.get().await()
            val existsAsKey = (tagSnapshot.data as? Map<String, Any>)?.containsKey(forTag) == true
            if (!existsAsKey) {
                postSideEffect(SettingSideEffect.Toast("존재하지 않는 태그입니다."))
                return@intent
            }

            // 서로 추천 금지 체크: recommendation에서 forTag: myTag 가 이미 존재하면 금지
            val recoSnapshot = recoDocRef.get().await()
            val recoMap = recoSnapshot.data as? Map<String, Any> ?: emptyMap()
            val reciprocal = (recoMap[forTag] as? String) == myTag
            if (reciprocal) {
                postSideEffect(SettingSideEffect.Toast("서로 추천할 수 없습니다."))
                return@intent
            }

            // 2) 추천 등록: recommendation 문서에 myTag: forTag
            recoDocRef.update(myTag, forTag).await()
            postSideEffect(SettingSideEffect.Toast("#$forTag 님을 추천하였습니다. +5햇살"))
            reduce { state.copy(recommending = forTag) }

            // 3) letter 문서에 맵 필드 추가 (키 = yyyyMMdd + forTag)
            val today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
            val todayDate = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val letterFieldKey = "90$forTag" // 예: 2025091244
            val letterValue = mapOf(
                "amount" to "10",
                "date" to todayDate,
                "link" to "0",
                "message" to "안녕하세요 이웃님!\n\n#$myTag 님의 추천을 받았습니다. 하루마을을 위해 애써주셔서 진심으로 감사합니다. 이웃님의 정성과 마음이 헛되지 않도록, 하루마을은 앞으로도 꾸준히 성장하며 더 따뜻한 공간이 되겠습니다. 언제나 함께해주셔서 고맙습니다.",
                "reward" to "money",
                "state" to "open",
                "title" to "추천인 보상"
            )
            letterDocRef.update(letterFieldKey, letterValue).await()

            userDao.update(
                id = "money",
                value = (state.userDataList.find { it.id == "money" }!!.value.toInt() + 5).toString()
            )

        } catch (e: Exception) {
            Log.e("recommendation", "처리 실패", e)
            postSideEffect(SettingSideEffect.Toast("처리 중 오류가 발생했습니다."))
        }

        loadData()
    }

    //입력 가능하게 하는 코드
    @OptIn(OrbitExperimental::class)
    fun onEditTextChange(editText: String) = blockingIntent {
        reduce {
            state.copy(editText = editText)
        }
    }

    private fun loadDonationList() = intent {

        Firebase.firestore
            .collection("code")
            .document("donation")
            .get()
            .addOnSuccessListener { snap ->

                if (!snap.exists()) return@addOnSuccessListener

                val list = mutableListOf<Donation>()

                snap.data?.forEach { (key, value) ->

                    val number = key.toIntOrNull() ?: return@forEach
                    val map = value as? Map<*, *> ?: return@forEach

                    val date = map["date"] as? String ?: ""
                    val message = map["message"] as? String ?: ""
                    val tag = map["tag"] as? String ?: ""
                    val name = map["name"] as? String ?: ""

                    list.add(
                        Donation(
                            number = number,
                            date = date,
                            message = message,
                            tag = tag,
                            name = name
                        )
                    )
                }

                val sorted = list.sortedByDescending { it.number }

                viewModelScope.launch {
                    intent {
                        reduce {
                            state.copy(donationList = sorted)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("DonationLoad", "후원 목록 로드 실패: ${e.message}")
            }
    }

    fun onReviewClick() = intent {

        //매달, medal, 칭호23
        val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: return@intent
        val (updated, acquired) = tryAcquireMedal(currentMedals, 23)
        if (acquired) {
            userDao.update(id = "etc", value3 = updated)

            val userDataList = userDao.getAllUserData()

            userDao.update(
                id = "money",
                value = (userDataList.find { it.id == "money" }?.value?.toInt()?.plus(10) ?: 10).toString()
            )

            val url = "https://play.google.com/store/apps/details?id=com.a0100019.mypat"
            postSideEffect(SettingSideEffect.OpenUrl(url))
        } else {
            postSideEffect(SettingSideEffect.Toast("이미 칭호를 획득하였습니다!"))
        }

        reduce {
            state.copy(settingSituation = "")
        }

        loadData()

    }

    fun onGoogleLoginChangeClick(
        googleIdToken: String
    ) = intent {

        try {
            val user = FirebaseAuth.getInstance().currentUser ?: return@intent
            if (!user.isAnonymous) return@intent

            val credential =
                GoogleAuthProvider.getCredential(googleIdToken, null)

            // 익명 계정 -> 구글 계정 연결 (UID 유지 핵심)
            user.linkWithCredential(credential).await()

            Log.e("login", "게스트 → 구글 로그인 전환 성공 (uid 유지): ${user.uid}")

            // Local DB auth 정보는 그대로 UID 유지
            userDao.update(id = "selectPat", value3 = "0")

            postSideEffect(
                SettingSideEffect.Toast("계정이 구글 계정으로 저장되었습니다 ☁️")
            )

            loadData()

        } catch (e: FirebaseAuthUserCollisionException) {
            // 이미 다른 계정에 연결된 구글 계정
            Log.e("login", "구글 계정 충돌", e)
            postSideEffect(
                SettingSideEffect.Toast("이미 사용 중인 구글 계정입니다.")
            )

        } catch (e: Exception) {
            Log.e("login", "구글 로그인 전환 실패", e)
            postSideEffect(
                SettingSideEffect.Toast("구글 로그인 실패: ${e.localizedMessage}")
            )
        }
    }

    fun loadNotice() = intent {
        // 1. 기존 공지 비우기 (로딩 느낌)
        reduce {
            state.copy(notice = "불러오는 중...")
        }

        try {
            // 2. 실제 서버 통신
            val fetchedNotice = noticeService.getNotice()

            // 3. 성공 시 State 업데이트
            reduce {
                state.copy(
                    notice = fetchedNotice,
                    settingSituation = "notice"
                )
            }
        } catch (e: Exception) {
            // 4. 에러 발생 시: e.localizedMessage나 e.message를 사용합니다.
            val errorMessage = e.localizedMessage ?: "알 수 없는 에러가 발생했습니다."

            reduce {
                state.copy(
                    notice = "공지사항 로드 실패"
                )
            }

            // 토스트 메시지에 실제 에러 원인을 포함시켜요!
            postSideEffect(
                SettingSideEffect.Toast("에러 발생")
            )

            // 로그캣에서도 자세히 볼 수 있게 로그를 남겨두면 더 좋아요.
            Log.e("NoticeError", errorMessage, e)
        }
    }

}


@Immutable
data class SettingState(
    val userDataList: List<User> = emptyList(),
    val isLoggingIn:Boolean = false,
    val patDataList: List<Pat> = emptyList(),
    val itemDataList: List<Item> = emptyList(),
    val worldDataList: List<World> = emptyList(),
    val walkDataList: List<Walk> = emptyList(),
    val englishDataList: List<English> = emptyList(),
    val koreanIdiomDataList: List<KoreanIdiom> = emptyList(),
    val diaryDataList: List<Diary> = emptyList(),
    val settingSituation: String = "",
    val imageUrl: String = "",
    val editText: String = "",
    val clickLetterData: Letter = Letter(),
    val letterDataList: List<Letter> = emptyList(),
    val sudokuDataList: List<Sudoku> = emptyList(),
    val areaDataList: List<Area> = emptyList(),
    val recommending: String = "-1",
    val recommended: String = "-1",
    val donationList: List<Donation> = emptyList(),
    val knowledgeDataList: List<Knowledge> = emptyList(),
    val notice: String = "서버에서 받아오는 중.."
    )

@Immutable
data class Donation(
    val number: Int = 0,
    val date: String = "2025-01-01",
    val message: String = "",
    val tag: String = "0",
    val name: String = "0",
)



sealed interface SettingSideEffect {
    class Toast(val message: String) : SettingSideEffect
    data object NavigateToLoginScreen : SettingSideEffect
    data class OpenUrl(val url: String) : SettingSideEffect

}
