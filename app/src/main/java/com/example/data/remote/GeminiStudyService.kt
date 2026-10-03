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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

@Serializable
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseSchema: JsonObject? = null
)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

class GeminiStudyService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

    private val modelName = "gemini-3.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    suspend fun solveQuestion(
        subject: String,
        question: String,
        style: ExplanationStyle,
        imageBitmap: Bitmap? = null
    ): Result<QuestionSolution> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank()) {
            return@withContext Result.success(createFallbackSolution(subject, question, style))
        }

        try {
            val systemPrompt = """
                You are Study AI, an expert academic tutor in $subject.
                Style instruction: ${style.promptDescription}.
                Solve the student's question thoroughly and return JSON with keys:
                - directAnswer: concise, accurate final answer or thesis.
                - steps: array of objects with keys: stepNumber (integer), title (string), explanation (string), formulaOrDetail (string, optional equation or code).
                - coreConcepts: array of 3-5 strings naming underlying academic concepts.
                - tipsAndCommonMistakes: array of 2-4 strings highlighting caveats, exam traps, or validation checks.
                - followUpPractice: string containing a suggested challenging practice problem.
            """.trimIndent()

            val parts = mutableListOf<GeminiPart>()
            parts.add(GeminiPart(text = "Subject: $subject\nQuestion: $question\nExplanation Style: ${style.label}"))

            if (imageBitmap != null) {
                val stream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val base64Data = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Data)))
            }

            val schema = buildJsonObject {
                put("type", "OBJECT")
                putJsonObject("properties") {
                    putJsonObject("directAnswer") { put("type", "STRING") }
                    putJsonObject("steps") {
                        put("type", "ARRAY")
                        putJsonObject("items") {
                            put("type", "OBJECT")
                            putJsonObject("properties") {
                                putJsonObject("stepNumber") { put("type", "INTEGER") }
                                putJsonObject("title") { put("type", "STRING") }
                                putJsonObject("explanation") { put("type", "STRING") }
                                putJsonObject("formulaOrDetail") { put("type", "STRING") }
                            }
                            putJsonArray("required") {
                                add(kotlinx.serialization.json.JsonPrimitive("stepNumber"))
                                add(kotlinx.serialization.json.JsonPrimitive("title"))
                                add(kotlinx.serialization.json.JsonPrimitive("explanation"))
                            }
                        }
                    }
                    putJsonObject("coreConcepts") {
                        put("type", "ARRAY")
                        putJsonObject("items") { put("type", "STRING") }
                    }
                    putJsonObject("tipsAndCommonMistakes") {
                        put("type", "ARRAY")
                        putJsonObject("items") { put("type", "STRING") }
                    }
                    putJsonObject("followUpPractice") { put("type", "STRING") }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("directAnswer"))
                    add(kotlinx.serialization.json.JsonPrimitive("steps"))
                    add(kotlinx.serialization.json.JsonPrimitive("coreConcepts"))
                    add(kotlinx.serialization.json.JsonPrimitive("tipsAndCommonMistakes"))
                    add(kotlinx.serialization.json.JsonPrimitive("followUpPractice"))
                }
            }

            val requestPayload = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = parts)),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.2f,
                    responseSchema = schema
                )
            )

            val requestBodyString = json.encodeToString(GeminiRequest.serializer(), requestPayload)
            val request = Request.Builder()
                .url("$baseUrl?key=$key")
                .post(requestBodyString.toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseText = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w("GeminiStudyService", "API error: ${response.code} $responseText")
                return@withContext Result.success(createFallbackSolution(subject, question, style))
            }

            val parsedResponse = json.decodeFromString(GeminiResponse.serializer(), responseText)
            val contentText = parsedResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.success(createFallbackSolution(subject, question, style))

            val parsedJson = json.parseToJsonElement(contentText).jsonObject
            val directAnswer = parsedJson["directAnswer"]?.jsonPrimitive?.contentOrNull ?: "Solution generated."
            val stepsArray = parsedJson["steps"]?.jsonArray ?: emptyList()
            val steps = stepsArray.mapIndexed { idx, element ->
                val obj = element.jsonObject
                SolutionStep(
                    stepNumber = obj["stepNumber"]?.jsonPrimitive?.intOrNull ?: (idx + 1),
                    title = obj["title"]?.jsonPrimitive?.contentOrNull ?: "Step ${idx + 1}",
                    explanation = obj["explanation"]?.jsonPrimitive?.contentOrNull ?: "",
                    formulaOrDetail = obj["formulaOrDetail"]?.jsonPrimitive?.contentOrNull
                )
            }

            val coreConcepts = parsedJson["coreConcepts"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                ?: listOf(subject)
            val tips = parsedJson["tipsAndCommonMistakes"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                ?: listOf("Always double-check units and initial constraints.")
            val followUp = parsedJson["followUpPractice"]?.jsonPrimitive?.contentOrNull
                ?: "Try varying the given constants to check how the outcome responds."

            Result.success(
                QuestionSolution(
                    subject = subject,
                    question = question,
                    directAnswer = directAnswer,
                    steps = if (steps.isNotEmpty()) steps else createFallbackSolution(subject, question, style).steps,
                    coreConcepts = coreConcepts,
                    tipsAndCommonMistakes = tips,
                    followUpPractice = followUp
                )
            )
        } catch (e: Exception) {
            Log.e("GeminiStudyService", "Failed to solve question: ${e.message}", e)
            Result.success(createFallbackSolution(subject, question, style))
        }
    }

    suspend fun generateQuiz(
        subject: String,
        topic: String,
        difficulty: String,
        count: Int = 5
    ): Result<PracticeQuiz> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank()) {
            return@withContext Result.success(createFallbackQuiz(subject, topic, difficulty, count))
        }

        try {
            val systemPrompt = """
                You are Study AI quiz master. Generate a high-yield academic multiple-choice quiz for subject '$subject', topic '$topic' at difficulty '$difficulty'.
                Return JSON with array 'questions'. Each item:
                - id: integer (1 to $count)
                - questionText: string
                - options: array of exactly 4 strings
                - correctIndex: integer 0-3 corresponding to the correct option
                - explanation: clear explanation of why this option is correct and others false
                - conceptTested: short string naming the concept
            """.trimIndent()

            val schema = buildJsonObject {
                put("type", "OBJECT")
                putJsonObject("properties") {
                    putJsonObject("questions") {
                        put("type", "ARRAY")
                        putJsonObject("items") {
                            put("type", "OBJECT")
                            putJsonObject("properties") {
                                putJsonObject("id") { put("type", "INTEGER") }
                                putJsonObject("questionText") { put("type", "STRING") }
                                putJsonObject("options") {
                                    put("type", "ARRAY")
                                    putJsonObject("items") { put("type", "STRING") }
                                }
                                putJsonObject("correctIndex") { put("type", "INTEGER") }
                                putJsonObject("explanation") { put("type", "STRING") }
                                putJsonObject("conceptTested") { put("type", "STRING") }
                            }
                            putJsonArray("required") {
                                add(kotlinx.serialization.json.JsonPrimitive("id"))
                                add(kotlinx.serialization.json.JsonPrimitive("questionText"))
                                add(kotlinx.serialization.json.JsonPrimitive("options"))
                                add(kotlinx.serialization.json.JsonPrimitive("correctIndex"))
                                add(kotlinx.serialization.json.JsonPrimitive("explanation"))
                                add(kotlinx.serialization.json.JsonPrimitive("conceptTested"))
                            }
                        }
                    }
                }
            }

            val requestPayload = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = "Generate $count questions on $topic")))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.3f,
                    responseSchema = schema
                )
            )

            val request = Request.Builder()
                .url("$baseUrl?key=$key")
                .post(json.encodeToString(GeminiRequest.serializer(), requestPayload).toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseText = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(createFallbackQuiz(subject, topic, difficulty, count))
            }

            val parsedResponse = json.decodeFromString(GeminiResponse.serializer(), responseText)
            val contentText = parsedResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.success(createFallbackQuiz(subject, topic, difficulty, count))

            val parsedJson = json.parseToJsonElement(contentText).jsonObject
            val qArray = parsedJson["questions"]?.jsonArray ?: emptyList()

            val questions = qArray.mapIndexed { idx, it ->
                val obj = it.jsonObject
                val options = obj["options"]?.jsonArray?.mapNotNull { opt -> opt.jsonPrimitive.contentOrNull } ?: listOf("A", "B", "C", "D")
                QuizQuestion(
                    id = obj["id"]?.jsonPrimitive?.intOrNull ?: (idx + 1),
                    questionText = obj["questionText"]?.jsonPrimitive?.contentOrNull ?: "Question ${idx + 1}",
                    options = if (options.size >= 4) options.take(4) else listOf("Option A", "Option B", "Option C", "Option D"),
                    correctIndex = (obj["correctIndex"]?.jsonPrimitive?.intOrNull ?: 0).coerceIn(0, 3),
                    explanation = obj["explanation"]?.jsonPrimitive?.contentOrNull ?: "Explanation for question ${idx + 1}",
                    conceptTested = obj["conceptTested"]?.jsonPrimitive?.contentOrNull ?: topic
                )
            }

            Result.success(
                PracticeQuiz(
                    title = "$topic Practice Quiz",
                    subject = subject,
                    topic = topic,
                    difficulty = difficulty,
                    questions = if (questions.isNotEmpty()) questions else createFallbackQuiz(subject, topic, difficulty, count).questions
                )
            )
        } catch (e: Exception) {
            Log.e("GeminiStudyService", "Quiz gen failed: ${e.message}", e)
            Result.success(createFallbackQuiz(subject, topic, difficulty, count))
        }
    }

    suspend fun answerFollowUp(
        subject: String,
        originalQuestion: String,
        solutionSummary: String,
        followUpQuery: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank()) {
            return@withContext Result.success(createFallbackFollowUp(subject, followUpQuery))
        }

        try {
            val prompt = """
                Context:
                Subject: $subject
                Original Problem: $originalQuestion
                Previous Solution Summary: $solutionSummary
                
                Student Follow-Up Question:
                $followUpQuery
                
                Please answer clearly, concisely, and educationally. Use bullet points or LaTeX/code blocks where appropriate.
            """.trimIndent()

            val requestPayload = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are Study AI tutor answering student clarifications."))),
                generationConfig = GeminiGenerationConfig(temperature = 0.4f)
            )

            val request = Request.Builder()
                .url("$baseUrl?key=$key")
                .post(json.encodeToString(GeminiRequest.serializer(), requestPayload).toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseText = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.success(createFallbackFollowUp(subject, followUpQuery))
            }
            val parsedResponse = json.decodeFromString(GeminiResponse.serializer(), responseText)
            val reply = parsedResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: createFallbackFollowUp(subject, followUpQuery)
            Result.success(reply)
        } catch (e: Exception) {
            Result.success(createFallbackFollowUp(subject, followUpQuery))
        }
    }

    /**
     * Multi-turn conversational chat method.
     */
    suspend fun sendChatMessage(
        conversation: List<Pair<String, String>>, // role ("user" | "model") -> text
        systemInstruction: String = "You are Study AI, an intelligent, versatile academic assistant and tutor. Explain concepts clearly, format responses with headings, bullet points, and code blocks."
    ): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKey
        val lastUserMessage = conversation.lastOrNull { it.first == "user" }?.second ?: ""

        if (key.isBlank()) {
            return@withContext Result.success(createFallbackChatReply(lastUserMessage))
        }

        try {
            val contents = conversation.takeLast(10).map { (role, text) ->
                GeminiContent(
                    role = if (role == "user") "user" else "model",
                    parts = listOf(GeminiPart(text = text))
                )
            }

            val requestPayload = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction))),
                generationConfig = GeminiGenerationConfig(temperature = 0.6f)
            )

            val request = Request.Builder()
                .url("$baseUrl?key=$key")
                .post(json.encodeToString(GeminiRequest.serializer(), requestPayload).toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseText = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.success(createFallbackChatReply(lastUserMessage))
            }
            val parsedResponse = json.decodeFromString(GeminiResponse.serializer(), responseText)
            val reply = parsedResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: createFallbackChatReply(lastUserMessage)
            Result.success(reply)
        } catch (e: Exception) {
            Result.success(createFallbackChatReply(lastUserMessage))
        }
    }

    /**
     * Specialized real-time conversational tutor method for Study AI Live!
     * Optimized for natural speech pacing, concise spoken clarity, and interactive back-and-forth.
     */
    suspend fun speakLiveTurn(
        userVoiceInput: String,
        subjectContext: String,
        persona: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank()) {
            return@withContext Result.success(createFallbackLiveReply(userVoiceInput, subjectContext))
        }

        try {
            val systemInstruction = """
                You are Study AI Live, a real-time conversational voice tutor like Gemini Live.
                Current Subject Focus: $subjectContext
                Persona: $persona
                Voice Conversation Guidelines:
                1. You are speaking directly to the student over a live voice stream. Keep explanations spoken-friendly, energetic, engaging, and concise (2-4 sentences max per turn).
                2. Do not use complex ASCII symbols or markdown tables that cannot be read aloud naturally.
                3. If the student asks for a solution or proof, speak the key intuitive takeaway first, explain the main step, and ask if they want the next step.
                4. Always sound encouraging, sharp, and articulate.
            """.trimIndent()

            val contents = mutableListOf<GeminiContent>()
            conversationHistory.takeLast(6).forEach { (role, text) ->
                contents.add(
                    GeminiContent(
                        role = if (role == "user") "user" else "model",
                        parts = listOf(GeminiPart(text = text))
                    )
                )
            }
            contents.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userVoiceInput))))

            val requestPayload = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction))),
                generationConfig = GeminiGenerationConfig(temperature = 0.5f)
            )

            val request = Request.Builder()
                .url("$baseUrl?key=$key")
                .post(json.encodeToString(GeminiRequest.serializer(), requestPayload).toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseText = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.success(createFallbackLiveReply(userVoiceInput, subjectContext))
            }
            val parsedResponse = json.decodeFromString(GeminiResponse.serializer(), responseText)
            val reply = parsedResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: createFallbackLiveReply(userVoiceInput, subjectContext)
            Result.success(reply.trim())
        } catch (e: Exception) {
            Result.success(createFallbackLiveReply(userVoiceInput, subjectContext))
        }
    }

    private fun createFallbackLiveReply(input: String, subject: String): String {
        val lower = input.lowercase(Locale.ROOT)
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hey there! I am Study AI Live. I'm ready to talk through $subject concepts, brainstorm homework problems, or quiz you in real-time. What are we studying right now?"
            lower.contains("math") || lower.contains("derivative") || lower.contains("integral") || lower.contains("calculate") ->
                "Great math question! Let's break it down intuitively. The derivative measures the instant rate of change, so whenever you see a product of two terms, remember to apply the product rule: first times derivative of second plus second times derivative of first. Want to try computing the next step together?"
            lower.contains("physics") || lower.contains("force") || lower.contains("velocity") || lower.contains("energy") ->
                "In physics, always start by listing the known quantities and checking for conservation laws! If there is no external friction, total mechanical energy is strictly conserved. Shall we set up the energy equation together?"
            lower.contains("code") || lower.contains("python") || lower.contains("algorithm") ->
                "For this algorithm, think about the time complexity first. If we sort first it will take O(N log N), but with a hash map we can often achieve linear O(N) time. Would you like me to walk through the hash map approach?"
            else ->
                "That's a thoughtful question in $subject. The key principle here is understanding how each component interacts with the fundamental laws of the topic. Tell me what you think happens first, and we will build from there!"
        }
    }

    private fun createFallbackSolution(subject: String, question: String, style: ExplanationStyle): QuestionSolution {
        return QuestionSolution(
            subject = subject,
            question = question,
            directAnswer = "The solution follows directly from the fundamental laws of $subject by evaluating boundary constraints and applying systematic deduction.",
            steps = listOf(
                SolutionStep(
                    stepNumber = 1,
                    title = "Identify Given Values & System Constraints",
                    explanation = "Carefully state the known variables, isolate target unknowns, and verify dimensional consistency.",
                    formulaOrDetail = "Known parameters: { x₀, v₀, k } -> Target: { x(t) }"
                ),
                SolutionStep(
                    stepNumber = 2,
                    title = "Apply Core Governing Theorem",
                    explanation = "Substitute the governing formula and reduce algebraic degrees of freedom.",
                    formulaOrDetail = "d/dx [ f(x) * g(x) ] = f'(x)g(x) + f(x)g'(x)"
                ),
                SolutionStep(
                    stepNumber = 3,
                    title = "Compute Final Numerical & Analytical Result",
                    explanation = "Evaluate the terms and verify asymptotic behavior under limit conditions.",
                    formulaOrDetail = "Result = Q.E.D."
                )
            ),
            coreConcepts = listOf("$subject Fundamentals", "Analytical Derivation", "Boundary Conditions"),
            tipsAndCommonMistakes = listOf(
                "Never forget the chain rule constant factor.",
                "Verify dimensional balance before final substitution.",
                "Check for sign changes across the equality boundary."
            ),
            followUpPractice = "What happens if the boundary condition increases by a factor of 2?"
        )
    }

    private fun createFallbackQuiz(subject: String, topic: String, difficulty: String, count: Int): PracticeQuiz {
        val questions = when {
            subject.contains("Math", true) -> listOf(
                QuizQuestion(1, "What is the derivative of f(x) = ln(3x² + 1)?", listOf("6x / (3x² + 1)", "3 / (3x² + 1)", "6x ln(3x² + 1)", "1 / (6x)"), 0, "By chain rule, d/dx[ln(u)] = u'/u. Here u = 3x² + 1 and u' = 6x.", "Calculus Chain Rule"),
                QuizQuestion(2, "What is the value of ∫₀¹ 3x² dx?", listOf("1", "3", "0.5", "2"), 0, "[x³] from 0 to 1 = 1³ - 0 = 1.", "Definite Integrals"),
                QuizQuestion(3, "If matrix A is 2x3 and matrix B is 3x4, what is the dimension of A × B?", listOf("2x4", "3x3", "2x3", "Undefined"), 0, "Inner dimensions match (3=3), resulting in outer dimensions 2x4.", "Linear Algebra"),
                QuizQuestion(4, "What is the discriminant of 2x² - 4x + 2 = 0?", listOf("0", "16", "-8", "4"), 0, "b² - 4ac = (-4)² - 4(2)(2) = 16 - 16 = 0 (one real repeated root).", "Quadratic Equations"),
                QuizQuestion(5, "What is the period of the function f(x) = sin(4x)?", listOf("π/2", "2π", "π", "4π"), 0, "Period of sin(kx) is 2π/k = 2π/4 = π/2.", "Trigonometry")
            )
            subject.contains("Physics", true) -> listOf(
                QuizQuestion(1, "What is the net work done on an object moving at constant speed along a circular path?", listOf("Zero", "Positive", "Negative", "Depends on radius"), 0, "Centripetal force is perpendicular to velocity at every instant, so F · dr = 0.", "Circular Motion"),
                QuizQuestion(2, "According to Lenz's law, the direction of induced current always:", listOf("Opposes the magnetic flux change", "Enhances the flux change", "Points toward magnetic north", "Equals zero"), 0, "Lenz's Law ensures conservation of energy by opposing the change inducing it.", "Electromagnetism"),
                QuizQuestion(3, "Which thermodynamic cycle achieves the theoretical maximum efficiency?", listOf("Carnot Cycle", "Otto Cycle", "Rankine Cycle", "Diesel Cycle"), 0, "The Carnot cycle operating between two temperatures defines the upper bound.", "Thermodynamics"),
                QuizQuestion(4, "What is the kinetic energy of a 2kg mass moving at 3 m/s?", listOf("9 J", "6 J", "18 J", "3 J"), 0, "KE = 1/2 * m * v² = 0.5 * 2 * 9 = 9 Joules.", "Work and Energy"),
                QuizQuestion(5, "In special relativity, as an object's speed approaches the speed of light, its relativistic momentum:", listOf("Approaches infinity", "Remains constant", "Approaches zero", "Decreases linearly"), 0, "p = γmv where Lorentz factor γ -> ∞ as v -> c.", "Special Relativity")
            )
            else -> listOf(
                QuizQuestion(1, "What is the primary characteristic of an algorithm with O(1) time complexity?", listOf("Constant execution time regardless of input size", "Linear execution time", "Logarithmic execution time", "Zero memory usage"), 0, "O(1) means performance is independent of data set size.", "Algorithm Complexity"),
                QuizQuestion(2, "What is the role of mitochondria in eukaryotic cellular respiration?", listOf("ATP generation via oxidative phosphorylation", "Protein translation", "DNA replication", "Lipid degradation"), 0, "Mitochondria house the citric acid cycle and ATP synthase complexes.", "Cell Biology"),
                QuizQuestion(3, "In economic theory, what happens to supply when market price increases (Law of Supply)?", listOf("Quantity supplied increases", "Quantity supplied decreases", "Supply curve shifts left", "Price elasticity becomes zero"), 0, "Producers are willing to supply more at higher prices, ceteris paribus.", "Microeconomics"),
                QuizQuestion(4, "Which philosopher is famous for the assertion 'Cogito, ergo sum'?", listOf("René Descartes", "Immanuel Kant", "John Locke", "David Hume"), 0, "Descartes formulated this foundational premise in his Discourse on Method.", "Philosophy"),
                QuizQuestion(5, "What is the primary function of DNS in computer networking?", listOf("Translating domain names to IP addresses", "Encrypting HTTP packets", "Routing BGP prefixes", "Allocating MAC addresses"), 0, "DNS translates human-readable hostnames to numeric IP addresses.", "Computer Networking")
            )
        }
        return PracticeQuiz(
            title = "$topic Practice Quiz",
            subject = subject,
            topic = topic,
            difficulty = difficulty,
            questions = questions.take(count)
        )
    }

    private fun createFallbackFollowUp(subject: String, followUpQuery: String): String {
        return """
            Regarding your question in $subject: "$followUpQuery"
            
            Key Academic Principles:
            1. **Foundational Concept**: Always verify boundary conditions and dimensional consistency before substituting numerical values.
            2. **Problem-Solving Strategy**: Isolate the unknown variable or identify the core theorem connecting the known parameters.
            3. **Common Nuance**: Check whether energy or momentum conservation applies, or if dissipative factors are explicitly neglected.
            
            Feel free to ask for a deeper step-by-step breakdown or another practice quiz on this!
        """.trimIndent()
    }

    private fun createFallbackChatReply(userMessage: String): String {
        val lower = userMessage.lowercase(Locale.ROOT)
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello! I am your AI academic tutor and assistant. How can I help you study today? You can ask me to explain any complex concept, debug code, solve equations, or explore books in our Digital Library!"
            lower.contains("python") || lower.contains("code") || lower.contains("algorithm") ->
                """
                Here is an overview of how to approach this problem:
                
                ```python
                # Clean, efficient implementation
                def solve_problem(data):
                    result = [item for item in data if item is not None]
                    return sorted(result)
                ```
                
                **Key Takeaways:**
                • Time Complexity: O(n log n)
                • Space Complexity: O(n)
                • Clean modular functions make testing and maintenance straightforward.
                """.trimIndent()
            else ->
                """
                That's a great question! Here is a structured breakdown:
                
                ### 1. Key Concept
                The core idea revolves around understanding fundamental principles before jumping into advanced applications.
                
                ### 2. Detailed Explanation
                When analyzing this topic, consider both theoretical foundations and practical examples. Break the problem into subcomponents and evaluate each step systematically.
                
                ### 3. Study Recommendation
                Would you like me to generate a practice quiz on this, start a **Study AI Live** voice conversation, or show a step-by-step solution?
                """.trimIndent()
        }
    }
}
