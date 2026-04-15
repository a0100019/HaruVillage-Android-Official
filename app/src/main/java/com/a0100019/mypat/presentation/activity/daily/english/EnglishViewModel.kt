package com.a0100019.mypat.presentation.activity.daily.english

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ViewModel
import com.a0100019.mypat.data.room.english.English
import com.a0100019.mypat.data.room.english.EnglishDao
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.presentation.main.management.addMedalAction
import com.a0100019.mypat.presentation.main.management.getMedalActionCount
import com.a0100019.mypat.presentation.main.management.RewardAdManager
import com.a0100019.mypat.presentation.main.management.tryAcquireMedal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class EnglishViewModel @Inject constructor(
    private val userDao: UserDao,
    private val englishDao: EnglishDao,
    private val application: Application,
    private val rewardAdManager: RewardAdManager
) : ViewModel(), ContainerHost<EnglishState, EnglishSideEffect> {

    override val container: Container<EnglishState, EnglishSideEffect> = container(
        initialState = EnglishState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(EnglishSideEffect.Toast(message = throwable.message.orEmpty()))
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

        val englishDataList = englishDao.getOpenEnglishData()
        val words = WordRepository.loadWords(application)
        val userData = userDao.getAllUserData()
        val removeAd = userData.find { it.id == "name" }!!.value3

        reduce {
            state.copy(
                englishDataList = englishDataList,
                allWordsData = words,
                userData = userData,
                removeAd = removeAd
            )
        }

        if(englishDataList.count { it.state == "완료" || it.state == "별"} >= 50) {
            //매달, medal, 칭호7
            val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: ""
            val (updatedMedal, acquired) = tryAcquireMedal(currentMedals, 7)
            if (acquired) {
                userDao.update(id = "etc", value3 = updatedMedal)
                postSideEffect(EnglishSideEffect.Toast("칭호를 획득했습니다!"))
            }
        }

    }

    fun onEnglishClick(english: English) = intent {
        reduce {
            state.copy(
                clickEnglishData = english,
                clickEnglishDataState = english.state
            )
        }

    }

    fun onAlphabetClick(alphabet: String) = intent {
        val englishTextList = state.englishTextList.toMutableList()

        if(englishTextList[0] == " ") {
            englishTextList[0] = alphabet
            reduce {
                state.copy(
                    englishTextList = englishTextList
                )
            }
        } else if(englishTextList[1] == " ") {
            englishTextList[1] = alphabet
            reduce {
                state.copy(
                    englishTextList = englishTextList
                )
            }
        } else if(englishTextList[2] == " ") {
            englishTextList[2] = alphabet
            reduce {
                state.copy(
                    englishTextList = englishTextList
                )
            }
        } else if(englishTextList[3] == " ") {
            englishTextList[3] = alphabet
            reduce {
                state.copy(
                    englishTextList = englishTextList
                )
            }
        } else if(englishTextList[4] == " ") {
            englishTextList[4] = alphabet
            reduce {
                state.copy(
                    englishTextList = englishTextList
                )
            }
        }
    }

    fun onAlphabetDeleteClick() = intent {

        val englishTextList = state.englishTextList.toMutableList()

        if(englishTextList[4] != " ") {
            englishTextList[4] = " "
            reduce {
                state.copy(
                    englishTextList = englishTextList
                )
            }
        } else if(englishTextList[3] != " ") {
            englishTextList[3] = " "
            reduce {
                state.copy(
                    englishTextList = englishTextList
                )
            }
        } else if(englishTextList[2] != " ") {
            englishTextList[2] = " "
            reduce {
                state.copy(
                    englishTextList = englishTextList
                )
            }
        } else if(englishTextList[1] != " ") {
            englishTextList[1] = " "
            reduce {
                state.copy(
                    englishTextList = englishTextList
                )
            }
        } else if(englishTextList[0] != " ") {
            englishTextList[0] = " "
            reduce {
                state.copy(
                    englishTextList = englishTextList
                )
            }
        }

    }

    fun onHardSubmitClick() = intent {

        if(state.englishTextList[4] != " ") {

            val testEnglish = state.englishTextList.joinToString("")
            val allWordsData = state.allWordsData

            if(testEnglish in allWordsData) {
                if (testEnglish == state.clickEnglishData!!.word) {

                    val newClickEnglishData = state.clickEnglishData
                    newClickEnglishData!!.state = "완료"

                    if(state.clickEnglishDataState == "어려움") {//보상
                        userDao.update(
                            id = "money",
                            value2 = (state.userData.find { it.id == "money" }!!.value2.toInt() + 2000).toString()
                        )
                        postSideEffect(EnglishSideEffect.Toast("정답입니다 (달빛 +2000)"))
                    } else {
                        //보상
                        userDao.update(
                            id = "money",
                            value2 = (state.userData.find { it.id == "money" }!!.value2.toInt() + 500).toString()
                        )
                        postSideEffect(EnglishSideEffect.Toast("정답입니다 (달빛 +500)"))
                    }

                    englishDao.update(newClickEnglishData)

                    reduce {
                        state.copy(
                            clickEnglishDataState = "완료",
                            englishTextList = listOf(" ", " ", " ", " ", " "),
                            failEnglishList = emptyList(),
                            failEnglishStateList = emptyList(),
                        )
                    }

                    loadData()

                } else {

                    val answerEnglish = state.clickEnglishData!!.word

                    val failEnglishList = state.failEnglishList.toMutableList()
                    failEnglishList.add(testEnglish)

                    val failEnglishStateList = state.failEnglishStateList.toMutableList()
                    val failEnglishState = state.englishTextList.mapIndexed { index, s ->
                        if (s == answerEnglish[index].toString()) {
                            '2'
                        } else if (answerEnglish.contains(s)) {
                            '1'
                        } else {
                            '0'
                        }
                    }.joinToString("")
                    failEnglishStateList.add(failEnglishState)

                    val notUseEnglishList = state.notUseEnglishList.toMutableList()
                    state.englishTextList.forEach {
                        if(it !in answerEnglish) {
                            notUseEnglishList.add(it)
                        }
                    }

                    val useEnglishList = state.useEnglishList.toMutableList()
                    state.englishTextList.forEach {
                        if(it in answerEnglish) {
                            useEnglishList.add(it)
                        }
                    }

                    reduce {
                        state.copy(
                            failEnglishList = failEnglishList,
                            failEnglishStateList = failEnglishStateList,
                            englishTextList = listOf(" ", " ", " ", " ", " "),
                            notUseEnglishList = notUseEnglishList,
                            useEnglishList = useEnglishList
                        )
                    }

                }

            } else {
                postSideEffect(EnglishSideEffect.Toast("존재하지 않는 단어입니다"))
            }

        } else {
            postSideEffect(EnglishSideEffect.Toast("영어 단어를 입력하세요"))
        }

    }

    fun onEasySubmitClick() = intent {

        if(state.englishTextList[4] != " ") {

            val testEnglish = state.englishTextList.joinToString("")
            val allWordsData = state.allWordsData

            if(testEnglish in allWordsData) {
                if (testEnglish == state.clickEnglishData!!.word) {

                    val newClickEnglishData = state.clickEnglishData
                    newClickEnglishData!!.state = "완료"

                    if(state.clickEnglishDataState == "쉬움") {//보상
                        userDao.update(
                            id = "money",
                            value2 = (state.userData.find { it.id == "money" }!!.value2.toInt() + 1000).toString()
                        )
                        postSideEffect(EnglishSideEffect.Toast("정답입니다 (달빛 +1000)"))
                    } else {
                        //보상
                        userDao.update(
                            id = "money",
                            value2 = (state.userData.find { it.id == "money" }!!.value2.toInt() + 250).toString()
                        )
                        postSideEffect(EnglishSideEffect.Toast("정답입니다 (달빛 +250)"))
                    }

                    englishDao.update(newClickEnglishData)

                    reduce {
                        state.copy(
                            clickEnglishDataState = "완료",
                            englishTextList = listOf(" ", " ", " ", " ", " "),
                            failEnglishList = emptyList(),
                            failEnglishStateList = emptyList(),
                        )
                    }

                    loadData()

                } else {

                    val answerEnglish = state.clickEnglishData!!.word

                    val failEnglishList = state.failEnglishList.toMutableList()
                    failEnglishList.add(testEnglish)

                    val failEnglishStateList = state.failEnglishStateList.toMutableList()
                    val failEnglishState = state.englishTextList.mapIndexed { index, s ->
                        if (s == answerEnglish[index].toString()) {
                            '2'
                        } else if (answerEnglish.contains(s)) {
                            '1'
                        } else {
                            '0'
                        }
                    }.joinToString("")
                    failEnglishStateList.add(failEnglishState)

                    val notUseEnglishList = state.notUseEnglishList.toMutableList()
                    state.englishTextList.forEach {
                        if(it !in answerEnglish) {
                            notUseEnglishList.add(it)
                        }
                    }

                    val useEnglishList = state.useEnglishList.toMutableList()
                    state.englishTextList.forEach {
                        if(it in answerEnglish) {
                            useEnglishList.add(it)
                        }
                    }

                    reduce {
                        state.copy(
                            failEnglishList = failEnglishList,
                            failEnglishStateList = failEnglishStateList,
                            englishTextList = listOf(" ", " ", " ", " ", " "),
                            notUseEnglishList = notUseEnglishList,
                            useEnglishList = useEnglishList
                        )
                    }

                    postSideEffect(EnglishSideEffect.Toast("오답입니다"))

                }

            } else {
                postSideEffect(EnglishSideEffect.Toast("존재하지 않는 단어입니다"))
            }

        } else {
            postSideEffect(EnglishSideEffect.Toast("영어 단어를 입력하세요"))
        }

    }

    fun onFilterClick() = intent {

        if(state.filter == "일반") {
            val englishStarList = englishDao.getStarEnglishData()
            reduce {
                state.copy(
                    filter = "별",
                    englishDataList = englishStarList
                )
            }
        } else {
            val englishDataList = englishDao.getOpenEnglishData()
            reduce {
                state.copy(
                    filter = "일반",
                    englishDataList = englishDataList
                )
            }
        }
    }

    fun onCloseClick() = intent {
        reduce {
            state.copy(
                clickEnglishData = null,
                clickEnglishDataState = "",
                englishTextList = listOf(" ", " ", " ", " ", " "),
                failEnglishList = emptyList(),
                failEnglishStateList = emptyList(),
                notUseEnglishList = emptyList(),
                useEnglishList = emptyList(),
                situation = ""
            )
        }
    }

    fun onStateChangeClick() = intent {

        val stateChangeEnglishData = state.clickEnglishData
        stateChangeEnglishData!!.state = if(stateChangeEnglishData.state == "별") "완료" else "별"
        englishDao.update(stateChangeEnglishData)

        val englishDataList = state.englishDataList
        val updatedList = englishDataList.map {
            if (it.id == stateChangeEnglishData.id) stateChangeEnglishData else it
        }

        reduce {
            state.copy(
                clickEnglishData = stateChangeEnglishData,
                clickEnglishDataState = stateChangeEnglishData.state,
                englishDataList = updatedList
            )
        }

    }

    fun onAdClick() = intent {

//        if(state.removeAd == "0") {
//            postSideEffect(EnglishSideEffect.ShowRewardAd)
//        } else {
//            onRewardEarned()
//        }

    }

//    fun showRewardAd(activity: Activity) {
//        rewardAdManager.show(
//            activity = activity,
//            onReward = {
//                onRewardEarned()
//            },
//            onNotReady = {
//                intent {
//                    postSideEffect(
//                        EnglishSideEffect.Toast(
//                            "광고를 불러오는 중이에요. 잠시 후 다시 시도해주세요."
//                        )
//                    )
//                }
//            }
//        )
//    }

    fun onSituationChange(situation: String) = intent {

        reduce {
            state.copy(
                situation = situation
            )
        }
    }

    fun onRewardEarned() = intent {

        postSideEffect(EnglishSideEffect.Toast("힌트를 얻었습니다!"))

        if(state.clickEnglishDataState == "어려움"){
            val stateChangeEnglishData = state.clickEnglishData
            stateChangeEnglishData!!.state = "뜻"
            englishDao.update(stateChangeEnglishData)

            val newEnglishData = englishDao.getOpenEnglishData()

            reduce {
                state.copy(
                    clickEnglishDataState = "뜻",
                    englishDataList = newEnglishData,
                    situation = ""
                )
            }
        } else {
            val stateChangeEnglishData = state.clickEnglishData
            stateChangeEnglishData!!.state = "쉬움뜻"
            englishDao.update(stateChangeEnglishData)

            val newEnglishData = englishDao.getOpenEnglishData()

            reduce {
                state.copy(
                    clickEnglishDataState = "쉬움뜻",
                    englishDataList = newEnglishData,
                    situation = ""
                )
            }
        }

        //@@@@@@@@@@@@@@@@@@@@칭호
        var medalData = userDao.getAllUserData().find { it.id == "name" }!!.value2
        medalData = addMedalAction(medalData, actionId = 27)
        userDao.update(
            id = "name",
            value2 = medalData
        )

        if(getMedalActionCount(medalData, actionId = 27) == 15) {
            //매달, medal, 칭호27
            val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: ""
            val (updatedMedal, acquired) = tryAcquireMedal(currentMedals, 27)
            if (acquired) {
                userDao.update(id = "etc", value3 = updatedMedal)
                postSideEffect(EnglishSideEffect.Toast("칭호를 획득했습니다!"))
            }
        }

    }

    fun onPracticeSubmitClick() = intent {
        if(state.englishTextList[4] != " ") {

            val testEnglish = state.englishTextList.joinToString("")
            val allWordsData = state.allWordsData

            if(testEnglish in allWordsData) {
                if (testEnglish == "apple") {

                    postSideEffect(EnglishSideEffect.Toast("정답입니다!"))

                    //매달, medal, 칭호30
                    val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: ""
                    val (updatedMedal, acquired) = tryAcquireMedal(currentMedals, 30)
                    if (acquired) {
                        userDao.update(id = "etc", value3 = updatedMedal)
                        postSideEffect(EnglishSideEffect.Toast("칭호를 획득했습니다!"))
                    }

                    reduce {
                        state.copy(
                            situation = "",
                            englishTextList = listOf(" ", " ", " ", " ", " "),
                            failEnglishList = emptyList(),
                            failEnglishStateList = emptyList(),
                        )
                    }

                } else {

                    postSideEffect(EnglishSideEffect.Toast("오답입니다. 다시 생각해보세요 (a*ple)"))
                    reduce {
                        state.copy(
                            englishTextList = listOf(" ", " ", " ", " ", " "),
                        )
                    }

                }

            } else {
                postSideEffect(EnglishSideEffect.Toast("존재하지 않는 단어입니다"))
            }

        } else {
            postSideEffect(EnglishSideEffect.Toast("영어 단어를 입력하세요"))
        }

    }

    fun onLevelClick(level: String) = intent {

        if(level == "easy") {

            val stateChangeEnglishData = state.clickEnglishData
            stateChangeEnglishData!!.state = "쉬움"
            englishDao.update(stateChangeEnglishData)

            val newEnglishData = englishDao.getOpenEnglishData()

            reduce {
                state.copy(
                    clickEnglishDataState = "쉬움",
                    englishDataList = newEnglishData,
                )
            }

        } else {

            val stateChangeEnglishData = state.clickEnglishData
            stateChangeEnglishData!!.state = "어려움"
            englishDao.update(stateChangeEnglishData)

            val newEnglishData = englishDao.getOpenEnglishData()

            reduce {
                state.copy(
                    clickEnglishDataState = "어려움",
                    englishDataList = newEnglishData,
                )
            }

        }

    }

}

@Immutable
data class EnglishState(

    val userData: List<User> = emptyList(),
    val englishDataList: List<English> = emptyList(),
    val clickEnglishData: English? = null,
    val filter: String = "일반",
    val clickEnglishDataState: String = "",
    val englishTextList: List<String> = listOf(" ", " ", " ", " ", " "),
    val allWordsData: List<String> = emptyList(),
    val failEnglishList: List<String> = emptyList(),
    val failEnglishStateList: List<String> = emptyList(),
    val notUseEnglishList: List<String> = emptyList(),
    val useEnglishList: List<String> = emptyList(),
    val situation: String = "",
    val removeAd: String = "0"
    )


//상태와 관련없는 것
sealed interface EnglishSideEffect{
    class Toast(val message:String): EnglishSideEffect
//    data object NavigateToDailyActivity: LoadingSideEffect

//    data object ShowRewardAd : EnglishSideEffect

}