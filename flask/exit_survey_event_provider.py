from firebase_cloudstore_provider import get_firestore_database
from event_constants import *
from util import *

def get_user_id_to_exit_survey_event():
    '''
    Returns { <user_ad_id>: <exit_survey_event_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(EXIT_SURVEY_COLLECTION).stream()
    exit_dict = {}
    for doc in docs:
        try:
            user_ad_id = doc.get(USER_AD_ID)
            exit_dict[user_ad_id] = add_server_doc_id_to_doc_dict(doc)
        except KeyError:
            print('exit survey doc {} does not have ad_id field'.format(doc.id))
    return exit_dict