package com.a0100019.mypat.presentation.neighbor

import android.util.Log
import androidx.lifecycle.ViewModel
import com.a0100019.mypat.data.room.user.User
import com.a0100019.mypat.data.room.user.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class NeighborViewModel @Inject constructor(
    private val userDao: UserDao,
) : ViewModel(), ContainerHost<NeighborState, NeighborSideEffect> {

    override val container: Container<NeighborState, NeighborSideEffect> = container(
        initialState = NeighborState(),
    )

    init {
        generateAiGreeting()
    }

    private fun generateAiGreeting() = intent {
        reduce { state.copy(aiText = "AI 촌장님이 오는 중...") }

        val result = withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                // 유빈님이 넣으신 OpenAI 키
                val json = JSONObject().apply {
                    put("model", "gpt-3.5-turbo")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "너는 하루마을 촌장이야. 주민들에게 기분 좋은 짧은 인사 한 문장 해줘.")
                        })
                    })
                }

                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
//                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()

                    if (!response.isSuccessful) {
                        // 실패 시 로그 (401: 키 문제, 429: 한도 초과 등)
                        Log.e("OpenAI", "통신 실패 코드: ${response.code}")
                        Log.e("OpenAI", "에러 내용: $responseBody")
                        return@withContext "반가워요! 오늘도 평화로운 하루마을입니다."
                    }

                    Log.d("OpenAI", "통신 성공: $responseBody")

                    val jsonResponse = JSONObject(responseBody ?: "")
                    val aiMessage = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    Log.d("OpenAI", "추출된 메시지: $aiMessage")
                    aiMessage
                }
            } catch (e: Exception) {
                Log.e("OpenAI", "예외 발생: ${e.message}")
                e.printStackTrace()
                "오늘도 평화로운 하루마을입니다."
            }
        }

        reduce { state.copy(aiText = result.trim()) }
    }
}

@Immutable
data class NeighborState(
    val userData: List<User> = emptyList(),
    val aiText: String = "반가워요!"
)

sealed interface NeighborSideEffect {
    class Toast(val message: String) : NeighborSideEffect
}