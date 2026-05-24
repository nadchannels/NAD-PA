"""
NAD PA — FastAPI Backend Entry Point.
Runs the REST API server and schedules background cron jobs.
"""
import asyncio
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.cron import CronTrigger
from contextlib import asynccontextmanager
from routers import tasks, goals, inspiration, ai
from cron.week_shift import run_week_shift
from cron.daily_inspiration import run_daily_inspiration


def create_scheduler() -> AsyncIOScheduler:
    scheduler = AsyncIOScheduler()

    # === WEEKLY CRON: Relative Week Engine ===
    # Every Sunday at 23:59:59 — decrement all relativeWeekIndex by 1
    scheduler.add_job(
        run_week_shift,
        CronTrigger(day_of_week="sun", hour=23, minute=59, second=59),
        id="week_shift",
        name="Weekly Relative Week Index Shift",
        replace_existing=True,
    )

    # === DAILY CRON: Islamic Inspiration Fetch ===
    # Every day at 00:01 AM
    scheduler.add_job(
        run_daily_inspiration,
        CronTrigger(hour=0, minute=1, second=0),
        id="daily_inspiration",
        name="Daily Ayah + Hadith Fetch",
        replace_existing=True,
    )

    return scheduler


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup and shutdown lifecycle manager."""
    scheduler = create_scheduler()
    scheduler.start()
    print("\n============================")
    print("  NAD PA Backend Started")
    print("  Cron Jobs Scheduled:")
    print("    - Weekly week shift: Every Sunday 23:59:59")
    print("    - Daily inspiration: Every day 00:01 AM")
    print("===========================\n")
    yield
    scheduler.shutdown()
    print("NAD PA Backend Stopped.")


app = FastAPI(
    title="NAD PA — Personal Assistant API",
    description="Backend for the NAD Personal Assistant app. Handles tasks, goals, AI chat, and daily Islamic inspiration.",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS — allow Android app to connect
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Restrict to your app domain in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routers
app.include_router(tasks.router)
app.include_router(goals.router)
app.include_router(inspiration.router)
app.include_router(ai.router)


@app.get("/", tags=["Health"])
async def root():
    return {
        "status": "online",
        "app": "NAD PA Personal Assistant",
        "version": "1.0.0",
        "endpoints": ["/tasks", "/goals", "/inspiration", "/ai/chat"]
    }


@app.get("/health", tags=["Health"])
async def health():
    return {"status": "healthy"}
