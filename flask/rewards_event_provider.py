from firebase_cloudstore_provider import get_firestore_database
from event_constants import *

def get_all_reward_events():
    '''
    Returns { <user_ad_id>: <reward_event_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(REWARDS_COLLECTION).stream()
    reward_dict = {}
    for doc in docs:
        try:
            user_ad_id = doc.get(USER_AD_ID)
            reward_dict[user_ad_id] = doc.to_dict()
        except KeyError:
            print('doc {} does not have ad_id field'.format(doc.id))
    return reward_dict