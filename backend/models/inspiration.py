from pydantic import BaseModel
from typing import Optional


class InspirationResponse(BaseModel):
    date: str
    ayahText: str
    ayahTranslation: str
    hadithText: str
    hadithTranslation: str
    aiCommentary: str
    fetchedAt: Optional[str] = None
