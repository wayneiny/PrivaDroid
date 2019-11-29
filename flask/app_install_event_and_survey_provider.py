from firebase_cloudstore_provider import get_firestore_database
from event_constants import *
from util import *
from event_analytics import get_number_of_events_by_user_across_android_versions

## app install events
def get_doc_id_to_app_install_events():
    '''
    Get a server doc id to event dict map.
    Return { <doc.id>: <app_install_event_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(APP_INSTALL_COLLECTION).stream()
    app_install_dict = {}
    for doc in docs:
        app_install_dict[doc.id] = add_server_doc_id_to_doc_dict(doc)
    return app_install_dict


def get_app_install_events_by_user_ad_id():
    '''
    Get a user ad id to a list of app install events map.
    Returns { <user_ad_id>: [ <app_install_event_python_dict> ] }
    '''
    db = get_firestore_database()
    docs = db.collection(APP_INSTALL_COLLECTION).stream()
    app_install_dict = {}
    for doc in docs:
        try:
            user_ad_id = doc.get(USER_AD_ID)
            if user_ad_id not in app_install_dict.keys():
                app_install_dict[user_ad_id] = list()
            app_install_dict[user_ad_id].append(add_server_doc_id_to_doc_dict(doc))
        except KeyError:
            print('app install doc {} does not have {} field'.format(doc.id, \
                USER_AD_ID))
    app_install_dict = link_survey_dict_to_event_dict(app_install_dict, \
        get_doc_id_to_app_install_survey_events())
    return app_install_dict


## app install survey events
def get_doc_id_to_app_install_survey_events():
    '''
    Get a server doc id to event dict map.
    Returns { <doc.id>: <app_install_survey_event_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(APP_INSTALL_SURVEY_COLLECTION).stream()
    app_install_survey_dict = {}
    for doc in docs:
        app_install_survey_dict[doc.id] = add_server_doc_id_to_doc_dict(doc)
    return app_install_survey_dict


## analytics
def get_app_install_and_survey_analytics(app_install_events_by_user={},
                                            user_ad_id_to_demographic_event={},
                                            real_user_ad_id_to_join_event={}):
    # 1. how many events were surveyed and percentage (total, across demo groups)
    # 2. average install events per user (total, across demo groups)
    # 3. most popular install reasons (total, across demo groups)
    # 4. most popular install factors (total, across demo groups)
    # 5. the percentage of people knowing the permissions requested by the app (total, across different demographic groups)
    # 6. number of app installs across different android versions (validate app)
    # 7. the most popular guessed permission requested by app (total, across different demographic groups)
    result = {}

    # 6.
    result['number_of_installs_across_android_versions'] = get_number_of_events_by_user_across_android_versions(app_install_events_by_user, real_user_ad_id_to_join_event)

    return result
