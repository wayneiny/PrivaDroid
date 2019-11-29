from event_constants import ANDROID_VERSION

def get_number_of_events_by_user_across_android_versions(events_by_user={},
                                                        real_user_ad_id_to_join_event={}):
    result = {}
    for user_ad_id in events_by_user.keys():
        join_event = real_user_ad_id_to_join_event[user_ad_id]
        android_version = join_event[ANDROID_VERSION]
        if android_version not in result.keys():
            result[android_version] = 0
        result[android_version] += len(events_by_user[user_ad_id])
    return result