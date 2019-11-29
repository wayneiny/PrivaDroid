import os
import firebase_admin
from firebase_admin import credentials

from firebase_admin import firestore
cred = credentials.Certificate('./privadroidproductiontest-firebase-adminsdk-w6jyo-c066a56190.json')
firebase_admin.initialize_app(cred)

def get_firestore_database():
    db = firestore.client()
    return db