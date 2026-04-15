package com.a0100019.mypat.presentation.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a0100019.mypat.data.room.allUser.AllUserDao
import com.a0100019.mypat.data.room.diary.Diary
import com.a0100019.mypat.data.room.diary.DiaryDao
import com.a0100019.mypat.data.room.english.EnglishDao
import com.a0100019.mypat.data.room.item.ItemDao
import com.a0100019.mypat.data.room.koreanIdiom.KoreanIdiomDao
import com.a0100019.mypat.data.room.letter.Letter
import com.a0100019.mypat.data.room.letter.LetterDao
import com.a0100019.mypat.data.room.area.AreaDao
import com.a0100019.mypat.data.room.knowledge.KnowledgeDao
import com.a0100019.mypat.data.room.knowledge.getKnowledgeInitialData
import com.a0100019.mypat.data.room.pat.PatDao
import com.a0100019.mypat.data.room.photo.Photo
import com.a0100019.mypat.data.room.photo.PhotoDao
import com.a0100019.mypat.data.room.sudoku.SudokuDao
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.data.room.walk.Walk
import com.a0100019.mypat.data.room.walk.WalkDao
import com.a0100019.mypat.data.room.world.World
import com.a0100019.mypat.data.room.world.WorldDao
import com.a0100019.mypat.presentation.main.management.ManagementSideEffect
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
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
    private val allUserDao: AllUserDao,
    private val knowledgeDao: KnowledgeDao,
    private val photoDao: PhotoDao,
    @ApplicationContext private val context: Context
) : ViewModel(), ContainerHost<LoginState, LoginSideEffect> {

    override val container: Container<LoginState, LoginSideEffect> = container(
        initialState = LoginState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(LoginSideEffect.Toast(message = throwable.message.orEmpty()))
                }
            }
        }
    )

    // 뷰 모델 초기화 시 모든 user 데이터를 로드
    init {
        loadData()
    }

    //room에서 데이터 가져옴
    fun loadData() = intent {
        val userDataList = userDao.getAllUserData()
        val loginState = userDataList.find { it.id == "auth" }?.value ?: "loading"

        if (knowledgeDao.count() == 0) {
            knowledgeDao.insertAll(getKnowledgeInitialData())
        }

        when (loginState) {
            "0" -> {
                reduce {
                    state.copy(
                        loginState = "unLogin"
                    )
                }
            }
            "loading" -> {
                reduce {
                    state.copy(
                        loginState = "loading"
                    )
                }
            }
            else -> {
                reduce {
                    state.copy(
                        loginState = "login"
                    )
                }
                todayAttendance()
            }
        }

    }

    fun reLoading() = intent {
        loadData()
    }

    fun onGuestLoginClick() = intent {
        // 1. 중복 로그인 방지
        if (state.isLoggingIn) return@intent

        reduce {
            state.copy(
                isLoggingIn = true,
                loginState = "loginLoading" // 🔹 로딩 시작
            )
        }

        try {
            // 2. Firebase 익명 로그인 실행
            val authResult = FirebaseAuth.getInstance()
                .signInAnonymously()
                .await()

            val user = authResult.user
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

            Log.e("login", "Guest User = $user, isNewUser = $isNewUser")

            user?.let {
                if (isNewUser) {
                    val db = FirebaseFirestore.getInstance()

                    //  Tag 계산
                    val lastKey: Int = withContext(Dispatchers.IO) {
                        val snapshot = db.collection("tag")
                            .document("tag")
                            .get()
                            .await()
                        val dataMap = snapshot.data ?: emptyMap()
                        dataMap.keys.maxOfOrNull { it.toInt() } ?: 0
                    }

                    val nextKey = (lastKey + 1).toString()

                    //  Local DB
                    userDao.update(id = "auth", value = it.uid, value2 = nextKey)

                    //  Firestore Tag
                    db.collection("tag")
                        .document("tag")
                        .update(nextKey, it.uid)
                        .await()

                    //  초기 데이터
                    val currentDate =
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                    userDao.update(id = "date", value3 = currentDate)
                    userDao.update(id = "selectPat", value3 = "1")
//                    letterDao.updateDateByTitle("시작의 편지", currentDate)

                    //  Firestore User
                    db.collection("users")
                        .document(it.uid)
                        .set(
                            mapOf(
                                "online" to "1",
                                "community" to mapOf("like" to "0"),
                                "name" to "게스트",
                                "tag" to nextKey
                            ),
                            SetOptions.merge()
                        )
                        .await()

                    Log.e("login", "익명 신규 사용자 등록 완료")
                } else {
                    Log.e("login", "기존 익명 사용자 세션 재사용")
                }

                //  성공 시
                reduce {
                    state.copy(
                        dialog = "explanation",
                        loginState = "loginSuccess"
                    )
                }
            }

        } catch (e: Exception) {
            Log.e("login", "익명 로그인 실패", e)

            //  실패 시 unLogin 복귀
            reduce {
                state.copy(loginState = "unLogin")
            }

            postSideEffect(
                LoginSideEffect.Toast("게스트 로그인 실패: ${e.localizedMessage}")
            )
        } finally {
            reduce {
                state.copy(isLoggingIn = false)
            }
        }
    }


    fun onGoogleLoginClick(idToken: String) = intent {
        Log.e("login", "idToken = $idToken")

        if (state.isLoggingIn) return@intent

        //  로그인 시작 상태
        reduce {
            state.copy(
                isLoggingIn = true,
                loginState = "loginLoading"
            )
        }

        try {

            //auth에 계정 생성
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            val user = authResult.user
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

            Log.e("login", "user = $user, isNewUser = $isNewUser")

            user?.let {
                if (isNewUser) {

                    //  신규 사용자일 때만 실행되는 코드
                    val db = FirebaseFirestore.getInstance()

                    //tag 설정
                    val lastKey: Int = withContext(Dispatchers.IO) {
                        val documentSnapshot = db.collection("tag")
                            .document("tag")
                            .get()
                            .await()

                        val dataMap = documentSnapshot.data ?: emptyMap()

                        dataMap.keys.maxOfOrNull { it.toInt() }!!
                    }
                    userDao.update(id = "auth", value = user.uid, value2 = "${lastKey+1}")

                    val firestore = Firebase.firestore
                    val tagDocRef = firestore.collection("tag").document("tag")
                    tagDocRef.get().addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val data = document.data.orEmpty()

                            // 키가 숫자인 필드들 중 가장 큰 숫자 찾기
                            val maxKey = data.keys.mapNotNull { it.toIntOrNull() }.maxOrNull() ?: -1
                            val nextKey = (maxKey + 1).toString()

                            // 새로운 필드 추가
                            val newField = mapOf(nextKey to user.uid)

                            // 문서 업데이트
                            tagDocRef.update(newField)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Field 추가 성공: $nextKey -> hello")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Field 추가 실패", e)
                                }
                        } else {
                            Log.e("Firestore", "문서가 존재하지 않음")
                        }
                    }.addOnFailureListener { e ->
                        Log.e("Firestore", "문서 읽기 실패", e)
                    }

                    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    userDao.update(id = "date", value3 = currentDate)

