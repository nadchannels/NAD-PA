"""
Gemini AI service — handles both Brainstorming and Execution modes.
Brainstorming: conversational, read-only context.
Execution: uses function calling to mutate Firestore via structured JSON.
"""
import os
import json
from typing import Optional
import google.generativeai as genai
from dotenv import load_dotenv

load_dotenv()

genai.configure(api_key=os.getenv("GEMINI_API_KEY"))

TRIGGER_PHRASES = [
    "schedule the plan",
    "schedule this plan",
    "execute the plan",
    "add to schedule",
    "book it",
    "confirm the schedule",
    "lock it in",
    "finalize the schedule",
]

BRAINSTORM_SYSTEM_PROMPT = """
You are NAD PA — a highly intelligent, minimalist Personal Assistant powered by AI.
You are in BRAINSTORMING MODE.

Your role:
- Act as a thoughtful sounding board and life coach.
- Help the user refine their goals, plan their week, and organize their thoughts.
- Ask clarifying questions to understand their needs.
- Suggest actionable tasks with specific time blocks when appropriate.
- Be concise, warm, and encouraging.

You have READ-ONLY access to the user's current context:
{context}

IMPORTANT: You CANNOT modify the database in this mode. You are purely advisory.
When the user says "schedule the plan" or a variation, the system will automatically switch to Execution Mode.
"""

EXECUTION_SYSTEM_PROMPT = """
You are NAD PA in EXECUTION MODE.
Your job is to parse the conversation history and extract all agreed-upon tasks and goals,
then call the appropriate functions to schedule them in the database.

Rules:
- Only schedule tasks the user has explicitly agreed to.
- Use relativeWeekIndex: 0 for current week, 1 for next week, etc.
- dayOfWeek must be: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, or Sunday.
- startTime and endTime must be in HH:MM format (24h).
- After calling functions, return a clear summary.
"""

TASK_FUNCTIONS = [
    {
        "name": "create_task",
        "description": "Creates a new scheduled task/session in the calendar.",
        "parameters": {
            "type": "object",
            "properties": {
                "title": {"type": "string", "description": "Task title"},
                "description": {"type": "string", "description": "Task description"},
                "dayOfWeek": {"type": "string", "enum": ["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"]},
                "startTime": {"type": "string", "description": "Start time in HH:MM format"},
                "endTime": {"type": "string", "description": "End time in HH:MM format"},
                "relativeWeekIndex": {"type": "integer", "description": "0=current week, 1=next week, -1=last week"},
            },
            "required": ["title", "dayOfWeek", "startTime", "endTime", "relativeWeekIndex"]
        }
    },
    {
        "name": "create_goal",
        "description": "Creates a new goal for the user.",
        "parameters": {
            "type": "object",
            "properties": {
                "title": {"type": "string"},
                "type": {"type": "string", "enum": ["Short-Term", "Long-Term"]},
            },
            "required": ["title", "type"]
        }
    },
    {
        "name": "update_task",
        "description": "Updates an existing task by ID.",
        "parameters": {
            "type": "object",
            "properties": {
                "id": {"type": "string", "description": "Task document ID"},
                "title": {"type": "string"},
                "dayOfWeek": {"type": "string", "enum": ["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"]},
                "startTime": {"type": "string"},
                "endTime": {"type": "string"},
                "status": {"type": "string", "enum": ["Pending","Completed","Missed"]},
            },
            "required": ["id"]
        }
    },
    {
        "name": "delete_task",
        "description": "Deletes a task by ID.",
        "parameters": {
            "type": "object",
            "properties": {
                "id": {"type": "string"}
            },
            "required": ["id"]
        }
    },
]


def is_execution_trigger(message: str) -> bool:
    """Check if user message triggers Execution Mode."""
    lower = message.lower().strip()
    return any(phrase in lower for phrase in TRIGGER_PHRASES)


async def brainstorm(message: str, history: list, context: dict) -> str:
    """Handle brainstorming conversation with read-only context."""
    model = genai.GenerativeModel(
        model_name="gemini-2.0-flash",
        system_instruction=BRAINSTORM_SYSTEM_PROMPT.format(context=json.dumps(context, indent=2))
    )
    chat = model.start_chat(history=_format_history(history))
    response = chat.send_message(message)
    return response.text


async def execute_schedule(history: list, context: dict) -> dict:
    """
    Execute mode: parse history, call functions, return list of function calls
    for the router to execute against Firestore.
    """
    model = genai.GenerativeModel(
        model_name="gemini-2.0-flash",
        system_instruction=EXECUTION_SYSTEM_PROMPT,
        tools=[{"function_declarations": TASK_FUNCTIONS}]
    )

    # Build a summary prompt from the history
    history_text = "\n".join([
        f"{m['role'].upper()}: {m['content']}"
        for m in history[-20:]  # Last 20 messages for context
    ])

    prompt = f"""
Based on this conversation, schedule all agreed-upon tasks and goals.
Current user context: {json.dumps(context, indent=2)}

Conversation history:
{history_text}

Now call the appropriate functions to create/update tasks and goals.
"""

    response = model.generate_content(prompt)

    # Extract function calls from response
    function_calls = []
    summary_text = ""

    for part in response.parts:
        if hasattr(part, 'function_call') and part.function_call:
            fc = part.function_call
            function_calls.append({
                "name": fc.name,
                "args": dict(fc.args)
            })
        elif hasattr(part, 'text') and part.text:
            summary_text += part.text

    return {
        "function_calls": function_calls,
        "summary": summary_text
    }


async def generate_inspiration_commentary(ayah_translation: str, hadith_translation: str) -> str:
    """Generate a 2-sentence encouragement from Ayah and Hadith texts."""
    model = genai.GenerativeModel(model_name="gemini-2.0-flash")
    prompt = f"""
You are a warm, wise Islamic scholar and life coach.
Based on this Quranic verse and Hadith, write exactly 2 sentences of personal encouragement.
Make it motivating, practical, and uplifting for someone planning their week.

Quranic Verse (English): {ayah_translation}

Hadith (English): {hadith_translation}

Write 2 sentences only. No headers. No quotes. Just the encouragement.
"""
    response = model.generate_content(prompt)
    return response.text.strip()


def _format_history(history: list) -> list:
    """Format chat history for Gemini's expected format."""
    formatted = []
    for msg in history:
        role = "user" if msg.get("role") == "user" else "model"
        formatted.append({"role": role, "parts": [msg.get("content", "")]})
    return formatted
