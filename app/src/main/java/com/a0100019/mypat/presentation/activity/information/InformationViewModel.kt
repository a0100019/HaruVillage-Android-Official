package com.a0100019.mypat.presentation.activity.information

import androidx.lifecycle.ViewModel
import com.a0100019.mypat.data.room.allUser.AllUserDao
import com.a0100019.mypat.data.room.item.Item
import com.a0100019.mypat.data.room.item.ItemDao
import com.a0100019.mypat.data.room.area.Area
import com.a0100019.mypat.data.room.area.AreaDao
import com.a0100019.mypat.data.room.pat.Pat
import com.a0100019.mypat.data.room.pat.PatDao
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.data.room.world.World
import com.a0100019.mypat.data.room.world.WorldDao
import com.a0100019.mypat.presentation.main.management.medalExplain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject


@HiltViewModel
class InformationViewModel @Inject constructor(
    private val userDao: UserDao,
    private val worldDao: WorldDao,
    private val patDao: PatDao,
    private val itemDao: ItemDao,
    private val allUserDao: AllUserDao,
    private val areaDao: AreaDao

    ) : ViewModel(), ContainerHost<InformationState, InformationSideEffect> {

    override val container: Container<InformationState, InformationSideEffect> = container(
        initialState = InformationState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(InformationSideEffect.Toast(message = throwable.message.orEmpty()))
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

        // 맵 데이터 가져오기
        val areaData = worldDao.getWorldDataById(1)

        // 펫 월드 데이터 리스트 가져오기
        val patWorldDataList = worldDao.getWorldDataListByType(type = "pat") ?: emptyList()
        val patDataList = patWorldDataList.mapNotNull { patWorldData ->
            patDao.getPatDataById(patWorldData.value)
        }

        // 아이템 월드 데이터 리스트 가져오기
        val itemWorldDataList = worldDao.getWorldDataListByType(type = "item") ?: emptyList()
        val itemDataList = itemWorldDataList.mapNotNull { itemWorldData ->
            itemDao.getItemDataById(itemWorldData.value)
        }

        val userDataList = userDao.getAllUserData()
        val allAreaDataList = areaDao.getAllAreaData()
        val allPatDataList = patDao.getAllPatData()
        val allItemDataList = itemDao.getAllItemDataWithShadow()
        val allUserDataList = allUserDao.getAllUserDataNoBan()
        val worldDataList = worldDao.getAllWorldData()

        if (allUserDataList.size > 4) {

            val myFirstGame  = userDataList.find { it.id == "firstGame" }!!.value.toInt()
            val mySecondGame = userDataList.find { it.id == "secondGame" }!!.value.toDouble()
            val myThirdGame  = userDataList.find { it.id == "thirdGame" }!!

            val firstGameRank       = allUserDataList.map { it.firstGame }.rankHigherBetter(myFirstGame)
            val secondGameRank      = allUserDataList.map { it.secondGame }.rankLowerBetter(mySecondGame)
            val thirdGameEasyRank   = allUserDataList.map { it.thirdGameEasy }.rankHigherBetter(myThirdGame.value.toInt())
            val thirdGameNormalRank = allUserDataList.map { it.thirdGameNormal }.rankHigherBetter(myThirdGame.value2.toInt())
            val thirdGameHardRank   = allUserDataList.map { it.thirdGameHard }.rankHigherBetter(myThirdGame.value3.toInt())

            reduce {
                state.copy(
                    gameRankList = listOf(
                        firstGameRank, secondGameRank,
                        thirdGameEasyRank, thirdGameNormalRank, thirdGameHardRank
                    ).map { it.toString() }
                )
            }
        }

        reduce {
            state.copy(
                areaData = areaData,
                patDataList = patDataList,
                itemDataList = itemDataList,
                userData = userDataList,
                allAreaDataList = allAreaDataList,
                allPatDataList = allPatDataList,
                allItemDataList = allItemDataList,
                worldDataList = worldDataList
            )
        }

    }

    //입력 가능하게 하는 코드
    @OptIn(OrbitExperimental::class)
    fun onTextChange(text: String) = blockingIntent {

        reduce {
            state.copy(text = text)
        }

    }

    fun onSituationChange(newSituation: String) = intent {
        reduce {
            state.copy(
                situation = newSituation,
            )
        }
    }

    fun onClose() = intent {
        reduce {
            state.copy(
                situation = "",
                text = "",
                medalExplain = ""
            )
        }
    }

    fun onIntroductionChangeClick() = intent {

        userDao.update(id = "etc", value = state.text)

        postSideEffect(InformationSideEffect.Toast("인삿말을 변경하였습니다."))

        onClose()
        loadData()
    }

    fun onMedalChangeClick(index: Int) = intent {

        val myMedal = state.userData.find { it.id == "etc" }?.value3 ?: ""
        val medalList = myMedal
            .split("/")
            .mapNotNull { it.toIntOrNull() }
            .toMutableList()

        if (medalList.isEmpty()) return@intent

        medalList[0] = index
        userDao.update(id = "etc", value3 = medalList.joinToString("/"))

        postSideEffect(InformationSideEffect.Toast("칭호를 변경하였습니다."))
        onClose()
        loadData()
    }

    fun onMedalExplainClick(index: Int) = intent {
        reduce {
            state.copy(
                situation = "medalExplain",
                medalExplain = medalExplain(index)
            )
        }
    }


}

@Immutable
data class InformationState(
    val userData: List<User> = emptyList(),
    val patDataList: List<Pat> = emptyList(),
    val itemDataList: List<Item> = emptyList(),
    val allPatDataList: List<Pat> = emptyList(),
    val allItemDataList: List<Item> = emptyList(),
    val allAreaDataList: List<Area> = emptyList(),
    val worldDataList: List<World> = emptyList(),

    val gameRankList: List<String> = listOf("-", "-", "-", "-", "-"),

    val areaData: World? = null,
    val text: String = "",
    val situation: String = "",
    val medalExplain: String = ""

    )


sealed interface InformationSideEffect {
    class Toast(val message: String) : InformationSideEffect
}

// 높은 점수가 좋은 게임: 나보다 높은 사람 수 + 1
private fun List<String>.rankHigherBetter(myScore: Int): Int =
    sortedDescending().count { it.toInt() > myScore } + 1

// 낮은 점수가 좋은 게임: 나보다 낮은 사람 수 + 1
private fun List<String>.rankLowerBetter(myScore: Double): Int =
    sortedDescending().count { it.toDouble() < myScore } + 1