from firebase_cloudstore_provider import get_firestore_database
from event_constants import *
from util import *


## events
def get_user_ad_id_to_demographic_events():
    '''
    Returns { <user_ad_id>: <demographic_event_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(DEMOGRAPHIC_COLLECTION).stream()
    demographic_dict = {}
    for doc in docs:
        try:
            user_ad_id = doc.get(USER_AD_ID)
            demographic_dict[user_ad_id] = doc.to_dict()
        except KeyError:
            print('demographic doc {} does not have {} field'.format(doc.id, USER_AD_ID))
    return demographic_dict


## analytics
def compile_demographic_by_category_analytics(all_demographic_events={}):
    '''
    Returns { <category>: { <category_value>: <analytics> } }
    '''
    result = {}
    result[AGE] = events_by_category(all_demographic_events, AGE)
    result[COUNTRY] = events_by_category(all_demographic_events, COUNTRY)
    result[DAILY_USAGE] = events_by_category(all_demographic_events, DAILY_USAGE)
    result[EDUCATION] = events_by_category(all_demographic_events, EDUCATION)
    result[GENDER] = events_by_category(all_demographic_events, GENDER)
    result[INCOME] = events_by_category(all_demographic_events, INCOME)
    result[INDUSTRY] = events_by_category(all_demographic_events, INDUSTRY)
    result[STATUS] = events_by_category(all_demographic_events, STATUS)
    result['total_number_of_users_answered'] = len(all_demographic_events)
    return result