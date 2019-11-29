from firebase_cloudstore_provider import get_firestore_database
from event_constants import *
from util import *

## events
def get_revoke_permission_by_users():
    '''
    Returns { <server_doc_id>: <revoke_permission_click_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(REVOKE_PERMISSION_NOTIFICATION_CLICK_COLLECTION).stream()
    revoke_dict = {}
    for doc in docs:
        revoke_dict[doc.get(GRANT_SURVEY_SERVER_ID)] = add_server_doc_id_to_doc_dict(doc)

    from permission_event_and_survey_provider import get_all_permission_grant_survey_events
    permission_grant_survey_dict = get_all_permission_grant_survey_events()

    # find all permission grant survey by users
    user_grant_surveys = dict()
    for event in permission_grant_survey_dict.values():
        if event[USER_AD_ID] not in user_grant_surveys.keys():
            user_grant_surveys[event[USER_AD_ID]] = list()
        if event[SERVER_DOC_ID] in revoke_dict.keys():
            event[REVOKE_PERMISSION_EVENT_OBJECT] = revoke_dict[event[SERVER_DOC_ID]]
        else:
            event[REVOKE_PERMISSION_EVENT_OBJECT] = None
        user_grant_surveys[event[USER_AD_ID]].append(event)

    return user_grant_surveys