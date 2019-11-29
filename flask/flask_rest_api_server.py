## flask related
from flask import Flask, jsonify, request
app = Flask('PrivaDroid', static_url_path = '/static')


## change directory for dev purpose
import os
os.chdir('ActivityBuddyBundle/privadroid')

## util
include_test_users = False
from util import filter_out_test_user_data, fill_in_users_that_dont_have_events


## joined users
from user_join_event_provider import get_user_ad_id_to_join_events, \
                                    get_all_real_users_join_events, \
                                    compile_joined_users_by_category_analytics

real_user_ad_id_to_join_events = get_all_real_users_join_events(get_user_ad_id_to_join_events(), include_test_users)
joined_user_analytics = compile_joined_users_by_category_analytics(real_user_ad_id_to_join_events)
@app.route('/joinedusersjson')
def joinedusersjson():
    return jsonify({
        'unique_join_events': real_user_ad_id_to_join_events,
        'join_user_analytics': joined_user_analytics
    })


## demographic
from demographic_event_provider import get_user_ad_id_to_demographic_events, \
                                        compile_demographic_by_category_analytics

user_ad_id_to_demographic_event = filter_out_test_user_data(real_user_ad_id_to_join_events,  get_user_ad_id_to_demographic_events())
demographic_analytics = compile_demographic_by_category_analytics(user_ad_id_to_demographic_event)
@app.route('/demographicjson')
def demographicjson():
    return jsonify({
        'demographic_surveys': user_ad_id_to_demographic_event,
        'demographic_analytics': demographic_analytics
    })


## app install and survey events
from app_install_event_and_survey_provider import get_doc_id_to_app_install_events, \
                                                get_app_install_events_by_user_ad_id, \
                                                get_doc_id_to_app_install_survey_events, \
                                                get_app_install_and_survey_analytics

app_install_events_by_user = filter_out_test_user_data(real_user_ad_id_to_join_events, get_app_install_events_by_user_ad_id())
app_install_analytics = get_app_install_and_survey_analytics(app_install_events_by_user, user_ad_id_to_demographic_event, real_user_ad_id_to_join_events)
@app.route('/appinstallsjson')
def appinstallsjson():
    return jsonify({
        'app_install_events_by_user': app_install_events_by_user,
        'app_install_analytics': app_install_analytics
    })


## app uninstall and survey events
from app_uninstall_event_and_survey_provider import get_doc_id_to_app_install_events,  \
                                                    get_app_uninstall_events_by_user_ad_id, \
                                                    get_doc_id_to_app_uninstall_survey_events, \
                                                    get_app_uninstall_and_survey_analytics

app_uninstall_events_by_user = filter_out_test_user_data(real_user_ad_id_to_join_events, get_app_uninstall_events_by_user_ad_id())
app_uninstall_analytics = get_app_uninstall_and_survey_analytics(app_uninstall_events_by_user, user_ad_id_to_demographic_event, real_user_ad_id_to_join_events)
@app.route('/appuninstallsjson')
def appuninstallsjson():
    return jsonify({
        'app_uninstall_events_by_user': app_uninstall_events_by_user,
        'app_uninstall_analytics': app_uninstall_analytics
    })


## permission and grant/deny survey events and proactive permission events
from permission_event_and_survey_provider import get_doc_id_to_permission_event, \
                                                get_permission_grant_deny_events_by_user, \
                                                get_permission_event_and_survey_analytics

permission_events_by_users = filter_out_test_user_data(real_user_ad_id_to_join_events, get_permission_grant_deny_events_by_user())
permission_events_analytics = get_permission_event_and_survey_analytics(permission_events_by_users, real_user_ad_id_to_join_events, user_ad_id_to_demographic_event)
@app.route('/permissioneventsjson')
def permissioneventsjson():
    return jsonify({
        'permission_events_by_users': permission_events_by_users,
        'permission_events_analytics': permission_events_analytics
    })


## exit survey events
from exit_survey_event_provider import get_user_id_to_exit_survey_event

exit_survey_event_by_user = filter_out_test_user_data(real_user_ad_id_to_join_events, get_user_id_to_exit_survey_event())
@app.route('/exitsurveysjson')
def exitsurveysjson():
    return jsonify({
        'exit_surveys_by_user': exit_survey_event_by_user
    })


## heartbeat events
from heartbeat_event_provider import get_user_ad_id_to_heartbeat_events

heartbeat_events_by_user = filter_out_test_user_data(real_user_ad_id_to_join_events, get_user_ad_id_to_heartbeat_events())
@app.route('/heartbeateventsjson')
def heartbeateventsjson():
    return jsonify({
        'heartbeat_events_by_users': heartbeat_events_by_user
    })


