from firebase_cloudstore_provider import get_firestore_database
from event_constants import *
from util import *

## events
def get_user_ad_id_to_heartbeat_events():
    '''
    Returns { <user_ad_id>: [ <heartbeat_event_python_dict> ] }
    '''
    db = get_firestore_database()
    docs = db.collection(HEARTBEAT_COLLECTION).stream()
    heartbeat_dict = {}
    for doc in docs:
        try:
            user_ad_id = doc.get(USER_AD_ID)
            if user_ad_id not in heartbeat_dict:
                heartbeat_dict[user_ad_id] = list()
            heartbeat_dict[user_ad_id].append(add_server_doc_id_to_doc_dict(doc))
        except KeyError:
            print('heartbeat doc {} does not have {} field'.format(doc.id, USER_AD_ID))
    return heartbeat_dict