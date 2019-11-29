from event_constants import LOGGED_TIME, USER_AD_ID, COUNTRY_CODE_LIST, GENDER, COUNTRY_CODE
from util import get_datetime_from_iso_or_utc


def handle_user_event_list(event_list, user_active_days, global_earlist_active_date, global_last_active_date, user_ad_id):
    from datetime import datetime, timezone, timedelta
    for event in event_list:
        datetime_obj = get_datetime_from_iso_or_utc(event[LOGGED_TIME])

        if user_active_days[user_ad_id]['earlist_active_date'] == None or \
            user_active_days[user_ad_id]['earlist_active_date'] > datetime_obj:
            user_active_days[user_ad_id]['earlist_active_date'] = datetime_obj
        if user_active_days[user_ad_id]['last_active_date'] == None or \
            user_active_days[user_ad_id]['last_active_date'] < datetime_obj:
            user_active_days[user_ad_id]['last_active_date'] = datetime_obj

        if global_earlist_active_date == None or global_earlist_active_date > datetime_obj:
            global_earlist_active_date = datetime_obj
        if global_last_active_date == None or global_last_active_date < datetime_obj:
            global_last_active_date = datetime_obj

    return user_active_days, global_earlist_active_date, global_last_active_date


def handle_event_doc_id_to_event_dict(id_to_event_dict, user_active_days, global_earlist_active_date, global_last_active_date):
    from datetime import datetime, timezone, timedelta
    for event in id_to_event_dict.values():
        user_ad_id = event[USER_AD_ID]
        if user_ad_id not in user_active_days.keys():
            user_active_days[user_ad_id] = {
                'earlist_active_date': None,
                'last_active_date': None
            }
        datetime_obj = get_datetime_from_iso_or_utc(event[LOGGED_TIME])

        if user_active_days[user_ad_id]['earlist_active_date'] == None or \
            user_active_days[user_ad_id]['earlist_active_date'] > datetime_obj:
            user_active_days[user_ad_id]['earlist_active_date'] = datetime_obj
        if user_active_days[user_ad_id]['last_active_date'] == None or \
            user_active_days[user_ad_id]['last_active_date'] < datetime_obj:
            user_active_days[user_ad_id]['last_active_date'] = datetime_obj

        if global_earlist_active_date == None or global_earlist_active_date > datetime_obj:
            global_earlist_active_date = datetime_obj
        if global_last_active_date == None or global_last_active_date < datetime_obj:
            global_last_active_date = datetime_obj

    return user_active_days, global_earlist_active_date, global_last_active_date


def handle_user_ad_id_to_events_list(user_to_events_list, user_active_days, global_earlist_active_date, global_last_active_date):
    from datetime import datetime, timezone, timedelta
    for user_ad_id in user_to_events_list.keys():
        if user_ad_id not in user_active_days.keys():
            user_active_days[user_ad_id] = {
                'earlist_active_date': None,
                'last_active_date': None
            }
        for event in user_to_events_list[user_ad_id]:
            datetime_obj = get_datetime_from_iso_or_utc(event[LOGGED_TIME])

            if user_active_days[user_ad_id]['earlist_active_date'] == None or \
                user_active_days[user_ad_id]['earlist_active_date'] > datetime_obj:
                user_active_days[user_ad_id]['earlist_active_date'] = datetime_obj
            if user_active_days[user_ad_id]['last_active_date'] == None or \
                user_active_days[user_ad_id]['last_active_date'] < datetime_obj:
                user_active_days[user_ad_id]['last_active_date'] = datetime_obj

            if global_earlist_active_date == None or global_earlist_active_date > datetime_obj:
                global_earlist_active_date = datetime_obj
            if global_last_active_date == None or global_last_active_date < datetime_obj:
                global_last_active_date = datetime_obj

    return user_active_days, global_earlist_active_date, global_last_active_date


