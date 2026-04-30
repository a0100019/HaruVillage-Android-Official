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

    companion object {
        private const val OPENAI_URL = "https://api.openai.com/v1/chat/completions"
        private const val OPENAI_MODEL = "gpt-3.5-turbo"
        private const val PROMPT = "너는 하루마을 촌장이야. 주민들에게 기분 좋은 짧은 인사 한 문장 해줘."
        private const val MEDIA_TYPE = "application/json; charset=utf-8"
        private const val FALLBACK_MESSAGE = "반가워요! 오늘도 평화로운 하루마을입니다."
        private const val FALLBACK_ERROR = "오늘도 평화로운 하루마을입니다."
        private const val LOADING_MESSAGE = "AI 촌장님이 오는 중..."
    }

    private val httpClient = OkHttpClient()

    override val container: Container<NeighborState, NeighborSideEffect> = container(
        initialState = NeighborState(),
    )

    init {
        generateAiGreeting()
    }

    private fun generateAiGreeting() = intent {
        reduce { state.copy(aiText = LOADING_MESSAGE) }

        val result = withContext(Dispatchers.IO) {
            try {
                val body = buildRequestBody()
                val request = Request.Builder()
                    .url(OPENAI_URL)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()

                    if (!response.isSuccessful) {
                        Log.e("OpenAI", "통신 실패 코드: ${response.code}")
                        Log.e("OpenAI", "에러 내용: $responseBody")
                        return@withContext FALLBACK_MESSAGE
                    }

                    Log.d("OpenAI", "통신 성공: $responseBody")
                    val aiMessage = parseAiMessage(responseBody)
                    Log.d("OpenAI", "추출된 메시지: $aiMessage")
                    aiMessage
                }
            } catch (e: Exception) {
                Log.e("OpenAI", "예외 발생: ${e.message}")
                e.printStackTrace()
                FALLBACK_ERROR
            }
        }

        reduce { state.copy(aiText = result.trim()) }
    }

    private fun buildRequestBody() = JSONObject().apply {
        put("model", OPENAI_MODEL)
        put("messages", JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", PROMPT)
            })
        })
    }.toString().toRequestBody(MEDIA_TYPE.toMediaType())

    private fun parseAiMessage(responseBody: String?): String =
        JSONObject(responseBody ?: "")
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

}

@Immutable
data class NeighborState(
    val userData: List<User> = emptyList(),
    val aiText: String = "반가워요!"
)

sealed interface NeighborSideEffect {
    class Toast(val message: String) : NeighborSideEffect
}