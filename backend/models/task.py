from pydantic import BaseModel, Field
from typing import Optional
from enum import Enum
from datetime import datetime


class DayOfWeek(str, Enum):
    MONDAY = "Monday"
    TUESDAY = "Tuesday"
    WEDNESDAY = "Wednesday"
    THURSDAY = "Thursday"
    FRIDAY = "Friday"
    SATURDAY = "Saturday"
    SUNDAY = "Sunday"


class TaskStatus(str, Enum):
    PENDING = "Pending"
    COMPLETED = "Completed"
    MISSED = "Missed"


class TaskCreate(BaseModel):
    title: str
    description: Optional[str] = ""
    dayOfWeek: DayOfWeek
    startTime: str  # HH:MM
    endTime: str    # HH:MM
    relativeWeekIndex: int = 0  # 0 = current week, 1 = next week, -1 = last week
    status: TaskStatus = TaskStatus.PENDING


class TaskUpdate(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    dayOfWeek: Optional[DayOfWeek] = None
    startTime: Optional[str] = None
    endTime: Optional[str] = None
    relativeWeekIndex: Optional[int] = None
    status: Optional[TaskStatus] = None


class TaskResponse(BaseModel):
    id: str
    title: str
    description: str
    dayOfWeek: DayOfWeek
    startTime: str
    endTime: str
    relativeWeekIndex: int
    status: TaskStatus
    createdAt: Optional[str] = None
    updatedAt: Optional[str] = None
