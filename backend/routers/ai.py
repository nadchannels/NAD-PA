"""
AI Chat router — implements the Brainstorm/Execution state machine.
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional
from datetime import datetime, timezone
from services.gemini_service import brainstorm, execute_schedule, is_execution_trigger
from services.firebase_service import get_db
import uuid

router = APIRouter(prefix="/ai", tags=["AI"])


class ChatMessage(BaseModel):
    role: str  # "user" or "assistant"
    content: str


class ChatRequest(BaseModel):
    message: str
    history: list[ChatMessage] = []


class ChatResponse(BaseModel):
    reply: str
    mode: str  # "brainstorming" or "execution"
    scheduled_items: Optional[int] = None


def _get_user_context() -> dict:
    """Fetch current goals and upcoming tasks for AI context."""
    try:
        db = get_db()
        goals = []
        tasks = []
        for doc in db.collection("goals").stream():
            d = doc.to_dict()
            goals.append({"title": d.get("title"), "type": d.get("type"), "status": d.get("status"), "completion": d.get("completionPercentage")})
        for doc in db.collection("tasks").where("relativeWeekIndex", ">=", 0).stream():
            d = doc.to_dict()
            tasks.append({"title": d.get("title"), "day": d.get("dayOfWeek"), "start": d.get("startTime"), "end": d.get("endTime"), "week": d.get("relativeWeekIndex")})
        return {"goals": goals, "upcoming_tasks": tasks}
    except Exception:
        return {"goals": [], "upcoming_tasks": []}


async def _execute_function_calls(function_calls: list) -> int:
    """Execute Gemini function calls against Firestore. Returns count of executed calls."""
    db = get_db()
    count = 0
    now = datetime.now(timezone.utc).isoformat()

    for call in function_calls:
        name = call.get("name")
        args = call.get("args", {})

        try:
            if name == "create_task":
                task_id = str(uuid.uuid4())
                data = {
                    "title": args.get("title", ""),
                    "description": args.get("description", ""),
                    "dayOfWeek": args.get("dayOfWeek", "Monday"),
                    "startTime": args.get("startTime", "09:00"),
                    "endTime": args.get("endTime", "10:00"),
                    "relativeWeekIndex": int(args.get("relativeWeekIndex", 0)),
                    "status": "Pending",
                    "createdAt": now,
                    "updatedAt": now,
                }
                db.collection("tasks").document(task_id).set(data)
                count += 1

            elif name == "create_goal":
                goal_id = str(uuid.uuid4())
                data = {
                    "title": args.get("title", ""),
                    "type": args.get("type", "Short-Term"),
                    "status": "Pending",
                    "completionPercentage": 0,
                    "createdAt": now,
                    "updatedAt": now,
                }
                db.collection("goals").document(goal_id).set(data)
                count += 1

            elif name == "update_task":
                task_id = args.pop("id", None)
                if task_id:
                    args["updatedAt"] = now
                    db.collection("tasks").document(task_id).update(args)
                    count += 1

            elif name == "delete_task":
                task_id = args.get("id")
                if task_id:
                    db.collection("tasks").document(task_id).delete()
                    count += 1

        except Exception as e:
            print(f"Error executing {name}: {e}")
            continue

    return count


@router.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """Main AI chat endpoint — routes between Brainstorm and Execution modes."""
    history_dicts = [m.model_dump() for m in request.history]
    context = _get_user_context()

    # Check if user triggers Execution Mode
    if is_execution_trigger(request.message):
        result = await execute_schedule(history_dicts, context)
        function_calls = result.get("function_calls", [])
        scheduled_count = await _execute_function_calls(function_calls)

        weeks_affected = list(set(
            call["args"].get("relativeWeekIndex", 0)
            for call in function_calls
            if call["name"] == "create_task"
        ))
        week_str = ", ".join([f"Week {w}" for w in sorted(weeks_affected)]) if weeks_affected else "Week 0"

        summary = result.get("summary") or f"✅ Scheduling complete. {scheduled_count} item(s) added. Your calendar for {week_str} has been updated."

        return ChatResponse(
            reply=summary,
            mode="execution",
            scheduled_items=scheduled_count
        )

    # Default: Brainstorming mode
    reply = await brainstorm(request.message, history_dicts, context)
    return ChatResponse(reply=reply, mode="brainstorming")
