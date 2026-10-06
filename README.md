# Study AI - Academic Tutor & Homework Solver

Study AI is an intelligent Android application built with **Jetpack Compose**, **Kotlin**, **Room Database**, and Google's **Gemini 3.5** multimodal models.

## ✨ Features

- **ChatGPT-Style Multimodal AI Chat**:
  - Upload photos, record videos, and attach documents (PDFs, study notes).
  - Built-in camera photo capture and video recording.
  - Attachment preview chips and voice dictation mic for speech-to-text.
  - Text-to-speech audio narration of AI answers.
- **Study AI Live**:
  - Real-time conversational voice tutor with dynamic interactive audio visualizer.
  - Double-tap to interrupt and ask spontaneous follow-up questions.
- **Step-by-Step Problem Solver (Ask StudyAI)**:
  - Deep-dive breakdown of math, physics, chemistry, biology, and CS problems.
  - Core concepts, exam caveats, and common traps.
- **Instant Practice Quizzes**:
  - Dynamic AI-generated quizzes by subject, topic, and difficulty (Easy, Medium, Hard).
  - Instant scoring and detailed rationales for each answer.
- **Digital Library**:
  - Academic textbook catalog with reader theme and font customization.
- **Saved History & Bookmarks**:
  - Local persistence powered by Room database with offline capability.

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Local Persistence**: Room SQLite Database
- **Networking**: OkHttp, Retrofit & Gemini REST API
- **AI Model**: Gemini 3.5 Flash (`gemini-3.5-flash`)
- **Speech & Audio**: Android SpeechRecognizer & TextToSpeech

## 🚀 Getting Started

1. Clone this repository:
   ```bash
   git clone https://github.com/<your-username>/study-ai.git
   ```
2. Open the project in **Android Studio Ladybug** or newer.
3. Configure your Gemini API key in `.env` (or via AI Studio secrets panel):
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
4. Build and run on an Android device or emulator running Android 7.0+ (API level 24+).