## proactive permission events
from proactive_permission_event_provider import get_all_proactive_permission_events

all_proactive_events_by_users = get_all_proactive_permission_events(None if include_test_users else set(real_user_ad_id_to_join_events.keys()))
@app.route('/proactiveeventsjson')
def proactiveeventsjson():
    return jsonify({
        'proactive_permission_events': all_proactive_events_by_users
    })


## revoke permission notification click events and corresponding permission grant events
from revoke_permission_notification_click_event_provider import get_revoke_permission_by_users

revoke_permission_notification_events_by_user = filter_out_test_user_data(real_user_ad_id_to_join_events, get_revoke_permission_by_users())
@app.route('/revokepermissionnotificationclickeventsjson')
def revokepermissionnotificationclickeventsjson():
    return jsonify({
        'revoke_permission_click_events':  revoke_permission_notification_events_by_user
    })


## local storage sync log events and all the offline sync events
from local_storage_sync_log_event_provider import get_local_storage_sync_log_events_by_user

user_ad_id_to_list_of_offline_sync_log_events = filter_out_test_user_data(real_user_ad_id_to_join_events , get_local_storage_sync_log_events_by_user())
@app.route('/localstoragesynclogeventsjson')
def localstoragesynclogeventsjson():
    return jsonify({
        'local_storage_sync_events': user_ad_id_to_list_of_offline_sync_log_events
    })


## daily active user
from daily_active_user_helper import get_daily_active_user_analytics

daily_active_user_analytics = get_daily_active_user_analytics(app_install_events_by_user, app_uninstall_events_by_user, user_ad_id_to_list_of_offline_sync_log_events, heartbeat_events_by_user, all_proactive_events_by_users, permission_events_by_users, real_user_ad_id_to_join_events, user_ad_id_to_demographic_event, real_user_ad_id_to_join_events)
@app.route('/dailyactiveuserjson')
def dailyactiveuserjson():
    return jsonify({
        'daily_active_users': daily_active_user_analytics
    })


## individual user
@app.route('/userjson')
def userjson():
    user_ad_id = request.args.get('id')
    return jsonify({
        'join_event': real_user_ad_id_to_join_events[user_ad_id],
        'demographic_event': user_ad_id_to_demographic_event[user_ad_id] if user_ad_id in user_ad_id_to_demographic_event.keys() else None,
        'app_install_events': app_install_events_by_user[user_ad_id] if user_ad_id in app_install_events_by_user.keys() else None,
        'app_uninstall_events': app_uninstall_events_by_user[user_ad_id] if user_ad_id in app_uninstall_events_by_user.keys() else None,
        'offline_sync_events': user_ad_id_to_list_of_offline_sync_log_events[user_ad_id] if user_ad_id in user_ad_id_to_list_of_offline_sync_log_events.keys() else None,
        'heartbeat_events': heartbeat_events_by_user[user_ad_id] if user_ad_id in heartbeat_events_by_user.keys() else None,
        'proactive_events': all_proactive_events_by_users[user_ad_id] if user_ad_id in all_proactive_events_by_users.keys() else None,
        'permission_events': permission_events_by_users[user_ad_id] if user_ad_id in permission_events_by_users.keys() else None,
        'daily_active': daily_active_user_analytics['user_active_date'][user_ad_id]
    })


## exit survey mturk
from mturk_exit_survey_provider import calculate_cronbachs_alpha
@app.route('/mturkexitsurveyjson')
def mturkexitsurveyjson():
    from exit_survey_analysis.GoogleFormCronbachsAlpha import GoogleFormCronbachsAlpha
    v1_50_batch = GoogleFormCronbachsAlpha('./exit_survey_analysis/PrivaDroid_Exit_Survey_MTurk_GForm_Responses_V1_50.csv', 1)
    return jsonify({
        'initial_50_with_7_point_scale': calculate_cronbachs_alpha('./exit_survey_analysis/PrivaDroid_Exit_Survey_MTurk_GForm_Responses_V1_50.csv', 1),
        'total_100_with_7_point_scale': calculate_cronbachs_alpha('./exit_survey_analysis/PrivaDroid_Exit_Survey_MTurk_GForm_Responses_V1_100.csv', 1),
        '100_with_5_point_scale': calculate_cronbachs_alpha('./exit_survey_analysis/PrivaDroid_Exit_Survey_MTurk_GForm_Responses_V2.csv', 2)
    })


## start server
if __name__ == '__main__':
    # app.run(host='127.0.0.1', port='5001')

    app.run(host='0.0.0.0', port='5001')