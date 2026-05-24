"""
Goals CRUD router.
"""
from fastapi import APIRouter, HTTPException
from datetime import datetime, timezone
import uuid
from models.goal import GoalCreate, GoalUpdate, GoalResponse
from services.firebase_service import get_db

router = APIRouter(prefix="/goals", tags=["Goals"])
COLLECTION = "goals"


@router.get("/", response_model=list[GoalResponse])
async def get_goals(status: str = None, type: str = None):
    db = get_db()
    query = db.collection(COLLECTION)
    if status:
        query = query.where("status", "==", status)
    if type:
        query = query.where("type", "==", type)
    docs = query.stream()
    results = []
    for doc in docs:
        data = doc.to_dict()
        data["id"] = doc.id
        results.append(GoalResponse(**data))
    return results


@router.post("/", response_model=GoalResponse, status_code=201)
async def create_goal(goal: GoalCreate):
    db = get_db()
    goal_id = str(uuid.uuid4())
    now = datetime.now(timezone.utc).isoformat()
    data = goal.model_dump()
    data["type"] = data["type"].value
    data["status"] = data["status"].value
    data["createdAt"] = now
    data["updatedAt"] = now
    db.collection(COLLECTION).document(goal_id).set(data)
    return GoalResponse(id=goal_id, **data)


@router.get("/{goal_id}", response_model=GoalResponse)
async def get_goal(goal_id: str):
    db = get_db()
    doc = db.collection(COLLECTION).document(goal_id).get()
    if not doc.exists:
        raise HTTPException(status_code=404, detail="Goal not found")
    data = doc.to_dict()
    data["id"] = doc.id
    return GoalResponse(**data)


@router.patch("/{goal_id}", response_model=GoalResponse)
async def update_goal(goal_id: str, goal: GoalUpdate):
    db = get_db()
    doc_ref = db.collection(COLLECTION).document(goal_id)
    if not doc_ref.get().exists:
        raise HTTPException(status_code=404, detail="Goal not found")
    update_data = {k: v for k, v in goal.model_dump().items() if v is not None}
    if "type" in update_data:
        update_data["type"] = update_data["type"].value
    if "status" in update_data:
        update_data["status"] = update_data["status"].value
    update_data["updatedAt"] = datetime.now(timezone.utc).isoformat()
    doc_ref.update(update_data)
    updated = doc_ref.get().to_dict()
    updated["id"] = goal_id
    return GoalResponse(**updated)


@router.delete("/{goal_id}", status_code=204)
async def delete_goal(goal_id: str):
    db = get_db()
    doc_ref = db.collection(COLLECTION).document(goal_id)
    if not doc_ref.get().exists:
        raise HTTPException(status_code=404, detail="Goal not found")
    doc_ref.delete()
