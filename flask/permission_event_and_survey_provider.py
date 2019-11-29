from firebase_cloudstore_provider import get_firestore_database
from event_constants import *
from util import *
from proactive_permission_event_provider import *

## events
def get_doc_id_to_permission_event():
    db = get_firestore_database()
    docs = db.collection(PERMISSION_COLLECTION).stream()
    permission_dict = {}
    for doc in docs:
        permission_dict[doc.id] = add_server_doc_id_to_doc_dict(doc)
    return permission_dict


def filter_out_repetitive_permission_events(user_ad_id_to_permission_events={}):
    '''
    user_ad_id_to_permission_events: { <user_ad_id>: [ <permission_event> ] }

    The problem is that some permission events are logged multiple times. It's
    because of race condition in Android system.
    '''
    def find_event_in_interval_list(list_of_events=[]):
        # 1. check if any permission event has survey id, if so, return that event
        for event in list_of_events:
            if event[SURVEY_ID] and event[SURVEY_ID] != '':
                return event

        # 2. find the longest previous_screen_text one
        max_len_previous_context_index = -1
        max_len_previous_context = -1
        for i in range(len(list_of_events)):
            event = list_of_events[i]
            if PREVIOUS_SCREEN_CONTEXT in event and \
                event[PREVIOUS_SCREEN_CONTEXT] and \
                len(event[PREVIOUS_SCREEN_CONTEXT]) > max_len_previous_context:
                max_len_previous_context_index = i

        if max_len_previous_context_index == -1:
            return list_of_events[0]

        return list_of_events[max_len_previous_context_index]

    import datetime
    import json
    result = {}
    for user_ad_id in user_ad_id_to_permission_events.keys():
        result[user_ad_id] = list()
        # create a dictionary with key being package_name + granted + user_initiated + permission_requested
        permission_dict = dict()
        for event in user_ad_id_to_permission_events[user_ad_id]:
            package_name = event[PACKAGE_NAME]
            user_initiated = event[INITIATED_BY_USER]
            granted = event[GRANTED]
            permission_name = event[PERMISSION_REQUESTED_NAME]
            key = package_name + granted + user_initiated + permission_name
            if key not in permission_dict.keys():
                permission_dict[key] = list()
            permission_dict[key].append(event)
        # print(json.dumps(permission_dict, indent=4, sort_keys=True))

        for compound_key in permission_dict.keys():
            # 1. if only one event is in the list, then don't need to filter it
            if len(permission_dict[compound_key]) == 1:
                result[user_ad_id].append(permission_dict[compound_key][0])
            # 2. if multiple events in the list, need to check the time, proactive permission request and previous screen context
            else:
                sorted_dict_by_logged_time = dict()
                for event in permission_dict[compound_key]:
                    logged_time = event[LOGGED_TIME]
                    # add logged time and event into sorted dict
                    sorted_dict_by_logged_time[logged_time] = event
                # create interval dictionary
                interval_start_date_key = None
                current_interval_events = list()
                for sorted_key in sorted(sorted_dict_by_logged_time.keys()):
                    if interval_start_date_key == None:
                        interval_start_date_key = sorted_key
                        current_interval_events.append(sorted_dict_by_logged_time[sorted_key])
                        continue
                    interval_start_datetime = get_datetime_from_iso_or_utc(interval_start_date_key)
                    current_datetime = get_datetime_from_iso_or_utc(sorted_key)
                    interval_end_datetime_should_be = interval_start_datetime + datetime.timedelta(seconds=30)

                    # if outside of the interval, find the event that need to be included in the result
                    if current_datetime > interval_end_datetime_should_be:
                        result[user_ad_id].append(find_event_in_interval_list(current_interval_events))
                        interval_start_date_key = sorted_key
                        current_interval_events = list()
                        current_interval_events.append(sorted_dict_by_logged_time[sorted_key])
                    # if inside the interval, add to current interval events list
                    else:
                        current_interval_events.append(sorted_dict_by_logged_time[sorted_key])
                # find the event in the last current_interval_events
                result[user_ad_id].append(find_event_in_interval_list(current_interval_events))

    return result


def get_permission_grant_deny_events_by_user():
    '''
    Returns { <user_ad_id>: [ <permission_event_python_dict> ] }
    '''
    db = get_firestore_database()
    docs = db.collection(PERMISSION_COLLECTION).stream()
    permission_dict = {}
    for doc in docs:
        try:
            user_ad_id = doc.get(USER_AD_ID)
            if user_ad_id not in permission_dict:
                permission_dict[user_ad_id] = list()
            permission_dict[user_ad_id].append(add_server_doc_id_to_doc_dict(doc))
        except KeyError:
            print('permission event doc {} does not have {} field'.format(doc.id, USER_AD_ID))

    # filter out repetitive permission event
    permission_dict = filter_out_repetitive_permission_events(permission_dict)

    all_permission_surveys = get_all_permission_grant_survey_events()
    all_permission_surveys.update(get_all_permission_deny_survey_events())
    permission_dict = link_survey_dict_to_event_dict(permission_dict, all_permission_surveys)
    permission_dict = link_proactive_permission_event_to_permission_event_dict(permission_dict, get_all_proactive_permission_events())

    # divide grant and deny events
    result = {}
    for user_ad_id in permission_dict.keys():
        result[user_ad_id] = {
            'grant': [],
            'deny': []
        }
        for event in permission_dict[user_ad_id]:
            if event[GRANTED] == 'true':
                result[user_ad_id]['grant'].append(event)
            else:
                result[user_ad_id]['deny'].append(event)
    return result


def get_all_permission_grant_survey_events():
    '''
    Returns { <server_event_id>: <permission_grant_survey_event_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(PERMISSION_GRANT_SURVEY_COLLECTION).stream()
    permission_grant_dict = {}
    for doc in docs:
        permission_grant_dict[doc.id] = add_server_doc_id_to_doc_dict(doc)
    return permission_grant_dict


def get_all_permission_deny_survey_events():
    '''
    Returns { <server_event_id>: <permission_grant_survey_event_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(PERMISSION_DENY_SURVEY_COLLECTION).stream()
    permission_deny_dict = {}
    for doc in docs:
        permission_deny_dict[doc.id] = add_server_doc_id_to_doc_dict(doc)
    return permission_deny_dict


## analytics
def get_permission_event_and_survey_analytics(permission_events_by_user,
                                            real_user_ad_id_to_join_event,
                                            user_ad_id_to_demographic_event):
    # 1. number of permission events by android versions (to validate app)
    result = {}

    # 1.
    permission_number_by_android_versions = {}
    for user_ad_id in permission_events_by_user.keys():
        join_event = real_user_ad_id_to_join_event[user_ad_id]
        android_version = join_event[ANDROID_VERSION]
        if android_version not in permission_number_by_android_versions.keys():
            permission_number_by_android_versions[android_version] = 0
        permission_number_by_android_versions[android_version] += \
            len(permission_events_by_user[user_ad_id]['grant']) + \
            len(permission_events_by_user[user_ad_id]['deny'])
    result['number_of_permission_events_by_android_versions'] = permission_number_by_android_versions
    return result