"""
Daily Inspiration Worker — runs at 00:01 AM every day.
Fetches Ayah + Hadith, generates AI commentary, saves to Firestore.
"""
import sys
import os
import asyncio
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from datetime import date, datetime, timezone
from services.firebase_service import get_db
from services.quran_service import fetch_random_ayah
from services.hadith_service import fetch_random_hadith
from services.gemini_service import generate_inspiration_commentary


async def run_daily_inspiration():
    """
    Main daily inspiration pipeline:
    1. Fetch random Ayah (Arabic + English)
    2. Fetch random Hadith (Arabic + English)
    3. Generate AI commentary via Gemini
    4. Save to daily_inspiration/{today} in Firestore
    """
    today = date.today().isoformat()
    db = get_db()
    collection = db.collection("daily_inspiration")

    # Check if already fetched today
    existing = collection.document(today).get()
    if existing.exists:
        print(f"[Daily Inspiration] Already fetched for {today}. Skipping.")
        return

    print(f"[Daily Inspiration] Fetching for {today}...")

    try:
        # Fetch Ayah and Hadith concurrently
        ayah_data, hadith_data = await asyncio.gather(
            fetch_random_ayah(),
            fetch_random_hadith()
        )

        # Generate AI commentary
        commentary = await generate_inspiration_commentary(
            ayah_translation=ayah_data.get("ayahTranslation", ""),
            hadith_translation=hadith_data.get("hadithTranslation", "")
        )

        # Save to Firestore
        doc_data = {
            "date": today,
            "ayahText": ayah_data.get("ayahText", ""),
            "ayahTranslation": ayah_data.get("ayahTranslation", ""),
            "hadithText": hadith_data.get("hadithText", ""),
            "hadithTranslation": hadith_data.get("hadithTranslation", ""),
            "aiCommentary": commentary,
            "fetchedAt": datetime.now(timezone.utc).isoformat(),
        }

        collection.document(today).set(doc_data)
        print(f"[Daily Inspiration] Successfully saved for {today}")
        print(f"  Ayah: {ayah_data.get('ayahTranslation', '')[:80]}...")
        print(f"  Commentary: {commentary[:80]}...")

        return doc_data

    except Exception as e:
        print(f"[Daily Inspiration] ERROR: {e}")
        raise


def run():
    asyncio.run(run_daily_inspiration())


if __name__ == "__main__":
    run()
