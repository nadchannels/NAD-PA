"""
Firebase Firestore client wrapper.
Provides a singleton Firestore client initialized with Firebase Admin SDK.
"""
import os
import firebase_admin
from firebase_admin import credentials, firestore
from dotenv import load_dotenv

load_dotenv()

_db = None


def get_db():
    """Return a singleton Firestore client."""
    global _db
    if _db is None:
        cred_path = os.getenv("FIREBASE_CREDENTIALS_PATH", "firebase-credentials.json")
        if not firebase_admin._apps:
            cred = credentials.Certificate(cred_path)
            firebase_admin.initialize_app(cred)
        _db = firestore.client()
    return _db
