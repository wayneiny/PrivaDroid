from firebase_cloudstore_provider import get_firestore_database
from event_constants import *
from util import *


## server data
def get_user_ad_id_to_join_events():
    '''
    Returns { <user_ad_id>: <python_dict_of_an_join_event> }
    '''
    db = get_firestore_database()
    docs = db.collection(JOIN_EVENT_COLLECTION).stream()
    join_dict = {}
    for doc in docs:
        try:
            join_dict[doc.get(USER_AD_ID)] = add_server_doc_id_to_doc_dict(doc)
        except KeyError:
            print('doc {} does not have ad_id field'.format(doc.id))
    return join_dict


def get_all_real_users_join_events(all_join_events={}, include_test_users=True):
    '''
    Returns { <user_ad_id>: <python_dict_of_an_join_event> }
    '''
    from datetime import datetime, timezone
    # 1. first test run
    BLACKLIST_AD_ID = {
        u'f1f09e6e-3d83-46fa-9945-f5e532a1a712' # Nexus 6.0.1
    }
    TEST_USER_JOIN_DATE_CUTOFF = datetime.fromisoformat('2019-10-29T07:50:18.137+05:30')

    result = {}
    for doc in all_join_events.values():
        if doc[USER_AD_ID] in BLACKLIST_AD_ID or 'Android' in doc[CARRIER]:
            continue
        # 2. real users after Oct 30th
        if not include_test_users:
            # check if date is in iso format
            if not is_in_utc(doc[LOGGED_TIME]):
                if datetime.fromisoformat(doc[LOGGED_TIME]) < TEST_USER_JOIN_DATE_CUTOFF:
                    continue
            else:
                # date is in UTC (UK)
                if datetime.strptime(doc[LOGGED_TIME], '%Y-%m-%dT%H:%M:%S.%fZ').replace(tzinfo=timezone.utc) < TEST_USER_JOIN_DATE_CUTOFF:
                    continue
        result[doc[USER_AD_ID]] = doc

    return result


## analytics
def compile_joined_users_by_category_analytics(all_real_user_join_events={}):
    '''
    Returns { <category>: { <category_value>: <analytics> } }
    '''
    result = {}
    result[ANDROID_VERSION] = events_by_category(all_real_user_join_events, ANDROID_VERSION)
    result[CARRIER] = events_by_category(all_real_user_join_events, CARRIER)
    result[COUNTRY_CODE] = events_by_category(all_real_user_join_events, COUNTRY_CODE)
    result[PHONE_LOCALE] = events_by_category(all_real_user_join_events, PHONE_LOCALE)
    result[PHONE_MAKE] = events_by_category(all_real_user_join_events, PHONE_MAKE)
    result[PHONE_MODEL] = events_by_category(all_real_user_join_events, PHONE_MODEL)
    result[PARTICIPATE_WITH_NO_PAY] = events_by_category(all_real_user_join_events, PARTICIPATE_WITH_NO_PAY)
    result['total_number_of_joined_users'] = len(all_real_user_join_events)

    # join date
    from datetime import datetime
    user_join_date = dict()
    for user_ad_id in all_real_user_join_events.keys():
        join_date = get_datetime_from_iso_or_utc(all_real_user_join_events[user_ad_id][LOGGED_TIME]).date().strftime('%m/%d/%Y')
        if join_date not in user_join_date.keys():
            user_join_date[join_date] = 0
        user_join_date[join_date] += 1
    result['join_date'] = user_join_date

    return result