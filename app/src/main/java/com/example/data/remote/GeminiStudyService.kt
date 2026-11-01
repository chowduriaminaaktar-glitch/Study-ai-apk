package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.data.model.ExplanationStyle
import com.example.data.model.PracticeQuiz
import com.example.data.model.QuestionSolution
import com.example.data.model.QuizQuestion
import com.example.data.model.SolutionStep
import com.studyai.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiStudyService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Throwable) {
            ""
        }

    // Supported Gemini models with automatic resilience fallback
    private val candidateModels = listOf(
        "gemini-flash-lite-latest",
        "gemini-3.8-flash",
        "gemini-flash-latest"
    )

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getBaseUrl(model: String, streaming: Boolean = false): String {
        val action = if (streaming) "streamGenerateContent" else "generateContent"
        return "https://generativelanguage.googleapis.com/v1beta/models/$model:$action"
    }

    /**
     * Solves academic questions step-by-step with structured JSON output and image support.
     * Supports English, Bengali, and all subjects.
     */
    suspend fun solveQuestion(
        subject: String,
        question: String,
        style: ExplanationStyle,
        imageBitmap: Bitmap? = null,
        mediaData: Pair<String, ByteArray>? = null
    ): Result<QuestionSolution> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add GEMINI_API_KEY to Google AI Studio Secrets.")
            )
        }

        val systemPrompt = """
            You are Study AI, an expert, encouraging academic tutor specializing in $subject.
            Explanation Style: ${style.promptDescription}.
            Language Requirement: Respond in the language of the student's question (e.g. if the question is in Bengali, explain in Bengali; if in English, explain in English).
            
            Analyze the student's problem, diagram, or formula meticulously.
            Return a valid JSON object with the following schema:
            {
              "directAnswer": "Concise, definitive final answer or main thesis",
              "steps": [
                {
                  "stepNumber": 1,
                  "title": "Title of step",
                  "explanation": "Clear explanation of the step",
                  "formulaOrDetail": "Optional formula, calculation, or code (or empty string)"
                }
              ],
              "coreConcepts": ["Concept 1", "Concept 2", "Concept 3"],
              "tipsAndCommonMistakes": ["Tip or common trap 1", "Tip 2"],
              "followUpPractice": "A stimulating follow-up question or variant for the student to practice"
            }
        """.trimIndent()

        val partsArray = JSONArray()
        partsArray.put(JSONObject().apply {
            put("text", "Subject: $subject\nQuestion: $question\nExplanation Style: ${style.label}")
        })

        if (imageBitmap != null) {
            val base64Img = compressBitmapToBase64(imageBitmap)
            if (base64Img.isNotBlank()) {
                partsArray.put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64Img)
                    })
                })
            }
        } else if (mediaData != null) {
            val base64Data = Base64.encodeToString(mediaData.second, Base64.NO_WRAP)
            partsArray.put(JSONObject().apply {
                put("inlineData", JSONObject().apply {
                    put("mimeType", mediaData.first)
                    put("data", base64Data)
                })
            })
        }

        val contentsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("parts", partsArray)
            })
        }

        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
            })
        }

        var lastError: Exception? = null

        for (model in candidateModels) {
            try {
                val url = "${getBaseUrl(model)}?key=$key"
                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    Log.w("GeminiStudyService", "Model $model returned error ${response.code}")
                    lastError = Exception("API error (${response.code}) from Gemini: $responseBody")
                    continue
                }

                val text = extractTextFromResponse(responseBody)
                if (text.isBlank()) {
                    lastError = Exception("Model $model returned an empty text response")
                    continue
                }

                val parsedJson = JSONObject(text)
                val directAnswer = parsedJson.optString("directAnswer", "Solution completed.")
                val stepsJsonArray = parsedJson.optJSONArray("steps") ?: JSONArray()
                val steps = mutableListOf<SolutionStep>()

                for (i in 0 until stepsJsonArray.length()) {
                    val stepObj = stepsJsonArray.optJSONObject(i) ?: continue
                    steps.add(
                        SolutionStep(
                            stepNumber = stepObj.optInt("stepNumber", i + 1),
                            title = stepObj.optString("title", "Step ${i + 1}"),
                            explanation = stepObj.optString("explanation", ""),
                            formulaOrDetail = if (stepObj.has("formulaOrDetail")) stepObj.optString("formulaOrDetail") else null
                        )
                    )
                }

                val coreConcepts = mutableListOf<String>()
                val coreConceptsArray = parsedJson.optJSONArray("coreConcepts")
                if (coreConceptsArray != null) {
                    for (i in 0 until coreConceptsArray.length()) {
                        coreConcepts.add(coreConceptsArray.optString(i))
                    }
                }
                if (coreConcepts.isEmpty()) coreConcepts.add(subject)

                val tips = mutableListOf<String>()
                val tipsArray = parsedJson.optJSONArray("tipsAndCommonMistakes")
                if (tipsArray != null) {
                    for (i in 0 until tipsArray.length()) {
                        tips.add(tipsArray.optString(i))
                    }
                }
                if (tips.isEmpty()) tips.add("Double-check units and edge conditions.")

                val followUp = parsedJson.optString("followUpPractice", "Try changing constants to observe the solution behavior.")

                return@withContext Result.success(
                    QuestionSolution(
                        subject = subject,
                        question = question,
                        directAnswer = directAnswer,
                        steps = if (steps.isNotEmpty()) steps else listOf(
                            SolutionStep(1, "Complete Solution", directAnswer, null)
                        ),
                        coreConcepts = coreConcepts,
                        tipsAndCommonMistakes = tips,
                        followUpPractice = followUp
                    )
                )
            } catch (e: Exception) {
                Log.e("GeminiStudyService", "Failed with model $model: ${e.message}", e)
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("Failed to solve question using Gemini API."))
    }

    /**
     * Generates high-yield academic multiple-choice quizzes with explanations in structured JSON.
     */
    suspend fun generateQuiz(
        subject: String,
        topic: String,
        difficulty: String,
        count: Int = 5
    ): Result<PracticeQuiz> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured.")
            )
        }

        val systemPrompt = """
            You are Study AI Quiz Master. Generate a high-yield academic multiple-choice quiz for subject '$subject', topic '$topic' at difficulty level '$difficulty'.
            Respond in valid JSON with an array named 'questions'.
            Each item in 'questions' must have:
            - id: integer (1 to $count)
            - questionText: string
            - options: array of exactly 4 strings
            - correctIndex: integer 0-3 (index of the correct answer in options)
            - explanation: clear explanation of why the correct option is true and others are false
            - conceptTested: short string naming the specific concept
        """.trimIndent()

        val contentsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", "Generate $count practice questions on $topic.") })
                })
            })
        }

        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3)
            })
        }

        var lastError: Exception? = null

        for (model in candidateModels) {
            try {
                val url = "${getBaseUrl(model)}?key=$key"
                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    lastError = Exception("API error (${response.code}) from Gemini: $responseBody")
                    continue
                }

                val text = extractTextFromResponse(responseBody)
                if (text.isBlank()) continue

                val parsedJson = JSONObject(text)
                val qArray = parsedJson.optJSONArray("questions") ?: JSONArray()
                val questions = mutableListOf<QuizQuestion>()

                for (i in 0 until qArray.length()) {
                    val obj = qArray.optJSONObject(i) ?: continue
                    val optionsArray = obj.optJSONArray("options")
                    val options = mutableListOf<String>()
                    if (optionsArray != null) {
                        for (j in 0 until optionsArray.length()) {
                            options.add(optionsArray.optString(j))
                        }
                    }
                    val safeOptions = if (options.size >= 4) options.take(4) else listOf("Option A", "Option B", "Option C", "Option D")

                    questions.add(
                        QuizQuestion(
                            id = obj.optInt("id", i + 1),
                            questionText = obj.optString("questionText", "Question ${i + 1}"),
                            options = safeOptions,
                            correctIndex = obj.optInt("correctIndex", 0).coerceIn(0, 3),
                            explanation = obj.optString("explanation", ""),
                            conceptTested = obj.optString("conceptTested", topic)
                        )
                    )
                }

                if (questions.isNotEmpty()) {
                    return@withContext Result.success(
                        PracticeQuiz(
                            title = "$topic Practice Quiz",
                            subject = subject,
                            topic = topic,
                            difficulty = difficulty,
                            questions = questions
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("GeminiStudyService", "Quiz gen failed on $model: ${e.message}", e)
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("Failed to generate quiz with Gemini."))
    }

    /**
     * Real-time streaming chat message generation with contextual history and multimodal images.
     * Emits tokens chunk-by-chunk via [onChunk] callback and returns the full generated text.
     */
    suspend fun sendChatMessageStream(
        conversation: List<Pair<String, String>>,
        images: List<Bitmap> = emptyList(),
        attachmentSummary: String? = null,
        systemInstruction: String = DEFAULT_CHAT_SYSTEM_INSTRUCTION,
        onChunk: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add GEMINI_API_KEY in Google AI Studio Secrets.")
            )
        }

        val contentsArray = buildNormalizedContents(conversation, images, attachmentSummary)
        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.6)
            })
        }

        var lastError: Exception? = null

        for (model in candidateModels) {
            try {
                val url = "${getBaseUrl(model, streaming = true)}?key=$key&alt=sse"
                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    val err = response.body?.string().orEmpty()
                    Log.w("GeminiStudyService", "Streaming failed for $model (${response.code}): $err")
                    lastError = Exception("API error (${response.code}): $err")
                    continue
                }

                val body = response.body
                if (body == null) {
                    lastError = Exception("Empty response body from $model")
                    continue
                }

                val accumulated = StringBuilder()
                val reader = BufferedReader(InputStreamReader(body.byteStream()))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: break
                    if (currentLine.startsWith("data: ")) {
                        val dataJson = currentLine.removePrefix("data: ").trim()
                        if (dataJson.isNotEmpty() && dataJson != "[DONE]") {
                            try {
                                val obj = JSONObject(dataJson)
                                val candidates = obj.optJSONArray("candidates")
                                val candidate0 = candidates?.optJSONObject(0)
                                val content = candidate0?.optJSONObject("content")
                                val parts = content?.optJSONArray("parts")
                                val chunkText = parts?.optJSONObject(0)?.optString("text").orEmpty()
                                if (chunkText.isNotEmpty()) {
                                    accumulated.append(chunkText)
                                    onChunk(chunkText)
                                }
                            } catch (e: Exception) {
                                // Skip individual malformed chunk
                            }
                        }
                    }
                }

                val finalText = accumulated.toString().trim()
                if (finalText.isNotEmpty()) {
                    return@withContext Result.success(finalText)
                } else {
                    lastError = Exception("Model $model generated an empty stream")
                }
            } catch (e: Exception) {
                Log.e("GeminiStudyService", "Stream error on $model: ${e.message}", e)
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("Failed to generate chat response."))
    }

    /**
     * Non-streaming chat message generation fallback.
     */
    suspend fun sendChatMessage(
        conversation: List<Pair<String, String>>,
        imageBitmap: Bitmap? = null,
        mediaData: Pair<String, ByteArray>? = null,
        attachmentSummary: String? = null,
        systemInstruction: String = DEFAULT_CHAT_SYSTEM_INSTRUCTION
    ): Result<String> {
        val images = if (imageBitmap != null) listOf(imageBitmap) else emptyList()
        val accumulated = StringBuilder()
        val streamResult = sendChatMessageStream(
            conversation = conversation,
            images = images,
            attachmentSummary = attachmentSummary,
            systemInstruction = systemInstruction,
            onChunk = { accumulated.append(it) }
        )
        return if (streamResult.isSuccess) {
            Result.success(streamResult.getOrThrow())
        } else {
            streamResult
        }
    }

    /**
     * Answers contextual follow-up questions from the solver screen.
     */
    suspend fun answerFollowUp(
        subject: String,
        originalQuestion: String,
        solutionSummary: String,
        followUpQuery: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        val prompt = """
            Context:
            Subject: $subject
            Original Problem: $originalQuestion
            Previous Solution Summary: $solutionSummary
            
            Student Follow-Up Question:
            $followUpQuery
            
            Instruction: Answer clearly, helpfully, and educationally. Support Bengali, English, and other languages naturally if the query is in that language.
        """.trimIndent()

        val conversation = listOf(
            Pair("user", "Here is my problem: $originalQuestion. Solution: $solutionSummary"),
            Pair("assistant", "I understand. What clarification do you need?"),
            Pair("user", followUpQuery)
        )

        val result = sendChatMessage(
            conversation = conversation,
            systemInstruction = "You are Study AI tutor answering student clarifications."
        )
        result
    }

    /**
     * Specialized real-time conversational voice tutor for Study AI Live.
     */
    suspend fun speakLiveTurn(
        userVoiceInput: String,
        subjectContext: String,
        persona: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        val systemInstruction = """
            You are Study AI Live, a real-time conversational voice tutor inspired by Gemini Live.
            Current Subject: $subjectContext
            Persona: $persona
            Language: Fluent in English, Bengali (বাংলা), and multilingual based on the student's input.
            Voice Guidelines:
            1. You are speaking directly to the student over a voice stream.
            2. Keep explanations conversational, energetic, and concise (2-4 sentences max per turn).
            3. Do not use complex ASCII tables, asterisks, or unpronounceable math syntax.
            4. Be encouraging and break down concepts step-by-step.
        """.trimIndent()

        val fullConv = conversationHistory + listOf(Pair("user", userVoiceInput))
        sendChatMessage(
            conversation = fullConv,
            systemInstruction = systemInstruction
        )
    }

    /**
     * Normalizes conversation turns to ensure valid alternating "user" / "model" roles
     * required by the Gemini API specifications.
     */
    private fun buildNormalizedContents(
        conversation: List<Pair<String, String>>,
        images: List<Bitmap>,
        attachmentSummary: String?
    ): JSONArray {
        val contentsArray = JSONArray()
        val recentConv = conversation.takeLast(12)
        if (recentConv.isEmpty()) {
            val userParts = JSONArray().apply {
                put(JSONObject().apply { put("text", "Hello") })
            }
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", userParts)
            })
            return contentsArray
        }

        // Merge consecutive roles and ensure alternation
        val normalizedTurns = mutableListOf<Pair<String, String>>()
        for (turn in recentConv) {
            val role = if (turn.first.equals("model", ignoreCase = true) || turn.first.equals("assistant", ignoreCase = true)) {
                "model"
            } else {
                "user"
            }
            val text = turn.second.trim()
            if (text.isEmpty() && normalizedTurns.isEmpty()) continue

            if (normalizedTurns.isNotEmpty() && normalizedTurns.last().first == role) {
                val previous = normalizedTurns.removeAt(normalizedTurns.lastIndex)
                normalizedTurns.add(Pair(role, "${previous.second}\n\n$text"))
            } else {
                normalizedTurns.add(Pair(role, text))
            }
        }

        // Gemini requires the first turn to have role 'user'
        if (normalizedTurns.isNotEmpty() && normalizedTurns.first().first != "user") {
            normalizedTurns.removeAt(0)
        }

        if (normalizedTurns.isEmpty()) {
            normalizedTurns.add(Pair("user", "Hello"))
        }

        val lastIndex = normalizedTurns.lastIndex
        normalizedTurns.forEachIndexed { index, (role, text) ->
            val isLastTurn = index == lastIndex && role == "user"
            val turnParts = JSONArray()

            val augmentedText = if (isLastTurn && !attachmentSummary.isNullOrBlank()) {
                if (text.isNotBlank()) "$text\n\n[Attached: $attachmentSummary]" else "Please analyze the attached items: $attachmentSummary"
            } else {
                text.ifBlank { "..." }
            }

            turnParts.put(JSONObject().apply { put("text", augmentedText) })

            if (isLastTurn && images.isNotEmpty()) {
                for (bitmap in images) {
                    val base64Data = compressBitmapToBase64(bitmap)
                    if (base64Data.isNotBlank()) {
                        turnParts.put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Data)
                            })
                        })
                    }
                }
            }

            contentsArray.put(JSONObject().apply {
                put("role", role)
                put("parts", turnParts)
            })
        }

        return contentsArray
    }

    private fun compressBitmapToBase64(bitmap: Bitmap): String {
        return try {
            val stream = ByteArrayOutputStream()
            // Resize if bitmap is huge to stay within recommended bounds
            val maxDimension = 1536
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val targetW = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                val targetH = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("GeminiStudyService", "Failed to compress bitmap: ${e.message}")
            ""
        }
    }

    private fun extractTextFromResponse(responseBody: String): String {
        return try {
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return ""
            val candidate0 = candidates.optJSONObject(0) ?: return ""
            val content = candidate0.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            parts.optJSONObject(0)?.optString("text").orEmpty()
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        const val DEFAULT_CHAT_SYSTEM_INSTRUCTION =
            "You are Study AI, an intelligent, versatile academic assistant and ChatGPT-style tutor. " +
            "You provide comprehensive, step-by-step explanations for all subjects, debug code, analyze equations, " +
            "and thoroughly inspect attached images, diagrams, and photos. " +
            "Language versatility: You natively support Bengali (বাংলা), English, and multilingual conversations. " +
            "If a user asks in Bengali or any other language, answer in that same language. " +
            "Format your answers with markdown headings, structured bullet points, clear formulas, and syntax-highlighted code blocks."
    }
}
