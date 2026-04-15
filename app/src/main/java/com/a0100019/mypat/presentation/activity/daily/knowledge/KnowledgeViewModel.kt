package com.a0100019.mypat.presentation.activity.daily.knowledge

import androidx.lifecycle.ViewModel
import com.a0100019.mypat.data.room.knowledge.Knowledge
import com.a0100019.mypat.data.room.knowledge.KnowledgeDao
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
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
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class KnowledgeViewModel @Inject constructor(
    private val userDao: UserDao,
    private val knowledgeDao: KnowledgeDao,
) : ViewModel(), ContainerHost<KnowledgeState, KnowledgeSideEffect> {

    override val container: Container<KnowledgeState, KnowledgeSideEffect> = container(
        initialState = KnowledgeState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(KnowledgeSideEffect.Toast(message = throwable.message.orEmpty()))
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
        val knowledgeDataList = knowledgeDao.getOpenKnowledgeData()
        val allKnowledgeDataList = knowledgeDao.getAllKnowledgeData()
        val userData = userDao.getAllUserData()

        reduce {
            state.copy(
                knowledgeDataList = knowledgeDataList,
                allKnowledgeDataList = allKnowledgeDataList,
                userData = userData
            )
        }

    }

    fun onFilterClick() = intent {

        if(state.filter == "일반") {
            val knowledgeStarList = knowledgeDao.getStarKnowledgeData()
            reduce {
                state.copy(
                    filter = "별",
                    knowledgeDataList = knowledgeStarList
                )
            }
        } else {
            val knowledgeDataList = knowledgeDao.getOpenKnowledgeData()
            reduce {
                state.copy(
                    filter = "일반",
                    knowledgeDataList = knowledgeDataList
                )
            }
        }
    }

    fun onCloseClick() = intent {
        reduce {
            state.copy(
                clickKnowledgeData = null,
                clickKnowledgeDataState = "",
                situation = "",
                text = ""
            )
        }
    }

    fun onStateChangeClick() = intent {

        val stateChangeKnowledgeData = state.clickKnowledgeData
        stateChangeKnowledgeData!!.state = if(stateChangeKnowledgeData.state == "별") "완료" else "별"
        knowledgeDao.update(stateChangeKnowledgeData)

        val knowledgeDataList = state.knowledgeDataList
        val updatedList = knowledgeDataList.map {
            if (it.id == stateChangeKnowledgeData.id) stateChangeKnowledgeData else it
        }

        reduce {
            state.copy(
                clickKnowledgeData = stateChangeKnowledgeData,
                clickKnowledgeDataState = stateChangeKnowledgeData.state,

                knowledgeDataList = updatedList
            )
        }

    }

    fun onKnowledgeClick(knowledge: Knowledge) = intent {

        reduce {
            state.copy(
                clickKnowledgeData = knowledge,
                clickKnowledgeDataState = knowledge.state
            )
        }

    }

    fun onSubmitClick() = intent {

        if(state.clickKnowledgeData!!.answer == state.text) {
            val newClickKnowledgeData = state.clickKnowledgeData
            newClickKnowledgeData!!.state = "완료"

            knowledgeDao.update(newClickKnowledgeData)

            postSideEffect(KnowledgeSideEffect.Toast("수고하셨습니다 (달빛 +1000)"))

            //매달, medal, 칭호31
            val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: ""
            val (updatedMedal, acquired) = tryAcquireMedal(currentMedals, 31)
            if (acquired) {
                userDao.update(id = "etc", value3 = updatedMedal)
                postSideEffect(KnowledgeSideEffect.Toast("칭호를 획득했습니다!"))
            }


            //보상
            userDao.update(
                id = "money",
                value2 = (state.userData.find { it.id == "money" }!!.value2.toInt() + 1000).toString()
            )

            reduce {
                state.copy(
                    clickKnowledgeDataState = "완료",
                    text = ""
                )
            }

            loadData()

        } else {

            reduce {
                state.copy(
                    text = ""
                )
            }

            postSideEffect(KnowledgeSideEffect.Toast("정확히 입력해주세요 (띄어쓰기 포함)"))

        }

    }

    //입력 가능하게 하는 코드
    @OptIn(OrbitExperimental::class)
    fun onTextChange(text: String) = blockingIntent {
        reduce {
            state.copy(text = text)
        }
    }

}

@Immutable
data class KnowledgeState(
    val userData: List<User> = emptyList(),
    val knowledgeDataList: List<Knowledge> = emptyList(),
    val allKnowledgeDataList: List<Knowledge> = emptyList(),

    val clickKnowledgeData: Knowledge? = null,
    val clickKnowledgeDataState: String = "",
    val filter: String = "일반",
    val situation: String = "",
    val text: String = ""

    )


//상태와 관련없는 것
sealed interface KnowledgeSideEffect{
    class Toast(val message:String): KnowledgeSideEffect
//    data object NavigateToDailyActivity: LoadingSideEffect

}