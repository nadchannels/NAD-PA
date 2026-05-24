"""
Hadith API service — fetches a random hadith from fawazahmed0/hadith-api (Sahih Bukhari).
Delivered via jsDelivr CDN. Returns both Arabic and English text.
"""
import random
import httpx

# Sahih Bukhari has 7563 hadiths — we rotate by day of year for consistency
BUKHARI_COUNT = 7563
BASE_URL = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions"


async def fetch_random_hadith() -> dict:
    """Fetch a random hadith from Sahih Bukhari with Arabic + English text."""
    hadith_number = random.randint(1, BUKHARI_COUNT)

    eng_url = f"{BASE_URL}/eng-bukhari/{hadith_number}.json"
    ara_url = f"{BASE_URL}/ara-bukhari/{hadith_number}.json"

    async with httpx.AsyncClient(timeout=15.0) as client:
        eng_resp, ara_resp = await _fetch_both(client, eng_url, ara_url)

    english_text = _extract_hadith_text(eng_resp)
    arabic_text = _extract_hadith_text(ara_resp)

    return {
        "hadithText": arabic_text,
        "hadithTranslation": english_text,
        "hadithNumber": hadith_number,
    }


async def _fetch_both(client, eng_url, ara_url):
    import asyncio
    eng_task = client.get(eng_url)
    ara_task = client.get(ara_url)
    eng_resp = await eng_task
    ara_resp = await ara_task
    return eng_resp.json() if eng_resp.status_code == 200 else {}, \
           ara_resp.json() if ara_resp.status_code == 200 else {}


def _extract_hadith_text(data: dict) -> str:
    """Extract hadith body from API response."""
    hadith_list = data.get("hadiths", [])
    if hadith_list:
        return hadith_list[0].get("text", "")
    # Fallback: sometimes the structure is flat
    return data.get("hadith", [{}])[0].get("body", "") if data.get("hadith") else ""
