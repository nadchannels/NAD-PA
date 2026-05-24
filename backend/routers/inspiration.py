"""
Daily Inspiration router — serves cached daily Islamic texts.
"""
from fastapi import APIRouter, HTTPException
from datetime import date
from models.inspiration import InspirationResponse
from services.firebase_service import get_db

router = APIRouter(prefix="/inspiration", tags=["Inspiration"])
COLLECTION = "daily_inspiration"


@router.get("/today", response_model=InspirationResponse)
async def get_today_inspiration():
    """Get today's inspiration from cache."""
    today = date.today().isoformat()
    db = get_db()
    doc = db.collection(COLLECTION).document(today).get()
    if not doc.exists:
        raise HTTPException(
            status_code=404,
            detail="Today's inspiration not yet fetched. The daily cron runs at 00:01 AM."
        )
    data = doc.to_dict()
    data["date"] = today
    return InspirationResponse(**data)


@router.get("/{date_str}", response_model=InspirationResponse)
async def get_inspiration_by_date(date_str: str):
    """Get inspiration for a specific date (YYYY-MM-DD)."""
    db = get_db()
    doc = db.collection(COLLECTION).document(date_str).get()
    if not doc.exists:
        raise HTTPException(status_code=404, detail=f"No inspiration found for {date_str}")
    data = doc.to_dict()
    data["date"] = date_str
    return InspirationResponse(**data)
