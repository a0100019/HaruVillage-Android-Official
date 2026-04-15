package com.a0100019.mypat.presentation.activity.daily.korean

import androidx.lifecycle.ViewModel
import com.a0100019.mypat.data.room.koreanIdiom.KoreanIdiom
import com.a0100019.mypat.data.room.koreanIdiom.KoreanIdiomDao
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.presentation.diary.DiarySideEffect
import com.a0100019.mypat.presentation.main.management.tryAcquireMedal
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.annotation.concurrent.Immutable
import javax.inject.Inject



@HiltViewModel
class KoreanViewModel @Inject constructor(
    private val userDao: UserDao,
    private val koreanDao: KoreanIdiomDao,
) : ViewModel(), ContainerHost<KoreanState, KoreanSideEffect> {

    override val container: Container<KoreanState, KoreanSideEffect> = container(
        initialState = KoreanState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(KoreanSideEffect.Toast(message = throwable.message.orEmpty()))
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
        val koreanDataList = koreanDao.getOpenKoreanIdiomData()
        val allKoreanDataList = koreanDao.getAllKoreanIdiomData()
        val userData = userDao.getAllUserData()

        reduce {
            state.copy(
                koreanDataList = koreanDataList,
                allKoreanDataList = allKoreanDataList,
                userData = userData
            )
        }

        if(koreanDataList.count {it.state == "별"} >= 10) {
            //매달, medal, 칭호6
            val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: ""
            val (updatedMedal, acquired) = tryAcquireMedal(currentMedals, 6)
            if (acquired) {
                userDao.update(id = "etc", value3 = updatedMedal)
                postSideEffect(KoreanSideEffect.Toast("칭호를 획득했습니다!"))
            }
        }
    }

    fun onFilterClick() = intent {

        if(state.filter == "일반") {
            val koreanStarList = koreanDao.getStarKoreanIdiomData()
            reduce {
                state.copy(
                    filter = "별",
                    koreanDataList = koreanStarList
                )
            }
        } else {
            val koreanDataList = koreanDao.getOpenKoreanIdiomData()
            reduce {
                state.copy(
                    filter = "일반",
                    koreanDataList = koreanDataList
                )
            }
        }
    }

    fun onKoreanClick(koreanIdiom: KoreanIdiom) = intent {
        reduce {
            state.copy(
                clickKoreanData = koreanIdiom,
                clickKoreanDataState = koreanIdiom.state
            )
        }

        if(koreanIdiom.state == "대기") {
            val allKoreanDataList = state.allKoreanDataList

            val koreanCharacterList = mutableListOf<String>()

            // 1. korean1~korean4 추가
            listOf(
                koreanIdiom.korean1,
                koreanIdiom.korean2,
                koreanIdiom.korean3,
                koreanIdiom.korean4
            ).forEach {
                if (it !in koreanCharacterList) koreanCharacterList.add(it)
            }

            while (koreanCharacterList.size < 10) {

                // 2. 랜덤 하나 추가
                val randomKoreanData = allKoreanDataList.shuffled().firstOrNull()
                // 3. 다시 korean1~4 추가 (중복 제외)
                listOf(
                    randomKoreanData!!.korean1,
                    randomKoreanData.korean2,
                    randomKoreanData.korean3,
                    randomKoreanData.korean4
                ).forEach {
                    if (koreanCharacterList.size < 10 && it !in koreanCharacterList) koreanCharacterList.add(it)
                }

            }

            reduce {
                state.copy(
                    koreanCharacterList = koreanCharacterList.shuffled(),
                    informationText = "아래 카드를 눌러 사자성어를 입력해주세요"
                )
            }

        }

    }

    fun onKoreanCharacterClick(koreanCharacter: String) = intent {

        if(state.koreanCharacter1 == "") {
            reduce {
                state.copy(
                    koreanCharacter1 = koreanCharacter
                )
            }
        } else if(state.koreanCharacter2 == "") {
            reduce {
                state.copy(
                    koreanCharacter2 = koreanCharacter
                )
            }
        } else if(state.koreanCharacter3 == "") {
            reduce {
                state.copy(
                    koreanCharacter3 = koreanCharacter
                )
            }
        } else if(state.koreanCharacter4 == "") {
            reduce {
                state.copy(
                    koreanCharacter4 = koreanCharacter
                )
            }
        }

    }

    fun onSubmitClick() = intent {

        if(
            state.koreanCharacter1 != "" &&
            state.koreanCharacter2 != "" &&
            state.koreanCharacter3 != "" &&
            state.koreanCharacter4 != ""
        ){
            val clickKoreanData = state.clickKoreanData

            val rightCount = listOf(
                state.koreanCharacter1 == clickKoreanData!!.korean1,
                state.koreanCharacter2 == clickKoreanData.korean2,
                state.koreanCharacter3 == clickKoreanData.korean3,
                state.koreanCharacter4 == clickKoreanData.korean4
            ).count { it }

            if (rightCount == 4) {

                val newClickKoreanData = state.clickKoreanData
                newClickKoreanData!!.state = "완료"

                koreanDao.update(newClickKoreanData)

                postSideEffect(KoreanSideEffect.Toast("정답입니다 (달빛+1000)"))

                //보상
                userDao.update(
                    id = "money",
                    value2 = (state.userData.find { it.id == "money" }!!.value2.toInt() + 1000).toString()
                )
                
                reduce {
                    state.copy(
                        clickKoreanDataState = "완료",
                        koreanCharacter1 = "",
                        koreanCharacter2 = "",
                        koreanCharacter3 = "",
                        koreanCharacter4 = "",
                        informationText = "아래 카드를 눌러 사자성어를 입력해주세요"
                    )
                }

                loadData()

            } else {

                val informationText =
                    state.koreanCharacter1.last().toString() +
                            state.koreanCharacter2.last() +
                            state.koreanCharacter3.last() +
                            state.koreanCharacter4.last() +
                            " : 오답 (${rightCount}개 일치)"

                reduce {
                    state.copy(
                        koreanCharacter1 = "",
                        koreanCharacter2 = "",
                        koreanCharacter3 = "",
                        koreanCharacter4 = "",
                        informationText = informationText
                    )
                }
                postSideEffect(KoreanSideEffect.Toast("오답입니다 (${rightCount}개 일치)"))
            }
        } else {
            postSideEffect(KoreanSideEffect.Toast("사자성어를 입력하세요"))
        }

    }

    fun onKoreanDeleteClick() = intent {
        if(state.koreanCharacter4 != "") {
            reduce {
                state.copy(
                    koreanCharacter4 = ""
                )
            }
        } else if(state.koreanCharacter3 != "") {
            reduce {
                state.copy(
                    koreanCharacter3 = ""
                )
            }
        } else if(state.koreanCharacter2 != "") {
            reduce {
                state.copy(
                    koreanCharacter2 = ""
                )
            }
        } else {
            reduce {
                state.copy(
                    koreanCharacter1 = ""
                )
            }
        }
    }

    fun onCloseClick() = intent {
        reduce {
            state.copy(
                clickKoreanData = null,
                clickKoreanDataState = "",
                koreanCharacter1 = "",
                koreanCharacter2 = "",
                koreanCharacter3 = "",
                koreanCharacter4 = "",
            )
        }
    }

    fun onStateChangeClick() = intent {

        val stateChangeKoreanData = state.clickKoreanData
        stateChangeKoreanData!!.state = if(stateChangeKoreanData.state == "별") "완료" else "별"
        koreanDao.update(stateChangeKoreanData)

        val koreanDataList = state.koreanDataList
        val updatedList = koreanDataList.map {
            if (it.id == stateChangeKoreanData.id) stateChangeKoreanData else it
        }

        reduce {
            state.copy(
                clickKoreanData = stateChangeKoreanData,
                clickKoreanDataState = stateChangeKoreanData.state,

                koreanDataList = updatedList
            )
        }

    }


}


@Immutable
data class KoreanState(
    val userData: List<User> = emptyList(),
    val koreanDataList: List<KoreanIdiom> = emptyList(),
    val allKoreanDataList: List<KoreanIdiom> = emptyList(),
    val koreanCharacterList: List<String> = emptyList(),

    val clickKoreanData: KoreanIdiom? = null,
    val filter: String = "일반",
    val clickKoreanDataState: String = "",
    val koreanCharacter1: String = "",
    val koreanCharacter2: String = "",
    val koreanCharacter3: String = "",
    val koreanCharacter4: String = "",
    val informationText: String = "아래 카드를 눌러 사자성어를 입력해주세요"

)


//상태와 관련없는 것
sealed interface KoreanSideEffect{
    class Toast(val message:String): KoreanSideEffect
//    data object NavigateToDailyActivity: LoadingSideEffect

}