def get_daily_active_user_analytics(app_install_event_by_user,
                                    app_uninstall_event_by_user,
                                    local_storage_sync_event_by_user,
                                    heartbeat_event_by_user,
                                    proactive_permission_dict,
                                    permission_grant_deny_event_by_user,
                                    real_user_join_event_by_user,
                                    demographic_event_by_user,
                                    join_event_by_user):
    from datetime import datetime, timezone, timedelta, date

    user_active_days = dict()
    earlist_active_date = None
    last_active_date = None

    # 1. app install
    user_active_days, earlist_active_date, last_active_date = handle_user_ad_id_to_events_list(app_install_event_by_user, user_active_days, earlist_active_date, last_active_date)

    # 2. app uninstall
    user_active_days, earlist_active_date, last_active_date = handle_user_ad_id_to_events_list(app_uninstall_event_by_user, user_active_days, earlist_active_date, last_active_date)

    # 3. local storage
    # user_active_days, earlist_active_date, last_active_date = handle_user_ad_id_to_events_list(local_storage_sync_event_by_user, user_active_days, earlist_active_date, last_active_date)

    # 4. heartbeat
    user_active_days, earlist_active_date, last_active_date = handle_user_ad_id_to_events_list(heartbeat_event_by_user, user_active_days, earlist_active_date, last_active_date)

    # 5. proactive permission
    user_active_days, earlist_active_date, last_active_date = handle_event_doc_id_to_event_dict(proactive_permission_dict, user_active_days, earlist_active_date, last_active_date)

    # 6. permission grant deny
    for user_ad_id in permission_grant_deny_event_by_user.keys():
        if user_ad_id not in user_active_days.keys():
            user_active_days[user_ad_id] = {
                'earlist_active_date': None,
                'last_active_date': None
            }
        user_active_days, earlist_active_date, last_active_date = handle_user_event_list(permission_grant_deny_event_by_user[user_ad_id]['grant'], user_active_days, earlist_active_date, last_active_date, user_ad_id)
        user_active_days, earlist_active_date, last_active_date = handle_user_event_list(permission_grant_deny_event_by_user[user_ad_id]['deny'], user_active_days, earlist_active_date, last_active_date, user_ad_id)

    # create histogram
    total_user_active_counts = dict()
    i = 0
    while earlist_active_date.date() + timedelta(days=i) <= last_active_date.date():
        total_user_active_counts[(earlist_active_date.date() + timedelta(days=i))] = 0
        i += 1
    for date in total_user_active_counts.keys():
        for user_ad_id in user_active_days.keys():
            if user_active_days[user_ad_id]['earlist_active_date'].date() <= date and \
                user_active_days[user_ad_id]['last_active_date'].date() >= date:
                total_user_active_counts[date] += 1
    return_total_user_active_counts = dict()
    for date in total_user_active_counts.keys():
        return_total_user_active_counts[date.strftime('%m/%d/%Y')] = total_user_active_counts[date]

    # create histogram by country and gender
    total_user_active_counts_country_code_gender = dict()
    i = 0
    while earlist_active_date.date() + timedelta(days=i) <= last_active_date.date():
        total_user_active_counts_country_code_gender[(earlist_active_date.date() + timedelta(days=i))] = {}
        for country_code in COUNTRY_CODE_LIST:
            total_user_active_counts_country_code_gender[(earlist_active_date.date() + timedelta(days=i))][country_code] = {
                'female': 0,
                'male': 0,
                'have not answered': 0,
                'other': 0,
                'prefer not to say':0
            }
        i += 1
    for date in total_user_active_counts_country_code_gender.keys():
        for user_ad_id in user_active_days.keys():
            if user_active_days[user_ad_id]['earlist_active_date'].date() <= date and \
                user_active_days[user_ad_id]['last_active_date'].date() >= date:
                # if user answered demographic
                gender = None
                if user_ad_id in demographic_event_by_user.keys():
                    gender = demographic_event_by_user[user_ad_id][GENDER].lower()
                else:
                    gender = 'have not answered'
                country_code = join_event_by_user[user_ad_id][COUNTRY_CODE]
                if not country_code or country_code == '' or not country_code in total_user_active_counts_country_code_gender[date].keys():
                    continue
                total_user_active_counts_country_code_gender[date][country_code][gender] += 1
    return_total_user_active_counts_country_code_gender = dict()
    for country_code in COUNTRY_CODE_LIST:
        return_total_user_active_counts_country_code_gender[country_code] = dict()
        for date in total_user_active_counts_country_code_gender.keys():
            return_total_user_active_counts_country_code_gender[country_code][date.strftime('%m/%d/%Y')] = total_user_active_counts_country_code_gender[date][country_code]

    # count number of users stayed from start to today
    user_stayed_all_along_count = dict()
    today_datetime = date.today()
    for user_ad_id in user_active_days.keys():
        start = user_active_days[user_ad_id]['earlist_active_date'].date()
        end = user_active_days[user_ad_id]['last_active_date'].date()
        stay = (end - start).days + 1
        if stay not in user_stayed_all_along_count.keys():
            user_stayed_all_along_count[stay] = 0
        user_stayed_all_along_count[stay] += 1

    # update user active to date only
    for user_ad_id in user_active_days.keys():
        user_active_days[user_ad_id]['earlist_active_date'] = user_active_days[user_ad_id]['earlist_active_date'].strftime('%m/%d/%Y')
        user_active_days[user_ad_id]['last_active_date'] = user_active_days[user_ad_id]['last_active_date'].strftime('%m/%d/%Y')

    # join date
    user_join_date = dict()
    for user_ad_id in real_user_join_event_by_user.keys():
        join_date = get_datetime_from_iso_or_utc(real_user_join_event_by_user[user_ad_id][LOGGED_TIME]).date().strftime('%m/%d/%Y')
        if join_date not in user_join_date.keys():
            user_join_date[join_date] = 0
        user_join_date[join_date] += 1

    return {
        'earliest_active_date': earlist_active_date.strftime('%m/%d/%Y'),
        'last_active_date': last_active_date.strftime('%m/%d/%Y'),
        'user_active_date': user_active_days,
        'total_user_active_counts': return_total_user_active_counts,
        'total_user_active_counts_country_code_gender': return_total_user_active_counts_country_code_gender,
        'how_long_users_stay': user_stayed_all_along_count,
        'join_date': user_join_date
    }