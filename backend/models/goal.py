from pydantic import BaseModel
from typing import Optional
from enum import Enum


class GoalType(str, Enum):
    SHORT_TERM = "Short-Term"
    LONG_TERM = "Long-Term"


class GoalStatus(str, Enum):
    ACHIEVED = "Achieved"
    PENDING = "Pending"


class GoalCreate(BaseModel):
    title: str
    type: GoalType
    status: GoalStatus = GoalStatus.PENDING
    completionPercentage: int = 0


class GoalUpdate(BaseModel):
    title: Optional[str] = None
    type: Optional[GoalType] = None
    status: Optional[GoalStatus] = None
    completionPercentage: Optional[int] = None


class GoalResponse(BaseModel):
    id: str
    title: str
    type: GoalType
    status: GoalStatus
    completionPercentage: int
    createdAt: Optional[str] = None
    updatedAt: Optional[str] = None
