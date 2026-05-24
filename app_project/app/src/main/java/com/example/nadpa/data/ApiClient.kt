package com.example.nadpa.data

import com.example.nadpa.data.Task
import com.example.nadpa.data.TaskCreate
import com.example.nadpa.data.Goal
import com.example.nadpa.data.GoalCreate
import com.example.nadpa.data.GoalUpdate
import com.example.nadpa.data.Inspiration
import com.example.nadpa.data.ChatRequest
import com.example.nadpa.data.ChatResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ─── Base URL ────────────────────────────────────────────────────────────────
// 10.0.2.2 maps to localhost from Android Emulator
// Replace with your actual server URL when deploying
private const val BASE_URL = "http://10.0.2.2:8000/"

// ─── Retrofit Interface ───────────────────────────────────────────────────────
interface NadPaApiService {

    // Tasks
    @GET("tasks/")
    suspend fun getTasks(@Query("relativeWeekIndex") relativeWeekIndex: Int? = null): List<Task>

    @POST("tasks/")
    suspend fun createTask(@Body task: TaskCreate): Task

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String)

    // Goals
    @GET("goals/")
    suspend fun getGoals(
        @Query("status") status: String? = null,
        @Query("type") type: String? = null
    ): List<Goal>

    @POST("goals/")
    suspend fun createGoal(@Body goal: GoalCreate): Goal

    @PATCH("goals/{id}")
    suspend fun updateGoal(@Path("id") id: String, @Body update: GoalUpdate): Goal

    // Daily Inspiration
    @GET("inspiration/today")
    suspend fun getTodayInspiration(): Inspiration

    // AI Chat
    @POST("ai/chat")
    suspend fun sendChatMessage(@Body request: ChatRequest): ChatResponse
}

// ─── Singleton Retrofit Client ────────────────────────────────────────────────
object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)  // AI responses may take longer
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: NadPaApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NadPaApiService::class.java)
}
