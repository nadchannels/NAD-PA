"""
Relative Time Engine — Weekly cron job.
Every Sunday at 23:59:59: decrement all task relativeWeekIndex values by 1.
This ensures WEEK 0 always reflects the current week.
"""
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from services.firebase_service import get_db
from datetime import datetime, timezone

ARCHIVE_THRESHOLD = -100  # Tasks older than 100 weeks are archived


def run_week_shift():
    """
    Core logic for the weekly relativeWeekIndex shift.
    Decrements all task relativeWeekIndex values by 1.
    Tasks that fall below ARCHIVE_THRESHOLD are deleted (too old).
    """
    db = get_db()
    tasks_ref = db.collection("tasks")
    all_tasks = list(tasks_ref.stream())

    updated_count = 0
    archived_count = 0
    now = datetime.now(timezone.utc).isoformat()

    for doc in all_tasks:
        data = doc.to_dict()
        current_index = data.get("relativeWeekIndex", 0)
        new_index = current_index - 1

        if new_index < ARCHIVE_THRESHOLD:
            # Archive: delete tasks older than 100 weeks
            doc.reference.delete()
            archived_count += 1
        else:
            doc.reference.update({
                "relativeWeekIndex": new_index,
                "updatedAt": now
            })
            updated_count += 1

    print(f"[Week Shift] {datetime.now(timezone.utc).isoformat()}")
    print(f"  Updated: {updated_count} tasks (relativeWeekIndex decremented by 1)")
    print(f"  Archived/Deleted: {archived_count} tasks (exceeded -{abs(ARCHIVE_THRESHOLD)} week threshold)")
    return {"updated": updated_count, "archived": archived_count}


if __name__ == "__main__":
    run_week_shift()
