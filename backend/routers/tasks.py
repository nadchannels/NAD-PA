"""
Tasks/Sessions CRUD router.
"""
from fastapi import APIRouter, HTTPException
from datetime import datetime, timezone
import uuid
from models.task import TaskCreate, TaskUpdate, TaskResponse
from services.firebase_service import get_db

router = APIRouter(prefix="/tasks", tags=["Tasks"])
COLLECTION = "tasks"


@router.get("/", response_model=list[TaskResponse])
async def get_tasks(relativeWeekIndex: int = None):
    """Get all tasks, optionally filtered by relativeWeekIndex."""
    db = get_db()
    query = db.collection(COLLECTION)
    if relativeWeekIndex is not None:
        query = query.where("relativeWeekIndex", "==", relativeWeekIndex)
    docs = query.stream()
    results = []
    for doc in docs:
        data = doc.to_dict()
        data["id"] = doc.id
        results.append(TaskResponse(**data))
    return results


@router.post("/", response_model=TaskResponse, status_code=201)
async def create_task(task: TaskCreate):
    """Create a new task."""
    db = get_db()
    task_id = str(uuid.uuid4())
    now = datetime.now(timezone.utc).isoformat()
    data = task.model_dump()
    data["createdAt"] = now
    data["updatedAt"] = now
    data["dayOfWeek"] = data["dayOfWeek"].value
    data["status"] = data["status"].value
    db.collection(COLLECTION).document(task_id).set(data)
    return TaskResponse(id=task_id, **data)


@router.get("/{task_id}", response_model=TaskResponse)
async def get_task(task_id: str):
    db = get_db()
    doc = db.collection(COLLECTION).document(task_id).get()
    if not doc.exists:
        raise HTTPException(status_code=404, detail="Task not found")
    data = doc.to_dict()
    data["id"] = doc.id
    return TaskResponse(**data)


@router.patch("/{task_id}", response_model=TaskResponse)
async def update_task(task_id: str, task: TaskUpdate):
    db = get_db()
    doc_ref = db.collection(COLLECTION).document(task_id)
    doc = doc_ref.get()
    if not doc.exists:
        raise HTTPException(status_code=404, detail="Task not found")
    update_data = {k: v for k, v in task.model_dump().items() if v is not None}
    if "dayOfWeek" in update_data:
        update_data["dayOfWeek"] = update_data["dayOfWeek"].value
    if "status" in update_data:
        update_data["status"] = update_data["status"].value
    update_data["updatedAt"] = datetime.now(timezone.utc).isoformat()
    doc_ref.update(update_data)
    updated = doc_ref.get().to_dict()
    updated["id"] = task_id
    return TaskResponse(**updated)


@router.delete("/{task_id}", status_code=204)
async def delete_task(task_id: str):
    db = get_db()
    doc_ref = db.collection(COLLECTION).document(task_id)
    if not doc_ref.get().exists:
        raise HTTPException(status_code=404, detail="Task not found")
    doc_ref.delete()
