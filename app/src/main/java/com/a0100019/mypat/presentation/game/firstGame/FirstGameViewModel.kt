package com.a0100019.mypat.presentation.game.firstGame

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a0100019.mypat.data.room.pat.Pat
import com.a0100019.mypat.data.room.pat.PatDao
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.presentation.game.secondGame.SecondGameSideEffect
import com.a0100019.mypat.presentation.main.management.tryAcquireMedal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.lang.Math.abs
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.math.pow


@HiltViewModel
class FirstGameViewModel @Inject constructor(
    private val userDao: UserDao,
    private val patDao: PatDao
) : ViewModel(), ContainerHost<FirstGameState, FirstGameSideEffect> {

    override val container: Container<FirstGameState, FirstGameSideEffect> = container(
        initialState = FirstGameState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _ , throwable ->
                intent {
                    postSideEffect(FirstGameSideEffect.Toast(message = throwable.message.orEmpty()))
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
        val patData = patDao.getPatDataById(userDataList.find { it.id == "selectPat" }?.value ?: "0")
        reduce {
            state.copy(
                userData = userDataList,
                patData = patData
            )
        }
    }

    fun onGameStartClick(surfaceWidthDp: Dp, surfaceHeightDp:Dp) = intent {

        val snowballSize = surfaceWidthDp * 0.1f
        val targetSize = surfaceWidthDp * 0.3f // 나중에 0.3으로 !!
        val snowballX = surfaceWidthDp * 0.5f - snowballSize/2 // 가로의 50%
        val snowballY = surfaceHeightDp * 0.8f - snowballSize/2 // 세로의 90%
        val targetX = surfaceWidthDp * 0.5f - targetSize/2
        val targetY = surfaceHeightDp * 0.2f - targetSize/2

        reduce {
            state.copy(
                snowballX = snowballX,
                snowballY = snowballY,
                snowballSize = snowballSize,
                surfaceWidthDp = surfaceWidthDp,
                surfaceHeightDp = surfaceHeightDp,
                maxPower = (surfaceHeightDp.value.toInt())*6/5,
                targetX = targetX,
                targetY = targetY,
                targetSize = targetSize,
                score = 0,
                level = 0,
                situation = "회전",
                rotationAngle = 0f,
                isRotating = true
            )
        }

        viewModelScope.launch {
            while (state.isRotating) { // isActive를 체크하여 안전하게 종료 가능
                reduce {
                    state.copy(rotationAngle = state.rotationAngle + 2f)
                }
                delay((10-state.level/10).toLong()) // 회전속도
            }
        }

    }

    fun onGameReStartClick() = intent {
        val snowballX = state.surfaceWidthDp * 0.5f - state.snowballSize/2 // 가로의 50%
        val snowballY = state.surfaceHeightDp * 0.8f - state.snowballSize/2 // 세로의 90%
        val targetX = state.surfaceWidthDp * 0.5f - state.targetSize/2
        val targetY = state.surfaceHeightDp * 0.2f - state.targetSize/2

        val patData = patDao.getPatDataById(state.patData.id.toString())
        val userDataList = userDao.getAllUserData()

        reduce {
            state.copy(
                snowballX = snowballX,
                snowballY = snowballY,
                targetX = targetX,
                targetY = targetY,
                score = 0,
                level = 0,
                situation = "회전",
                rotationAngle = 0f,
                patData = patData,
                isRotating = true,
                shotPower = 0,
                userData = userDataList
            )
        }

        viewModelScope.launch {
            while (state.isRotating) { // isActive를 체크하여 안전하게 종료 가능
                reduce {
                    state.copy(rotationAngle = state.rotationAngle + 2f)
                }
                delay((10-state.level/10).toLong()) // 회전속도
            }
        }
    }

    fun onMoveClick() = intent {

        reduce {
            state.copy(
                situation = "발사중",
                isShotSetting = false
            )
        }

        val velocity = state.shotPower.dp
        val rotationAngle = if(state.rotationAngle >= 0) state.rotationAngle%360f else (state.rotationAngle+3600f)%360f

        var newVelocityX = 0.dp
        var newVelocityY = 0.dp

        if(rotationAngle in 0f..90f) {
            newVelocityX = velocity * (rotationAngle/90f)
            newVelocityY = -velocity * (90f - rotationAngle)/90f
        } else if (rotationAngle in 90f..180f) {
            newVelocityX = velocity * (180f - rotationAngle)/90f
            newVelocityY = -velocity * (90f - rotationAngle)/90f
        } else if (rotationAngle in 180f..270f) {
            newVelocityX = -velocity * (rotationAngle - 180f)/90f
            newVelocityY = -velocity * (rotationAngle - 270f)/90f
        } else {
            newVelocityX = velocity * (rotationAngle-360f)/90f
            newVelocityY = -velocity * (rotationAngle-270f)/90f
        }

        viewModelScope.launch {

            reduce {
                state.copy(
                    snowballY = state.snowballY + newVelocityY,
                    snowballX = state.snowballX + newVelocityX
                )
            }

            delay(1500)
            //공 이동 끝

            //표적과의 거리
            val distance = sqrt(
                (state.targetX + state.targetSize/2 - (state.snowballX + state.snowballSize/2)).value.pow(2) +
                        (state.targetY + state.targetSize/2 - (state.snowballY + state.snowballSize/2)).value.pow(2)
            )

            //맵안에 있는지
            val mapIn = (state.snowballX > 0.dp && state.snowballX < state.surfaceWidthDp
                    && state.snowballY > 0.dp && state.snowballY < state.surfaceHeightDp)

            //통과
            if(distance < (state.targetSize.value/2 + state.snowballSize.value/2) && mapIn && state.level < 99) {

                //점수
                val addScore = (1 - (distance / (state.targetSize.value/2 + state.snowballSize.value/2))) * 200

                reduce {
                    state.copy(
                        score = state.score + addScore.toInt(),
                        shotPower = 0,
                        situation = "다음"
                    )
                }

            } else {

                val level = state.level
                var plusValue = 0
                for (i in 0..level) {
                    plusValue += (i / 10) + 1
                }

                plusValue *= 5

                //애정도, cash 추가
                val updatePatData = state.patData
                updatePatData.love = state.patData.love + plusValue
                updatePatData.gameCount = state.patData.gameCount + 1
                patDao.update(updatePatData)

                if(state.patData.gameCount + 1 >= 100) {

                    //매달, medal, 칭호4
                    val currentMedals = userDao.getAllUserData().find { it.id == "etc" }?.value3 ?: ""
                    val (updatedMedal, acquired) = tryAcquireMedal(currentMedals, 4)
                    if (acquired) {
                        userDao.update(id = "etc", value3 = updatedMedal)
                        postSideEffect(FirstGameSideEffect.Toast("칭호를 획득했습니다!"))
                    }

                }

                userDao.update(
                    id = "money",
                    value2 = (state.userData.find { it.id == "money" }!!.value2.toInt() + plusValue).toString()
                )

                reduce {
                    state.copy(
                        plusValue = plusValue
                    )
                }

                if(state.userData.find { it.id == "firstGame" }!!.value.toDouble() < state.score){

                    userDao.update(id = "firstGame", value = state.score.toString(), value2 = state.level.toString())

                    reduce {
                        state.copy(
                            situation = "신기록"
                        )
                    }
                } else {
                    reduce {
                        state.copy(
                            situation = "종료"
                        )
                    }
                }

            }

        }

    }

    fun onNextLevelClick() = intent {
        val randomX = Random.nextInt(0, (state.surfaceWidthDp - state.targetSize).value.toInt())
        val randomY = Random.nextInt(0, (state.surfaceHeightDp - state.targetSize).value.toInt())

        reduce {
            state.copy(
                situation = "회전",
                level = state.level + 1,
                targetX = randomX.dp,
                targetY = randomY.dp,
                isRotating = true,
                rotationDuration = state.rotationDuration*0.9
            )
        }

        viewModelScope.launch {
            while (state.isRotating) { // isActive를 체크하여 안전하게 종료 가능
                reduce {
                    state.copy(rotationAngle = state.rotationAngle + 2f)
                }
                delay((10-state.level*2/10).toLong()) // 회전속도
            }
        }

    }

    fun onRotateStopClick() = intent {
        reduce {
            state.copy(
                isRotating = false,
                isShotSetting = true,
                situation = "준비"
            )
        }

        while (state.isShotSetting) { // isActive를 체크하여 안전하게 종료 가능

            if(state.shotPower < state.maxPower) {
                reduce {
                    state.copy(shotPower = state.shotPower + 20)
                }
            } else {
                reduce {
                    state.copy(shotPower = 0)
                }
            }

            delay((101 - state.level).toLong()) //
        }
    }

}



@Immutable
data class FirstGameState(
    val userData: List<User> = emptyList(),
    val snowballX: Dp = 0.dp,
    val snowballY: Dp = 0.dp,
    val surfaceWidthDp: Dp = 0.dp,
    val surfaceHeightDp: Dp = 0.dp,
    val maxPower: Int = 0,
    val rotationAngle: Float = 0f,
    val situation: String = "시작",
    val shotDuration: Int = 1000,
    val snowballSize: Dp = 0.dp,
    val targetSize: Dp = 0.dp,
    val targetX: Dp = 0.dp,
    val targetY: Dp = 0.dp,
    val score: Int = 0,
    val level: Int = 0,
    val isRotating: Boolean = false,
    val isShotSetting: Boolean = false,
    val rotationDuration: Double = 100.0,
    val shotPower: Int = 0,
    val patData: Pat = Pat(url = ""),
    val plusValue: Int = 0
)


//상태와 관련없는 것
sealed interface FirstGameSideEffect{
    class Toast(val message:String): FirstGameSideEffect
//    data object NavigateToDailyActivity: LoadingSideEffect

}