package com.a0100019.mypat.presentation.activity.daily.walk

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import com.a0100019.mypat.data.room.walk.Walk
import com.a0100019.mypat.data.room.walk.WalkDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class WalkViewModel @Inject constructor(
    private val userDao: UserDao,
    private val walkDao: WalkDao,
    @ApplicationContext private val context: Context
) : ViewModel(), ContainerHost<WalkState, WalkSideEffect> {

    companion object {
        private const val PREFS_NAME = "step_prefs"
        private const val KEY_SAVE_STEPS = "saveSteps"
        private const val KEY_STEPS_RAW = "stepsRaw"
        private const val STEP_GOAL = 5000
        private const val STEP_REWARD = 1
        private const val DATE_FORMAT = "yyyy-MM-dd"
        private const val MONTH_FORMAT = "yyyy-MM"
    }

    private val dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
    private val monthFormatter = DateTimeFormatter.ofPattern(MONTH_FORMAT)

    override val container: Container<WalkState, WalkSideEffect> = container(
        initialState = WalkState(),
        buildSettings = {
            this.exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                intent {
                    Log.e("WalkViewModel", "Coroutine exception: ${throwable.message}")
                    postSideEffect(WalkSideEffect.Toast(message = throwable.message.orEmpty()))
                }
            }
        }
    )

    init {
        loadData()
        startStepUpdater()
    }

    private fun loadData() = intent {
        Log.d("WalkViewModel", "loadData 호출")
        val userDataList = userDao.getAllUserData()
        val today = LocalDate.now().format(dateFormatter)
        val (saveSteps, stepsRaw) = readStepPrefs(today)

        userDao.update(id = "etc2", value2 = stepsRaw)

        reduce {
            state.copy(
                userDataList = userDataList,
                stepsRaw = stepsRaw,
                saveSteps = saveSteps,
                today = today,
                calendarMonth = today.substring(0, 7),
                baseDate = today
            )
        }
    }

    private var stepUpdateJob: Job? = null

    private fun startStepUpdater() {
        if (stepUpdateJob != null) return // 중복 방지

        stepUpdateJob = viewModelScope.launch {
            while (isActive) {
                loadData1()
                delay(1000L) // ⏱ 1초
            }
        }
    }

    private fun loadData1() = intent {
        Log.d("WalkViewModel", "loadData1 호출")
        val today = LocalDate.now().format(dateFormatter)
        val (saveSteps, stepsRaw) = readStepPrefs(today)

        userDao.update(id = "etc2", value2 = stepsRaw)

        reduce {
            state.copy(
                stepsRaw = stepsRaw,
                saveSteps = saveSteps,
            )
        }
    }

    // SharedPreferences에서 걸음수 관련 데이터를 읽어 반환
    private fun readStepPrefs(today: String): Pair<Int, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saveSteps = prefs.getInt(KEY_SAVE_STEPS, 0)
        val stepsRaw = prefs.getString(KEY_STEPS_RAW, "$today.1") ?: "$today.1"
        return saveSteps to stepsRaw
    }

    override fun onCleared() {
        stepUpdateJob?.cancel()
        stepUpdateJob = null
        super.onCleared()
    }

    fun onTodayWalkSubmitClick() = intent {

        if (state.saveSteps >= STEP_GOAL) {
            val currentMoney = state.userDataList.find { it.id == "money" }!!.value.toInt()
            userDao.update(
                id = "money",
                value = (currentMoney + STEP_REWARD).toString()
            )

            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_SAVE_STEPS, 0)
                .apply()

            val userDataList = userDao.getAllUserData()

            reduce { state.copy(saveSteps = 0, userDataList = userDataList) }
            postSideEffect(WalkSideEffect.Toast("햇살 +$STEP_REWARD"))

        } else {
            postSideEffect(WalkSideEffect.Toast("걸음 수가 부족합니다"))
        }

    }

    fun onSituationChangeClick(situation: String) = intent {
        when (situation) {
            "month", "week", "record" -> reduce { state.copy(situation = situation) }
        }
    }

    fun onCalendarMonthChangeClick(direction: String) = intent {

        when (state.situation) {
            "month" -> {
                val yearMonth = YearMonth.parse(state.calendarMonth, monthFormatter)
                val newMonth = when (direction) {
                    "left"  -> yearMonth.minusMonths(1).format(monthFormatter)
                    "right" -> yearMonth.plusMonths(1).format(monthFormatter)
                    "today" -> state.today.substring(0, 7)
                    else    -> state.calendarMonth
                }
                reduce { state.copy(calendarMonth = newMonth) }
            }
            "week" -> {
                val oldDate = LocalDate.parse(state.baseDate, dateFormatter)
                val newDate = when (direction) {
                    "left"  -> oldDate.minusDays(7)
                    "right" -> oldDate.plusDays(7)
                    "today" -> LocalDate.parse(state.today)
                    else    -> oldDate
                }
                reduce { state.copy(baseDate = newDate.format(dateFormatter)) }
            }
        }

    }

}

@Immutable
data class WalkState(
    val userDataList: List<User> = emptyList(),

    val saveSteps: Int = 0, // ✅ 걸음 수 저장 (초기값 0)
    val stepsRaw: String = "",
    val today: String = "2025-07-05",
    val calendarMonth: String = "2025-07",
    val baseDate: String = "2025-11-26",
    val situation: String = "record"

    )

sealed interface WalkSideEffect {
    class Toast(val message: String) : WalkSideEffect
}
