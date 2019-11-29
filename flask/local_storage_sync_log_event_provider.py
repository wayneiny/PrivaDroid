from firebase_cloudstore_provider import get_firestore_database
from event_constants import *
from util import *


## events
def get_local_storage_sync_log_events_by_user():
    '''
    Returns { <user_ad_id>: [ <local_storage_sync_log_python_dict> ] }
    '''
    db = get_firestore_database()
    docs = db.collection(LOCAL_STORAGE_SYNC_LOG_COLLECTION).stream()
    local_storage_sync_dict = {}
    for doc in docs:
        try:
            user_ad_id = doc.get(USER_AD_ID)
            if user_ad_id not in local_storage_sync_dict.keys():
                local_storage_sync_dict[user_ad_id] = list()
            local_storage_sync_dict[user_ad_id].append(add_server_doc_id_to_doc_dict(doc))
        except KeyError:
            print('app install doc {} does not have {} field'.format(doc.id, USER_AD_ID))
    return local_storage_sync_dict