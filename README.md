# NAD PA — Personal Assistant

A minimalist AI-powered personal assistant app with Islamic daily inspiration, smart scheduling, and a conversational AI agent.

## Project Structure

```
NAD-PA/
├── app_project/          # Android app (Kotlin + Jetpack Compose)
├── backend/              # Python FastAPI backend
│   ├── main.py           # Entry point
│   ├── routers/          # REST API routes
│   ├── services/         # Firebase, Gemini, Quran, Hadith
│   ├── models/           # Pydantic models
│   ├── cron/             # Scheduled jobs
│   ├── .env.example      # Environment variable template
│   └── requirements.txt  # Python dependencies
└── README.md
```

## Backend Setup

```bash
cd backend
pip install -r requirements.txt
# Copy .env.example to .env and fill in your credentials
cp .env.example .env
# Place your Firebase service account JSON as firebase-credentials.json
uvicorn main:app --reload
```

## Android Setup

1. Open `app_project/` in Android Studio
2. The app connects to `http://10.0.2.2:8000` (Android emulator localhost)
3. Run on emulator: `./gradlew installDebug`

## Features

- **Home**: Real-time clock, current/upcoming sessions, daily Ayah & Hadith
- **Schedule**: Week navigator (WEEK -100 → WEEK +52) with timetable grid
- **Dashboard**: Goals tracker with progress bars (Short-Term/Long-Term)
- **AI Chat**: Gemini-powered assistant with Brainstorming → Execution state machine

## Cron Jobs (Auto-scheduled)

| Job | Schedule | Action |
|-----|----------|--------|
| Week Shift | Every Sunday 23:59:59 | Decrements all `relativeWeekIndex` by 1 |
| Daily Inspiration | Every day 00:01 AM | Fetches Ayah + Hadith + AI commentary |

## Environment Variables

```
GEMINI_API_KEY=your_key_here
FIREBASE_CREDENTIALS_PATH=firebase-credentials.json
```
