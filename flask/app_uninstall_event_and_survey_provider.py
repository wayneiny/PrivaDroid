from firebase_cloudstore_provider import get_firestore_database
from event_constants import *
from util import *
from event_analytics import get_number_of_events_by_user_across_android_versions


## app uninstall events
def get_doc_id_to_app_install_events():
    '''
    Get a server doc id to event dict map.
    Return { <doc.id>: <app_install_event_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(APP_UNINSTALL_COLLECTION).stream()
    app_uninstall_dict = {}
    for doc in docs:
        app_uninstall_dict[doc.id] = add_server_doc_id_to_doc_dict(doc)
    return app_uninstall_dict


def get_app_uninstall_events_by_user_ad_id():
    '''
    Returns { <user_ad_id>: [ <app_uninstall_event_python_dict> ] }
    '''
    db = get_firestore_database()
    docs = db.collection(APP_UNINSTALL_COLLECTION).stream()
    app_uninstall_dict = {}
    for doc in docs:
        try:
            user_ad_id = doc.get(USER_AD_ID)
            if user_ad_id not in app_uninstall_dict:
                app_uninstall_dict[user_ad_id] = list()
            app_uninstall_dict[user_ad_id].append(add_server_doc_id_to_doc_dict(doc))
        except KeyError:
            print('app uninstall doc {} does not have {} field'.format(doc.id, \
                USER_AD_ID))
    app_uninstall_dict = link_survey_dict_to_event_dict(app_uninstall_dict, \
        get_doc_id_to_app_uninstall_survey_events())
    return app_uninstall_dict


## app uninstall survey events
def get_doc_id_to_app_uninstall_survey_events():
    '''
    Get a server doc id to event dict map.
    Returns { <doc.id>: <app_install_survey_event_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(APP_UNINSTALL_SURVEY_COLLECTION).stream()
    app_uninstall_survey_dict = {}
    for doc in docs:
        app_uninstall_survey_dict[doc.id] = add_server_doc_id_to_doc_dict(doc)
    return app_uninstall_survey_dict


## analytics
def get_app_uninstall_and_survey_analytics(app_uninstall_events_by_user={},
                                            user_ad_id_to_demographic_event={},
                                            real_user_ad_id_to_join_event={}):
    # 1. how many events were surveyed and percentage (total, across demo groups)
    # 2. average uninstall events per user (total, across demo groups)
    # 3. most popular uninstall reasons (total, across demo groups)
    # 4. number of app uninstalls across different android versions (validate app)

    result = {}

    # 4.
    result['number_of_uninstalls_across_android_versions'] = get_number_of_events_by_user_across_android_versions(app_uninstall_events_by_user, real_user_ad_id_to_join_event)

    return result