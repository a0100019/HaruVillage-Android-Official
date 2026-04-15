package com.a0100019.mypat.presentation.main.operator

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a0100019.mypat.data.room.allUser.AllUser
import com.a0100019.mypat.data.room.allUser.AllUserDao
import com.a0100019.mypat.data.room.area.AreaDao
import com.a0100019.mypat.data.room.item.Item
import com.a0100019.mypat.data.room.item.ItemDao
import com.a0100019.mypat.data.room.pat.Pat
import com.a0100019.mypat.data.room.pat.PatDao
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.data.room.world.WorldDao
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class OperatorViewModel @Inject constructor(
    private val userDao: UserDao,
    private val worldDao: WorldDao,
    private val patDao: PatDao,
    private val itemDao: ItemDao,
    private val allUserDao: AllUserDao,
    private val areaDao: AreaDao
) : ViewModel(), ContainerHost<OperatorState, OperatorSideEffect> {

    override val container: Container<OperatorState, OperatorSideEffect> = container(
        initialState = OperatorState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(OperatorSideEffect.Toast(message = throwable.message.orEmpty()))
                }
            }
        }
    )

    // 뷰 모델 초기화 시 모든 user 데이터를 로드
    init {
        loadData()
    }

    //room에서 데이터 가져옴
    private fun loadData() = intent {
        val userDataList = userDao.getAllUserData()
        val patDataList = patDao.getAllPatData()
        val itemDataList = itemDao.getAllItemDataWithShadow()
        var allUserDataList = allUserDao.getAllUserDataNoBan()
        allUserDataList = allUserDataList.filter { it.totalDate != "1" && it.totalDate != "0" }

        val allAreaCount = areaDao.getAllAreaData().size.toString()

        reduce {
            state.copy(
                userDataList = userDataList,
                patDataList = patDataList,
                itemDataList = itemDataList,
                allUserDataList =  allUserDataList,
                allAreaCount = allAreaCount
            )
        }
    }

    fun onCloseClick() = intent {
        reduce {
            state.copy(
                dialogState = "",
                text1 = "",
                text2 = "",
                text3 = "",
                text4 = "",
                text5 = "",
            )
        }
    }

    fun onDialogChangeClick(dialog: String) = intent {

        if(dialog == "askView") {
            loadAskMessages()
        }

        reduce {
            state.copy(
                dialogState = dialog
            )
        }

    }

    fun onNoticeChatWrite() = intent {
        val currentMessage = state.text1.trim()
        val userName = state.userDataList.find { it.id == "name" }!!.value // 또는 상태에서 유저 이름을 가져올 수 있다면 사용
        val userId = state.userDataList.find { it.id == "auth" }!!.value
        val userTag = state.userDataList.find { it.id == "auth" }!!.value2
        val userBan = state.userDataList.find { it.id == "community" }!!.value3

        if (currentMessage.isEmpty()) return@intent

        val timestamp = System.currentTimeMillis()
        val todayDocId = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        val chatData = mapOf(
            "message" to currentMessage,
            "name" to "공지사항",
            "ban" to userBan,
            "tag" to "2",
            "uid" to currentMessage
        )

        Firebase.firestore.collection("chat")
            .document(todayDocId)
            .set(mapOf(timestamp.toString() to chatData), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("ChatSubmit", "채팅 전송 성공 (merge)")
            }
            .addOnFailureListener { e ->
                Log.e("ChatSubmit", "채팅 전송 실패: ${e.message}")
            }

        // 입력 필드 초기화
        reduce {
            state.copy(
                text1 = "",
                dialogState = ""
            )
        }
    }

    fun onAskClick(message: String) = intent {
        reduce {
            state.copy(
                text1 = message,
                dialogState = "askWrite"
            )
        }
    }

    fun onAllUserDataGet() = intent {
        val db = Firebase.firestore

        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                // 성공 시 결과 처리
                applyResult(snapshot.documents)
            }
            .addOnFailureListener { e ->
                Log.e("DB", "모든 유저 조회 실패", e)
                // 실패 시 토스트 알림
                intent {
                    postSideEffect(OperatorSideEffect.Toast("유저 데이터 로드 실패: ${e.message}"))
                }
            }
    }

    private fun applyResult(docs: List<DocumentSnapshot>) = intent {
        // 1️⃣ map을 사용하여 모든 문서를 AllUser 객체로 변환
        val users = docs.map { doc ->
            val gameMap = doc.get("game") as? Map<String, String> ?: emptyMap()
            val communityMap = doc.get("community") as? Map<String, String> ?: emptyMap()
            val dateMap = doc.get("date") as? Map<String, String> ?: emptyMap()
            val itemMap = doc.get("item") as? Map<String, String> ?: emptyMap()
            val patMap = doc.get("pat") as? Map<String, String> ?: emptyMap()
            val worldMap = doc.get("world") as? Map<String, Map<String, String>> ?: emptyMap()

            val worldData = worldMap.entries.joinToString("/") { (_, inner) ->
                "${inner["id"].orEmpty()}@" +
                        "${inner["size"].orEmpty()}@" +
                        "${inner["type"].orEmpty()}@" +
                        "${inner["x"].orEmpty()}@" +
                        "${inner["y"].orEmpty()}@" +
                        "${inner["effect"].orEmpty()}"
            }

            AllUser(
                tag = doc.getString("tag").orEmpty(),
                lastLogin = doc.getString("lastLogin")?.toLongOrNull() ?: 0L,
                ban = communityMap["ban"].orEmpty(),
                like = communityMap["like"].orEmpty(),
                warning = communityMap["introduction"].orEmpty() +
                        "@" + communityMap["medal"].orEmpty(),
                firstDate = dateMap["firstDate"].orEmpty(),
                firstGame = gameMap["firstGame"].orEmpty(),
                secondGame = gameMap["secondGame"].orEmpty(),
                thirdGameEasy = gameMap["thirdGameEasy"].orEmpty(),
                thirdGameNormal = gameMap["thirdGameNormal"].orEmpty(),
                thirdGameHard = gameMap["thirdGameHard"].orEmpty(),
                openItem = itemMap["openItem"].orEmpty(),
                area = doc.getString("area").orEmpty(),
                name = doc.getString("name").orEmpty(),
                openPat = patMap["openPat"].orEmpty(),
                openArea = doc.getString("openArea").orEmpty(),
                totalDate = dateMap["totalDate"].orEmpty(),
                worldData = worldData
            )
        }

        // 2️⃣ 로컬 DB 저장
        allUserDao.insertAll(users)

        // 3️⃣ 저장 완료 후 토스트 알림
        postSideEffect(OperatorSideEffect.Toast("${users.size}명의 유저 정보를 모두 가져왔습니다."))
    }

    fun onAskChatWrite() = intent {
        val currentMessage = state.text1.trim()
        val userName = state.userDataList.find { it.id == "name" }!!.value // 또는 상태에서 유저 이름을 가져올 수 있다면 사용
        val userId = state.userDataList.find { it.id == "auth" }!!.value
        val userTag = state.userDataList.find { it.id == "auth" }!!.value2
        val userBan = state.userDataList.find { it.id == "community" }!!.value3

        if (currentMessage.isEmpty()) return@intent

        val timestamp = System.currentTimeMillis()
        val todayDocId = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        val chatData = mapOf(
            "message" to "[해당 내용은 최신 버전에서 확인할 수 있습니다.]",
            "name" to "도란도란",
            "ban" to userBan,
            "tag" to "3",
            "uid" to currentMessage
        )

        Firebase.firestore.collection("chat")
            .document(todayDocId)
            .set(mapOf(timestamp.toString() to chatData), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("ChatSubmit", "채팅 전송 성공 (merge)")
            }
            .addOnFailureListener { e ->
                Log.e("ChatSubmit", "채팅 전송 실패: ${e.message}")
            }

        // 입력 필드 초기화
        reduce {
            state.copy(
                text1 = "",
                dialogState = ""
            )
        }
    }

    fun onUserRankClick(userTag: Int) = intent {
        if(userTag == 0){
            reduce {
                state.copy(
                    clickAllUserData = AllUser(),
                    clickAllUserWorldDataList = emptyList()
                )
            }
        } else {
            val selectedUser = state.allUserDataList.find { it.tag == userTag.toString() }
            val selectedUserWorldDataList: List<String> = selectedUser!!.worldData
                .split("/")
                .filter { it.isNotBlank() } // 혹시 모를 빈 문자열 제거

            reduce {
                state.copy(
                    clickAllUserData = selectedUser,
                    clickAllUserWorldDataList = selectedUserWorldDataList
                )
            }
        }
    }

    private fun loadAskMessages() {
        Firebase.firestore.collection("ask")
            .addSnapshotListener { snapshot, error ->
                Log.d("CommunityViewModel", "전체 채팅 스냅샷 수신됨")

                if (error != null) {
                    Log.e("CommunityViewModel", "채팅 데이터 에러: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val allMessages = mutableListOf<OperatorMessage>()

                    for (doc in snapshot.documents) {

                        val data = doc.data ?: continue
                        val messages = data.mapNotNull { (key, value) ->
                            val timestamp = key.toLongOrNull() ?: return@mapNotNull null
                            val map = value as? Map<*, *> ?: return@mapNotNull null
                            val message = map["message"] as? String
                            val name = map["name"] as? String
                            val tag = map["tag"] as? String
                            val ban = map["ban"] as? String
                            val uid = map["uid"] as? String

                            if (message != null && name != null && tag != null && ban == "0" && uid != null) {
                                OperatorMessage(timestamp, message, name, tag, ban, uid)
                            } else null
                        }
                        allMessages.addAll(messages)
                    }

                    val sorted = allMessages.sortedBy { it.timestamp }

                    viewModelScope.launch {
                        intent {
                            reduce {
                                state.copy(askMessages = sorted)
                            }
                        }
                    }
                } else {
                    Log.w("CommunityViewModel", "chat 컬렉션에 문서가 없음")
                }
            }
    }

    fun onSituationChange(newSituation: String) = intent {
        reduce {

            state.copy(
                situation = newSituation,
            )
        }
    }

    fun onOperatorChatSubmitClick() = intent {
        val currentMessage = state.text1.trim()
        val userName = state.userDataList.find { it.id == "name" }!!.value // 또는 상태에서 유저 이름을 가져올 수 있다면 사용
        val userId = state.userDataList.find { it.id == "auth" }!!.value
        val userTag = state.userDataList.find { it.id == "auth" }!!.value2
        val userBan = state.userDataList.find { it.id == "community" }!!.value3

        if (currentMessage.isEmpty()) return@intent

        val timestamp = System.currentTimeMillis()
        val todayDocId = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        val chatData = mapOf(
            "message" to state.text1,
            "name" to state.text2,
            "ban" to userBan,
            "tag" to state.text3,
            "uid" to ""
        )

        Firebase.firestore
            .collection("chatting")
            .document("totalChat")
            .collection("totalChat")
            .document(todayDocId)
            .set(mapOf(timestamp.toString() to chatData), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("ChatSubmit", "채팅 전송 성공 (merge)")
            }
            .addOnFailureListener { e ->
                Log.e("ChatSubmit", "채팅 전송 실패: ${e.message}")
            }

        onCloseClick()
    }

    fun onOperatorLetterSubmitClick() = intent {

        val currentDate =
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val letterData = mapOf(
            "message" to state.text1,
            "title" to state.text2,
            "reward" to state.text4,
            "amount" to state.text5,
            "date" to currentDate,
            "link" to "0",
            "state" to "open"
        )

        //  필드명: "90" + state.tag
        val fieldKey = "90${state.text3}"

        Firebase.firestore
            .collection("code")
            .document("letter")
            .set(
                mapOf(fieldKey to letterData), // ✅ { "90abc" : { ... } }
                SetOptions.merge()              // ✅ 기존 필드 유지
            )
            .addOnSuccessListener {
                viewModelScope.launch {
                    intent {
                        postSideEffect(OperatorSideEffect.Toast("편지 전송 성공"))
                    }
                }
                onCloseClick()
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    intent {
                        postSideEffect(OperatorSideEffect.Toast("편지 전송 실패"))
                    }
                }
            }
    }

    //입력 가능하게 하는 코드
    @OptIn(OrbitExperimental::class)
    fun onTextChange(text1: String) = blockingIntent {

//        if (chatText.length <= 50) {
        reduce {
            state.copy(text1 = text1)
        }
//        }
    }

    //입력 가능하게 하는 코드
    @OptIn(OrbitExperimental::class)
    fun onTextChange2(text2: String) = blockingIntent {

//        if (chatText.length <= 50) {
        reduce {
            state.copy(text2 = text2)
        }
//        }
    }

    //입력 가능하게 하는 코드
    @OptIn(OrbitExperimental::class)
    fun onTextChange3(text3: String) = blockingIntent {

//        if (chatText.length <= 50) {
        reduce {
            state.copy(text3 = text3)
        }
//        }
    }

    //입력 가능하게 하는 코드
    @OptIn(OrbitExperimental::class)
    fun onTextChange4(text4: String) = blockingIntent {

//        if (chatText.length <= 50) {
        reduce {
            state.copy(text4 = text4)
        }
//        }
    }

    //입력 가능하게 하는 코드
    @OptIn(OrbitExperimental::class)
    fun onTextChange5(text5: String) = blockingIntent {

//        if (chatText.length <= 50) {
        reduce {
            state.copy(text5 = text5)
        }
//        }
    }

    fun alertStateChange(alertState: String) = intent {
        reduce {
            state.copy(
                alertState = alertState
            )
        }
    }

    fun onRankUpdate() = intent {
        onFirstGameRankUpdate()
        onSecondGameRankUpdate()
        onThirdGameEasyRankUpdate()
        onThirdGameNormalRankUpdate()
        onThirdGameHardRankUpdate()
    }

    fun onFirstGameRankUpdate() = intent {

        try {
            // 1️⃣ 로컬 유저 데이터 가져오기 (밴 제외)
            val allUserDataList = allUserDao.getAllUserDataNoBan()

            // 2️⃣ firstGame 점수 기준 내림차순 정렬
            val sortedList = allUserDataList.sortedByDescending { user ->
                user.firstGame.toLongOrNull() ?: 0L
            }

            // 3️⃣ Firestore rank / firstGame 문서 참조
            val docRef = Firebase.firestore
                .collection("rank")
                .document("firstGame")

            val rankMap = mutableMapOf<String, Any>()

            // 4️⃣ 1, 2, 3 ... 필드로 map 구성
            sortedList.take(500).forEachIndexed { index, user ->

                val map = mapOf(
                    "name" to user.name,
                    "tag" to user.tag,
                    "ban" to user.ban,
                    "score" to user.firstGame   // ⭐ firstGame 값
                )

                rankMap[(index + 1).toString()] = map
            }

            // 5️⃣ 문서 통째로 덮어쓰기
            docRef.set(rankMap)

            Log.d("Rank", "firstGame 랭킹 업데이트 완료")
            viewModelScope.launch {
                intent {
                    postSideEffect(OperatorSideEffect.Toast("firstGame 랭킹 업데이트 완료"))
                }
            }

        } catch (e: Exception) {
            Log.e("Rank", "firstGame 랭킹 업데이트 실패", e)
        }
    }

    fun onSecondGameRankUpdate() = intent {

        try {
            // 1️⃣ 로컬 유저 데이터 가져오기 (밴 제외)
            val allUserDataList = allUserDao.getAllUserDataNoBan()

            // 2️⃣ secondGame 점수 기준 오름차순 정렬 (소수 포함)
            val sortedList = allUserDataList.sortedBy { user ->
                user.secondGame.toDoubleOrNull() ?: Double.MAX_VALUE
            }

            // 3️⃣ Firestore rank / secondGame 문서 참조
            val docRef = Firebase.firestore
                .collection("rank")
                .document("secondGame")

            val rankMap = mutableMapOf<String, Any>()

            // 4️⃣ 1, 2, 3 ... 필드로 map 구성 (최대 500명)
            sortedList.take(500).forEachIndexed { index, user ->

                val map = mapOf(
                    "name" to user.name,
                    "tag" to user.tag,
                    "ban" to user.ban,
                    "score" to user.secondGame   // ⭐ secondGame 값
                )

                rankMap[(index + 1).toString()] = map
            }

            // 5️⃣ 문서 통째로 덮어쓰기
            docRef.set(rankMap)

            Log.d("Rank", "secondGame 랭킹 업데이트 완료")
            viewModelScope.launch {
                intent {
                    postSideEffect(
                        OperatorSideEffect.Toast("secondGame 랭킹 업데이트 완료")
                    )
                }
            }

        } catch (e: Exception) {
            Log.e("Rank", "secondGame 랭킹 업데이트 실패", e)
        }
    }

    fun onThirdGameEasyRankUpdate() = intent {

        try {
            // 1️⃣ 로컬 유저 데이터 가져오기 (밴 제외)
            val allUserDataList = allUserDao.getAllUserDataNoBan()

            // 2️⃣ firstGame 점수 기준 내림차순 정렬
            val sortedList = allUserDataList.sortedByDescending { user ->
                user.thirdGameEasy.toLongOrNull() ?: 0L
            }

            // 3️⃣ Firestore rank / firstGame 문서 참조
            val docRef = Firebase.firestore
                .collection("rank")
                .document("thirdGameEasy")

            val rankMap = mutableMapOf<String, Any>()

            // 4️⃣ 1, 2, 3 ... 필드로 map 구성
            sortedList.take(500).forEachIndexed { index, user ->

                val map = mapOf(
                    "name" to user.name,
                    "tag" to user.tag,
                    "ban" to user.ban,
                    "score" to user.thirdGameEasy   // ⭐ firstGame 값
                )

                rankMap[(index + 1).toString()] = map
            }

            // 5️⃣ 문서 통째로 덮어쓰기
            docRef.set(rankMap)

            viewModelScope.launch {
                intent {
                    postSideEffect(OperatorSideEffect.Toast("thirdGameEasy 랭킹 업데이트 완료"))
                }
            }

        } catch (e: Exception) {
            Log.e("Rank", "랭킹 업데이트 실패", e)
        }
    }

    fun onThirdGameNormalRankUpdate() = intent {

        try {
            // 1️⃣ 로컬 유저 데이터 가져오기 (밴 제외)
            val allUserDataList = allUserDao.getAllUserDataNoBan()

            // 2️⃣ firstGame 점수 기준 내림차순 정렬
            val sortedList = allUserDataList.sortedByDescending { user ->
                user.thirdGameNormal.toLongOrNull() ?: 0L
            }

            // 3️⃣ Firestore rank / firstGame 문서 참조
            val docRef = Firebase.firestore
                .collection("rank")
                .document("thirdGameNormal")

            val rankMap = mutableMapOf<String, Any>()

            // 4️⃣ 1, 2, 3 ... 필드로 map 구성
            sortedList.take(500).forEachIndexed { index, user ->

                val map = mapOf(
                    "name" to user.name,
                    "tag" to user.tag,
                    "ban" to user.ban,
                    "score" to user.thirdGameNormal   // ⭐ firstGame 값
                )

                rankMap[(index + 1).toString()] = map
            }

            // 5️⃣ 문서 통째로 덮어쓰기
            docRef.set(rankMap)

            viewModelScope.launch {
                intent {
                    postSideEffect(OperatorSideEffect.Toast("thirdGameNormal 랭킹 업데이트 완료"))
                }
            }

        } catch (e: Exception) {
            Log.e("Rank", "랭킹 업데이트 실패", e)
        }
    }

    fun onThirdGameHardRankUpdate() = intent {

        try {
            // 1️⃣ 로컬 유저 데이터 가져오기 (밴 제외)
            val allUserDataList = allUserDao.getAllUserDataNoBan()

            // 2️⃣ firstGame 점수 기준 내림차순 정렬
            val sortedList = allUserDataList.sortedByDescending { user ->
                user.thirdGameHard.toLongOrNull() ?: 0L
            }

            // 3️⃣ Firestore rank / firstGame 문서 참조
            val docRef = Firebase.firestore
                .collection("rank")
                .document("thirdGameHard")

            val rankMap = mutableMapOf<String, Any>()

            // 4️⃣ 1, 2, 3 ... 필드로 map 구성
            sortedList.take(500).forEachIndexed { index, user ->

                val map = mapOf(
                    "name" to user.name,
                    "tag" to user.tag,
                    "ban" to user.ban,
                    "score" to user.thirdGameHard   // ⭐ firstGame 값
                )

                rankMap[(index + 1).toString()] = map
            }

            // 5️⃣ 문서 통째로 덮어쓰기
            docRef.set(rankMap)

            viewModelScope.launch {
                intent {
                    postSideEffect(OperatorSideEffect.Toast("thirdGameHard 랭킹 업데이트 완료"))
                }
            }

        } catch (e: Exception) {
            Log.e("Rank", "랭킹 업데이트 실패", e)
        }
    }



}

@Immutable
data class OperatorState(
    val userDataList: List<User> = emptyList(),
    val patDataList: List<Pat> = emptyList(),
    val itemDataList: List<Item> = emptyList(),
    val allUserDataList: List<AllUser> = emptyList(),
    val situation: String = "world",
    val clickAllUserData: AllUser = AllUser(),
    val clickAllUserWorldDataList: List<String> = emptyList(),
    val allUserRankDataList: List<AllUser> = emptyList(),
    val text1: String = "",
    val alertState: String = "",
    val allAreaCount: String = "",
    val dialogState: String = "",
    val text2: String = "",
    val text3: String = "",
    val text4: String = "",
    val text5: String = "",
    val askMessages: List<OperatorMessage> = emptyList()
)

@Immutable
data class OperatorMessage(
    val timestamp: Long,
    val message: String,
    val name: String,
    val tag: String,
    val ban: String,
    val uid: String
)


//상태와 관련없는 것
sealed interface OperatorSideEffect{
    class Toast(val message:String): OperatorSideEffect
//    data object NavigateToDailyActivity: LoadingSideEffect

}