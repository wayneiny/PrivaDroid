from firebase_cloudstore_provider import get_firestore_database
from event_constants import *
from util import *

def get_all_proactive_permission_events(real_user_set=None):
    '''
    Returns { <event_correlation_id>: <proactive_permission_event_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(PROACTIVE_RATIONALE_COLLECTION).stream()
    proactive_permission_dict = {}
    for doc in docs:
        try:
            if real_user_set:
                if not doc.get(USER_AD_ID) in real_user_set:
                    continue
            proactive_permission_dict[doc.get(PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID)] = add_server_doc_id_to_doc_dict(doc)
        except KeyError:
            print('proactive permission event id={} does not have a {} field'.format(doc.id, PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID))
    return proactive_permission_dict


def link_proactive_permission_event_to_permission_event_dict(permission_event_dict={}, proactive_permission_event_dict={}):
    '''
    Link survey to actual event.
    permission_event_dict = { <user_ad_id>: [ <event_python_dict> ] }
    proactive_permission_event_dict = { <server_correlation_id>: <proactive_server_event_dict> }
    '''
    result = {}
    for user_ad_id in permission_event_dict.keys():
        if user_ad_id not in result:
            result[user_ad_id] = list()
        user_events = permission_event_dict[user_ad_id]
        for event in user_events:
            try:
                # [PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID] could be null or ''
                if PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID in event.keys() and \
                    event[PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID] != None and \
                    event[PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID].strip():
                    event[PROACTIVE_DICT_IN_PERMISSION_EVENT_OBJECT] = proactive_permission_event_dict[event[PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID]]
                else:
                    event[PROACTIVE_DICT_IN_PERMISSION_EVENT_OBJECT] = None
            except KeyError:
                print('cannot find proactive permission event with correlation_event_id={}'.format(event[PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID]))
            result[user_ad_id].append(event)
    return result