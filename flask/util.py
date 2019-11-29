## server event
def add_server_doc_id_to_doc_dict(doc):
    from event_constants import SERVER_DOC_ID
    doc_dict = doc.to_dict()
    doc_dict[SERVER_DOC_ID] = doc.id
    return doc_dict


def link_survey_dict_to_event_dict(user_events_dict={}, surveys_dict={}):
    '''
    Link survey to actual event.
    user_events_dict = { <user_ad_id>: [ <event_python_dict> ] }
    surveys_dict = { <server_event_id>: <survey_server_event_dict> }
    '''
    from event_constants import SURVEY_DICT_IN_EVENT_OBJECT, SURVEY_ID, SERVER_DOC_ID
    result = {}
    for user_ad_id in user_events_dict.keys():
        if user_ad_id not in result:
            result[user_ad_id] = list()
        user_events = user_events_dict[user_ad_id]
        for event in user_events:
            try:
                if event[SURVEY_ID].strip():
                    event[SURVEY_DICT_IN_EVENT_OBJECT] = surveys_dict[event[SURVEY_ID]]
                else:
                    event[SURVEY_DICT_IN_EVENT_OBJECT] = None
                result[user_ad_id].append(event)
            except KeyError:
                print('cannot find survey id={} for event id={}'.format(event[SURVEY_ID], event[SERVER_DOC_ID]))
    return result


def fill_in_users_that_dont_have_events(user_events_dict={}, all_user_join_events={}):
    for user_ad_id in all_user_join_events.keys():
        if user_ad_id not in user_events_dict.keys():
            user_events_dict[user_ad_id] = None
    return user_events_dict


def filter_out_test_user_data(real_user_join_events={}, to_be_filtered_dict={}):
    '''
    Input { <user_ad_id>: [ <python_dict_of_an_event> ] } or
            { <user_ad_id>: <python_dict_of_an_event> }

    Returns { <user_ad_id>: [ <python_dict_of_an_event> ] } or
            { <user_ad_id>: <python_dict_of_an_event> }
    '''
    result = {}
    for user_ad_id in to_be_filtered_dict.keys():
        if user_ad_id in real_user_join_events.keys():
            result[user_ad_id] = to_be_filtered_dict[user_ad_id]
    return result


## analytics
def events_by_category(events={}, category=''):
    '''
    Returns { <category>: { <analytics> } }
    '''
    from event_constants import SERVER_DOC_ID, MISSING_CATEGORY_UNKNOWN
    result = {}
    for event in events.values():
        try:
            value = event[category].lower().strip()
            if value not in result:
                result[value] = 0
            result[value] += 1
        except KeyError:
            print('join event doc {} does not have an {} field'.format(event[SERVER_DOC_ID], category))
            if MISSING_CATEGORY_UNKNOWN not in result:
                result[MISSING_CATEGORY_UNKNOWN] = 0
            result[MISSING_CATEGORY_UNKNOWN] += 1
    return result


## datetime
def is_in_utc(datetime_string):
    return 'Z' in datetime_string

def get_datetime_from_iso_or_utc(datetime_string):
    from datetime import datetime, timezone
    if is_in_utc(datetime_string):
        return datetime.strptime(datetime_string, '%Y-%m-%dT%H:%M:%S.%fZ').replace(tzinfo=timezone.utc)
    else:
        return datetime.fromisoformat(datetime_string)