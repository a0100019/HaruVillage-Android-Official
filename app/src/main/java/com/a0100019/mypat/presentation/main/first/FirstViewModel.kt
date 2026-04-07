package com.a0100019.mypat.presentation.main.first

import androidx.lifecycle.ViewModel
import com.a0100019.mypat.data.room.allUser.AllUserDao
import com.a0100019.mypat.data.room.area.AreaDao
import com.a0100019.mypat.data.room.item.ItemDao
import com.a0100019.mypat.data.room.pat.PatDao
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.data.room.world.WorldDao
import com.a0100019.mypat.presentation.diary.DiarySideEffect
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
class FirstViewModel @Inject constructor(
    private val userDao: UserDao,
    private val worldDao: WorldDao,
    private val patDao: PatDao,
    private val itemDao: ItemDao,
    private val allUserDao: AllUserDao,
    private val areaDao: AreaDao
) : ViewModel(), ContainerHost<FirstState, FirstSideEffect> {

    override val container: Container<FirstState, FirstSideEffect> = container(
        initialState = FirstState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(FirstSideEffect.Toast(message = throwable.message.orEmpty()))
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

        reduce {
            state.copy(
                userDataList = userDataList
            )
        }
    }

    fun onClose() = intent {
        reduce {
            state.copy(
                situation = ""
            )
        }
    }

    fun onSituationChange(situation: String) = intent {
        reduce {
            state.copy(
                situation = situation
            )
        }
    }

    fun onExitClick() = intent {
        postSideEffect(FirstSideEffect.ExitApp)
    }


}

@Immutable
data class FirstState(
    val userDataList: List<User> = emptyList(),
    val situation: String = ""
)


//상태와 관련없는 것
sealed interface FirstSideEffect{
    class Toast(val message:String): FirstSideEffect

    object ExitApp : FirstSideEffect   // 앱 종료용

//    data object NavigateToDailyActivity: LoadingSideEffect

}