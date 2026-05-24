"""
Quran API service — fetches a random Ayah from api.alquran.cloud.
Returns both Arabic (Uthmani script) and English (Sahih International) text.
"""
import random
import httpx

TOTAL_AYAHS = 6236
EDITIONS = "quran-uthmani,en.sahih"


async def fetch_random_ayah() -> dict:
    """Fetch a random Ayah with Arabic + English translation."""
    ayah_number = random.randint(1, TOTAL_AYAHS)
    url = f"https://api.alquran.cloud/v1/ayah/{ayah_number}/editions/{EDITIONS}"

    async with httpx.AsyncClient(timeout=15.0) as client:
        response = await client.get(url)
        response.raise_for_status()
        data = response.json()

    editions = data.get("data", [])
    arabic_text = ""
    english_text = ""

    for edition in editions:
        identifier = edition.get("edition", {}).get("identifier", "")
        if identifier == "quran-uthmani":
            arabic_text = edition.get("text", "")
            surah = edition.get("surah", {}).get("englishName", "")
            ayah_num_in_surah = edition.get("numberInSurah", "")
            arabic_text = f"{arabic_text} ({surah} {ayah_num_in_surah})"
        elif identifier == "en.sahih":
            english_text = edition.get("text", "")
            surah = edition.get("surah", {}).get("englishName", "")
            ayah_num_in_surah = edition.get("numberInSurah", "")
            english_text = f"{english_text} ({surah} {ayah_num_in_surah})"

    return {
        "ayahText": arabic_text,
        "ayahTranslation": english_text,
    }