//                    letterDao.updateDateByTitle(title = "시작의 편지", todayDate = currentDate)

                    val userRef = db.collection("users").document(it.uid)
                    userRef.set(
                        mapOf(
                            "online" to "1",
                            "community" to mapOf(
                                "like" to "0"
                            )
                        ),
                        SetOptions.merge()
                    )
                        .addOnSuccessListener {
                            Log.d("login", "online=1, community.like=0 저장 성공")
                        }
                        .addOnFailureListener { e ->
                            Log.e("login", "저장 실패", e)
                        }

                    Log.e("login", "신규 사용자입니다")
//                    postSideEffect(LoginSideEffect.Toast("환영합니다!"))

                    reduce {
                        state.copy(
                            dialog = "explanation",
                            loginState = "login"
                        )
                    }

                } else {

                    //  기존 사용자일 경우 처리
                    Log.e("login", "기존 사용자입니다")

                    // Firestore에서 유저 데이터 가져오기
                    val db = FirebaseFirestore.getInstance()
                    try {
                        val userDoc = db.collection("users").document(it.uid).get().await()
                        if (userDoc.exists()) {

                            //  online 필드 확인
                            val online = userDoc.getString("online")
                            if (online == "1") {
                                if(state.dialog != "check"){
                                    Log.w("login", "이미 로그인 중인 사용자입니다")
                                    reduce {
                                        state.copy(
                                            dialog = "loginWarning",
                                            loginState = "unLogin"
                                        )
                                    }
                                    return@intent // 또는 return (코루틴/함수 구조에 따라)
                                }
                            } else {
                                //  online 필드가 0이면 1로 업데이트
                                db.collection("users").document(it.uid)
                                    .update("online", "1")
                                    .addOnSuccessListener {
                                        Log.d("login", "online 필드가 1로 업데이트됨")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("login", "online 필드 업데이트 실패", e)
                                    }
                            }

                            val money = userDoc.getString("money")
                            val cash = userDoc.getString("cash")
                            userDao.update(id = "money", value = money, value2 = cash)

                            val saveStepsRaw = userDoc.getString("stepsRaw")

                            // SharedPreferences 불러오기
                            val prefs = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("stepsRaw", saveStepsRaw)
                                .apply()

                            userDao.update(id = "etc2", value2 = saveStepsRaw)

                            val pay = userDoc.getString("pay")
                            userDao.update(id = "name", value3 = pay)

                            val communityMap = userDoc.get("community") as Map<String, String>
                            val ban = communityMap["ban"]
                            val like = communityMap["like"]
                            val warning = communityMap["warning"]
                            userDao.update(id = "community", value = like, value2 = warning, value3 = ban)

                            val medal = communityMap["medal"]
                            val introduction = communityMap["introduction"]
                            if (medal != null && introduction != null) {
                                userDao.update(id = "etc", value = introduction, value3 = medal)
                            }
                            val medalQuest = communityMap["medalQuest"]
                            if (medalQuest != null) {
                                userDao.update(id = "name", value2 = medalQuest)
                            }

                            val dateMap = userDoc.get("date") as Map<String, String>
                            val firstDate = dateMap["firstDate"]
                            val totalDate = dateMap["totalDate"]
                            val lastDate = dateMap["lastDate"]
                            userDao.update(id = "date", value = lastDate, value2 = totalDate, value3 = firstDate)

                            val gameMap = userDoc.get("game") as Map<String, String>
                            val firstGame = gameMap["firstGame"]
                            val secondGame = gameMap["secondGame"]
                            val thirdGameEasy = gameMap["thirdGameEasy"]
                            val thirdGameNormal = gameMap["thirdGameNormal"]
                            val thirdGameHard = gameMap["thirdGameHard"]
                            userDao.update(id = "firstGame", value = firstGame)
                            userDao.update(id = "secondGame", value = secondGame)
                            userDao.update(id = "thirdGame", value = thirdGameEasy, value2 = thirdGameNormal, value3 = thirdGameHard)

                            val itemMap = userDoc.get("item") as Map<String, String>
                            val openItemSpace = itemMap["openItemSpace"]
                            val useItem = itemMap["useItem"]
                            userDao.update(id = "item", value2 = openItemSpace, value3 = useItem)

                            val patMap = userDoc.get("pat") as Map<String, String>
                            val openPatSpace = patMap["openPatSpace"]
                            val usePat = patMap["usePat"]
                            userDao.update(id = "pat", value2 = openPatSpace, value3 = usePat)

                            val name = userDoc.getString("name")
                            userDao.update(id = "name", value = name)
                            val tag = userDoc.getString("tag")
                            userDao.update(id = "auth", value = it.uid, value2 = tag)

                            val walkMap = userDoc.get("walk") as Map<String, String>
                            val saveWalk = walkMap["saveWalk"]
                            val totalWalk = walkMap["totalWalk"]
                            userDao.update(id = "walk", value = saveWalk, value3 = totalWalk)

                            //오류 안나게 월드 데이터 한번 지움
                            worldDao.deleteAllWorlds()

                            val area = userDoc.getString("area")
                            worldDao.insert(World(id = 1, value = area.toString(), type = "area"))

                            val worldMap = userDoc.get("world") as Map<String, Map<String, String>>
                            for ((index, innerMap) in worldMap) {
                                val id = innerMap["id"]
//                                val size = innerMap["size"]
                                val type = innerMap["type"]
//                                val x = innerMap["x"]
//                                val y = innerMap["y"]

                                worldDao.insert(World(id = index.toInt()+2, value = id.toString(), type = type.toString()))
//                                Log.d("Firestore", "[$key] color=$color, font=$font")
                            }

                            //daily 서브컬렉션
                            val dailySubCollectionSnapshot = db
                                .collection("users")
                                .document(it.uid)
                                .collection("daily")
                                .get()
                                .await()

                            val dailyDocs = dailySubCollectionSnapshot.documents
                                .sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }

                            for (dailyDoc in dailyDocs) {
                                // 숫자 순서대로 처리됨
                                val date = dailyDoc.getString("date") ?: continue

                                val diaryMap = dailyDoc.get("diary") as? Map<*, *>
                                val diaryContents = diaryMap?.get("contents") as? String ?: ""
                                val diaryEmotion = diaryMap?.get("emotion") as? String ?: ""
                                val diaryState = diaryMap?.get("state") as? String ?: ""
                                diaryDao.insert(
                                    Diary(
                                        id = dailyDoc.id.toInt(),
                                        date = date,
                                        emotion = diaryEmotion,
                                        state = diaryState,
                                        contents = diaryContents
                                    )
                                )

// dailyDocs를 처리하는 부모 코루틴(intent 블록 등) 내부라고 가정
// dailyDocs 반복문 내부
                                val photoMap = dailyDoc.get("photo") as? Map<*, *>
                                if (photoMap != null) {
                                    //  키(1, 2, 3...)를 숫자로 바꿔서 오름차순 정렬 후 순서대로 처리
                                    val sortedPhotos = photoMap.toList().sortedByDescending { (key, _) ->
                                        key.toString().toIntOrNull() ?: Int.MAX_VALUE
                                    }

                                    sortedPhotos.forEach { (key, value) ->
                                        val photoData = value as? Map<*, *>
                                        val firebaseUrl = photoData?.get("firebaseUrl") as? String

                                        if (firebaseUrl != null) {
                                            // launch를 지우고 순서대로(suspend) 실행하면 DB에도 순서대로 쌓입니다.
                                            val newLocalPath = downloadImageToLocal(context, firebaseUrl)
                                            photoDao.insert(
                                                Photo(
                                                    date = date,
                                                    firebaseUrl = firebaseUrl,
                                                    localPath = newLocalPath.toString(),
                                                    isSynced = true
                                                )
                                            )

                                            // 변수 증가 대신 reduce 내부에서 최신 state 값을 활용
                                            reduce {
                                                state.copy(
                                                    downloadPhotoCount = state.downloadPhotoCount + 1
                                                )
                                            }
                                            Log.d("LoginViewModel", "다운로드 완료: ${state.downloadPhotoCount + 1}개")
                                        }
                                    }
                                }

                                //  state 필드가 존재할 경우에만 처리
                                val stateMap = dailyDoc.get("state") as? Map<*, *>
                                val englishState = stateMap?.get("english") as? String
                                val koreanIdiomState = stateMap?.get("koreanIdiom") as? String

                                if (englishState != null) {
                                    englishDao.updateDateAndState(
                                        id = dailyDoc.id.toInt(),
                                        date = date,
                                        state = englishState
                                    )
                                }

                                if (koreanIdiomState != null) {
                                    koreanIdiomDao.updateDateAndState(
                                        id = dailyDoc.id.toInt(),
                                        date = date,
                                        state = koreanIdiomState
                                    )
                                }

                                val walk = dailyDoc.getString("walk")

                                if (!walk.isNullOrBlank()) {
                                    walkDao.insert(
                                        Walk(
                                            id = dailyDoc.id.toInt(),
                                            date = date,
                                            success = walk
                                        )
                                    )
                                }

                                dailyDoc.getString("knowledge")?.let { knowledge ->
                                    knowledgeDao.updateFirstZeroDateKnowledge(
                                        date = date,
                                        state = knowledge
                                    )
                                }

                            }

                            // 'items' 문서 안의 Map 필드들을 가져오기
                            val itemsSnapshot = db
                                .collection("users")
                                .document(it.uid)
                                .collection("dataCollection")
                                .document("items")
                                .get()
                                .await()

                            val itemsMap = itemsSnapshot.data ?: emptyMap()

                            for ((itemId, itemData) in itemsMap) {
                                if (itemData is Map<*, *>) {
                                    val date = itemData["date"] as? String ?: continue
                                    val size = (itemData["size"] as? String)?.toFloatOrNull() ?: continue
                                    val x = (itemData["x"] as? String)?.toFloatOrNull() ?: continue
                                    val y = (itemData["y"] as? String)?.toFloatOrNull() ?: continue

                                    itemDao.updateItemData(
                                        id = itemId.toInt(),
                                        date = date,
                                        x = x,
                                        y = y,
                                        size = size
                                    )
                                }
                            }

                            val areasSnapshot = db
                                .collection("users")
                                .document(it.uid)
                                .collection("dataCollection")
                                .document("areas")
                                .get()
                                .await()

                            val areasMap = areasSnapshot.data ?: emptyMap()

                            for ((areaId, areaData) in areasMap) {
                                if (areaData is Map<*, *>) {
                                    val date = areaData["date"] as? String ?: continue

                                    areaDao.updateAreaData(
                                        id = areaId.toInt(),
                                        date = date,
                                    )
                                }
                            }

                            val patsSnapshot = db
                                .collection("users")
                                .document(it.uid)
                                .collection("dataCollection")
                                .document("pats")
                                .get()
                                .await()

                            val patsMap = patsSnapshot.data ?: emptyMap()

                            for ((patId, patData) in patsMap) {
                                if (patData is Map<*, *>) {
                                    val date = patData["date"] as? String ?: continue
                                    val love = patData["love"] as? String ?: continue
                                    val size = (patData["size"] as? String)?.toFloatOrNull() ?: continue
                                    val x = (patData["x"] as? String)?.toFloatOrNull() ?: continue
                                    val y = (patData["y"] as? String)?.toFloatOrNull() ?: continue
                                    val gameCount = (patData["gameCount"] as? String)?.toIntOrNull() ?: continue
                                    val effect = (patData["effect"] as? String)?.toIntOrNull() ?: continue

                                    patDao.updatePatData(
                                        id = patId.toIntOrNull() ?: continue,
                                        date = date,
                                        love = love.toInt(),
                                        x = x,
                                        y = y,
                                        size = size,
                                        gameCount = gameCount,
                                        effect = effect
                                    )
                                }
                            }

                            //sudoku 서브컬렉션
                            val sudokuDoc = db
                                .collection("users")
                                .document(it.uid)
                                .collection("dataCollection")
                                .document("sudoku")
                                .get()
                                .await()

                            if (sudokuDoc.exists()) {
                                val level = sudokuDoc.getString("level")
                                val state = sudokuDoc.getString("state")
                                val sudokuBoard = sudokuDoc.getString("sudokuBoard")
                                val sudokuFirstBoard = sudokuDoc.getString("sudokuFirstBoard")
                                val sudokuMemoBoard = sudokuDoc.getString("sudokuMemoBoard")
                                val time = sudokuDoc.getString("time")
                                sudokuDao.update(id = "sudokuBoard", value = sudokuBoard)
                                sudokuDao.update(id = "sudokuFirstBoard", value = sudokuFirstBoard)
                                sudokuDao.update(id = "sudokuMemoBoard", value = sudokuMemoBoard)
                                sudokuDao.update(id = "time", value = time)
                                sudokuDao.update(id = "level", value = level)
                                sudokuDao.update(id = "state", value = state)
                            }

                            //letter 서브컬렉션
                            val lettersSnapshot = db
                                .collection("users")
                                .document(it.uid)
                                .collection("dataCollection")
                                .document("letters")
                                .get()
                                .await()

                            val lettersMap = lettersSnapshot.data ?: emptyMap()

                            for ((letterId, letterData) in lettersMap) {
                                //수정
                                if (letterData is Map<*, *>) {
                                    val date = letterData["date"] as? String ?: continue
                                    val title = letterData["title"] as? String ?: continue
                                    val message = letterData["message"] as? String ?: continue
                                    val link = letterData["link"] as? String ?: continue
                                    val reward = letterData["reward"] as? String ?: continue
                                    val amount = letterData["amount"] as? String ?: continue
                                    val state = letterData["state"] as? String ?: continue

                                    letterDao.insert(
                                        Letter(
                                            id = letterId.toInt(),
                                            date = date,
                                            title = title,
                                            message = message,
                                            link = link,
                                            reward = reward,
                                            amount = amount,
                                            state = state,
                                        )
                                    )
                                }
                            }

                        } else {
                            Log.w("login", "Firestore에 유저 문서가 없습니다")
                            postSideEffect(LoginSideEffect.Toast("유저 정보를 찾을 수 없습니다"))

                            reduce {
                                state.copy(
                                    dialog = "",
                                    loginState = "unLogin"
                                )
                            }

                            return@intent
                        }
                    } catch (e: Exception) {
                        Log.e("login", "Firestore에서 유저 문서 가져오기 실패", e)
                        postSideEffect(LoginSideEffect.Toast("유저 정보 로딩 실패"))

                        reduce {
                            state.copy(
                                dialog = "",
                                loginState = "unLogin"
                            )
                        }

                        return@intent
                    }

                    reduce {
                        state.copy(
                            dialog = "explanation",
                            loginState = "login"
                        )
                    }

                }

            }

        } catch (e: Exception) {
            Log.e("login", "뷰모델 로그인 실패", e)
            postSideEffect(LoginSideEffect.Toast("로그인 실패: ${e.localizedMessage}"))
            reduce {
                state.copy(
                    loginState = "unLogin"
                )
            }
        } finally {
            reduce { state.copy(isLoggingIn = false) }
        }
    }

    private suspend fun downloadImageToLocal(context: Context, firebaseUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. UUID를 사용하여 고유한 파일명 생성
                val uniqueId = UUID.randomUUID().toString()
                val fileName = "haru_photo_${uniqueId}.jpg"
                val localFile = File(context.filesDir, fileName)

                val storageRef = Firebase.storage.getReferenceFromUrl(firebaseUrl)
                storageRef.getFile(localFile).await()

                // 2. 복호화 로직 (기존과 동일)
                val scrambledBytes = localFile.readBytes()
                val originalBytes = togglePrivacy(scrambledBytes)
                localFile.writeBytes(originalBytes)

                localFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun togglePrivacy(data: ByteArray): ByteArray {
        val key = 0xAF.toByte()
        return ByteArray(data.size) { i -> (data[i].toInt() xor key.toInt()).toByte() }
    }


    fun onNavigateToMainScreen() = intent {
        postSideEffect(LoginSideEffect.NavigateToMainScreen)
    }

    fun onNavigateToDiaryScreen() = intent {
        postSideEffect(LoginSideEffect.NavigateToDiaryScreen)
    }

    fun onNavigateToFirstScreen() = intent {
        postSideEffect(LoginSideEffect.NavigateToFirstScreen)
    }

    fun dialogChange(string: String) = intent {
        reduce {
            state.copy(
                dialog = string
            )
        }
    }

    fun todayAttendance() = intent {
        val lastData = userDao.getValueById("date")
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        if (lastData != currentDate) {

            val allEnglishDataTest = englishDao.getAllEnglishData()
            var lastDate = allEnglishDataTest
                .filter { it.date != "0" }
                .maxByOrNull { it.id }
                ?.date ?: "0"

            val totalDate = userDao.getValue2ById("date")
            if(totalDate >= "100") {
                lastDate = walkDao.getLatestWalkData().date
            }

            if(lastDate != currentDate){
                val userData = userDao.getAllUserData()

                userDao.update(id = "date", value = currentDate)
                userDao.update(
                    id = "date",
                    value2 = (userData.find { it.id == "date" }!!.value2.toInt() + 1).toString()
                )

                //출석 일수 확인해서 편지 전송
                when (userData.find { it.id == "date" }!!.value2.toInt() + 1) {
                    3 -> letterDao.updateDateByTitle(title = "3일 출석 감사 편지", todayDate = currentDate)
                    30 -> letterDao.updateDateByTitle(
                        title = "30일 출석 감사 편지",
                        todayDate = currentDate
                    )

                    //매달, medal, 칭호22
                    50 -> {
                        //매달, medal, 칭호22
                        val myMedal = userDao.getAllUserData().find { it.id == "etc" }!!.value3

                        val myMedalList: MutableList<Int> =
                            myMedal
                                .split("/")
                                .mapNotNull { it.toIntOrNull() }
                                .toMutableList()

                        //  여기 숫자 두개 바꾸면 됨
                        if (!myMedalList.contains(22)) {
                            myMedalList.add(22)

                            // 다시 문자열로 합치기
                            val updatedMedal = myMedalList.joinToString("/")

                            // DB 업데이트
                            userDao.update(
                                id = "etc",
                                value3 = updatedMedal
                            )

                            postSideEffect(LoginSideEffect.Toast("칭호를 획득했습니다!"))
                        }

                    }

                    100 -> letterDao.updateDateByTitle(
                        title = "100일 출석 감사 편지",
                        todayDate = currentDate
                    )
                }

                val closeKoreanIdiomData = koreanIdiomDao.getCloseKoreanIdiom()
                if (closeKoreanIdiomData != null) {
                    closeKoreanIdiomData.date = currentDate
                    closeKoreanIdiomData.state = "대기"
                    koreanIdiomDao.update(closeKoreanIdiomData)
                }

                val closeEnglishData = englishDao.getCloseEnglish()
                if (closeEnglishData != null) {
                    closeEnglishData.date = currentDate
                    closeEnglishData.state = "대기"
                    englishDao.update(closeEnglishData)
                }

                val allDiaries = diaryDao.getAllDiaryData()

                // id < 10000 인 것들 중에서만 최대값 찾기
                val maxUnder10000 = allDiaries
                    .filter { it.id < 10000 }
                    .maxOfOrNull { it.id } ?: 0

                val newId = maxUnder10000 + 1   // 아무것도 없으면 1부터 시작

                diaryDao.insert(
                    Diary(
                        id = newId,
                        date = currentDate,            // "2025-11-12" 같은 형식
                        // emotion, state, contents 는 디폴트 쓰면 생략 가능
                    )
                )

                walkDao.insert(Walk(date = currentDate))
            }
        }

        if(!knowledgeDao.existsByDate(currentDate)){
            val closeKnowledgeData = knowledgeDao.getCloseKnowledge()
            if (closeKnowledgeData != null) {
                closeKnowledgeData.date = currentDate
                closeKnowledgeData.state = "대기"
                knowledgeDao.update(closeKnowledgeData)
            }
        }

        //칭호, 편지 관리
        if(itemDao.getAllCloseItemData().isEmpty()) {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            letterDao.updateDateByTitle(title = "모든 아이템 획득 축하 편지", todayDate = today)
        }

        //매달, medal, 칭호1
        val myMedal = userDao.getAllUserData().find { it.id == "etc" }!!.value3

        val myMedalList: MutableList<Int> =
            myMedal
                .split("/")
                .mapNotNull { it.toIntOrNull() }
                .toMutableList()

        //  여기 숫자 두개 바꾸면 됨
        if (!myMedalList.contains(1)) {
            myMedalList.add(1)

            // 다시 문자열로 합치기
            val updatedMedal = myMedalList.joinToString("/")

            // DB 업데이트
            userDao.update(
                id = "etc",
                value3 = updatedMedal
            )

            postSideEffect(LoginSideEffect.Toast("칭호를 획득했습니다!"))
        }

        Log.e("ManagementViewModel", "데이터 로드 완료")

        postSideEffect(LoginSideEffect.NavigateToFirstScreen)

    }


}

@Immutable
data class LoginState(
    val userData: List<User> = emptyList(),
    val isLoggingIn:Boolean = false,
    val loginState: String = "",
    val dialog: String = "loading",
    val downloadPhotoCount: Int = 0
)

//상태와 관련없는 것
sealed interface LoginSideEffect{
    class Toast(val message:String): LoginSideEffect
    data object NavigateToMainScreen: LoginSideEffect
    data object NavigateToDiaryScreen: LoginSideEffect
    data object NavigateToFirstScreen: LoginSideEffect


}