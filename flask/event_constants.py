## common
USER_AD_ID = u'ad_id'
PRIVADROID_VERSION = u'privadroid_version'
LOGGED_TIME = u'logged_time'
OFFLINE_SYNC = u'offline_sync'
EVENT_SERVER_ID = u'event_server_id'
OPTION_DELIMITER = u', '
SURVEY_ID = u'survey_id'

## join event
JOIN_EVENT_COLLECTION = u'JOIN_EVENT_COLLECTION'
ANDROID_VERSION = u'android_version'
CARRIER = u'carrier'
PHONE_MAKE = u'make'
PHONE_MODEL = u'model'
COUNTRY_CODE = u'country_code'
PHONE_LOCALE = u'locale'
PARTICIPATE_WITH_NO_PAY = u'participate_with_no_pay'

## app install/uninstall event
APP_INSTALL_COLLECTION = u'APP_INSTALL_COLLECTION'
APP_UNINSTALL_COLLECTION = u'APP_UNINSTALL_COLLECTION'
APP_NAME = u'app_name'
APP_VERSION = u'app_version'
PACKAGE_NAME = u'package_name'

## app install survey event
APP_INSTALL_SURVEY_COLLECTION = u'APP_INSTALL_SURVEY_COLLECTION'
WHY_INSTALL = u'why_install'
KNOW_PERMISSION_REQUIRED = u'know_permission_required'
INSTALL_FACTORS = u'install_factors'
PERMISSIONS_THINK_REQUIRED = u'permission_think_required'

## app uninstall survey event
APP_UNINSTALL_SURVEY_COLLECTION = u'APP_UNINSTALL_SURVEY_COLLECTION'
WHY_UNINSTALL = u'why_uninstall'
PERMISSION_REMEMBERED_REQUESTED = u'permission_remembered_requested'

## permission event
PERMISSION_COLLECTION = u'PERMISSION_COLLECTION'
PERMISSION_REQUESTED_NAME = u'permission_requested'
GRANTED = u'granted'
INITIATED_BY_USER = u'user_initiated'
PREVIOUS_SCREEN_CONTEXT = u'previous_screen_context'
PACKAGE_TOTAL_FOREGROUND_TIME = u'package_total_foreground_time'
PACKAGE_RECENT_FOREGROUND_TIME = u'package_recent_foreground_time'
PERMISSION_DIALOG_READ_TIME = u'permission_dialog_read_time'

## proactive permission event
PROACTIVE_RATIONALE_COLLECTION = u'PROACTIVE_RATIONALE_COLLECTION'
PROACTIVE_RATIONALE_MESSAGE = u'rationale_message'
PROACTIVE_REQUEST_GRANTED = u'granted'
PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID = u'event_correlation_id'

## permission grant/deny survey event
PERMISSION_GRANT_SURVEY_COLLECTION = u'PERMISSION_GRANT_SURVEY_COLLECTION'
PERMISSION_DENY_SURVEY_COLLECTION = u'PERMISSION_DENY_SURVEY_COLLECTION'
WHY_GRANT = u'why_grant'
WHY_DENY = u'why_deny'
EXPECTED_PERMISSION_REQUEST = u'expected_request'
COMFORT_LEVEL = u'comfort_level'

## demographic event
DEMOGRAPHIC_COLLECTION = u'DEMOGRAPHIC_COLLECTION'
EDUCATION = u'education'
INCOME = u'income'
AGE = u'age'
GENDER = u'gender'
INDUSTRY = u'industry'
DAILY_USAGE = u'daily_usage'
STATUS = u'status'
COUNTRY = u'country'

## reward event
REWARDS_COLLECTION = u'REWARDS_COLLECTION'
REWARDS_METHOD = u'rewards_method'
REWARDS_METHOD_VALUE = u'method_value'
REWARDS_JOIN_DATE = u'join_date'

## exit survey event
EXIT_SURVEY_COLLECTION = u'EXIT_SURVEY_COLLECTION'
CONTROL_ONE = u'control_q1';
CONTROL_TWO = u'control_q2'
CONTROL_THREE = u'control_q3'
AWARENESS_ONE = u'awareness_q1'
AWARENESS_TWO = u'awareness_q2'
AWARENESS_THREE = u'awareness_q3'
COLLECTION_ONE = u'collection_q1'
COLLECTION_TWO = u'collection_q2'
COLLECTION_THREE = u'collection_q3'
ERROR_ONE = u'error_q1'
ERROR_TWO = u'error_q2'
ERROR_THREE = u'error_q3'
ERROR_FOUR = u'error_q4'
SECONDARY_USE_ONE = u'secondary_use_q1'
SECONDARY_USE_TWO = u'secondary_use_q2'
SECONDARY_USE_THREE = u'secondary_use_q3'
SECONDARY_USE_FOUR = u'secondary_use_q4'
SECONDARY_USE_FIVE = u'secondary_use_q5'
IMPROPER_ONE = u'improper_q1'
IMPROPER_TWO = u'improper_q2'
IMPROPER_THREE = u'improper_q3'
GLOBAL_ONE = u'global_q1'
GLOBAL_TWO = u'global_q2'
GLOBAL_THREE = u'global_q3'
GLOBAL_FOUR = u'global_q4'
GLOBAL_FIVE = u'global_q5'
FAMILIAR_WITH_ANDROID_PERMISSION = u'familiar'
PERMISSIONS_THAT_DONT_UNDERSTAND = u'permissions_dont_understand'

## heartbeat events
HEARTBEAT_COLLECTION = u'HEARTBEAT_COLLECTION'
ACCESSIBILITY_ACCESS_ON = u'accessibility_service_on'
APP_USAGE_ACCESS_ON = u'app_usage_access_on'

## runtime parameters
RUNTIME_PARAMETERS_COLLECTION = u'RUNTIME_PARAMETERS_COLLECTION'
ACTIVE_USER_COUNT = u'active'
TARGET_USER_COUNT = u'target'
TOTAL_USER_COUNT = u'total'

## revoke permission notification click events
REVOKE_PERMISSION_NOTIFICATION_CLICK_COLLECTION = u'REVOKE_PERMISSION_NOTIFICATION_CLICK_COLLECTION'
GRANT_SURVEY_SERVER_ID = u'grant_survey_server_id'

## local storage sync events
LOCAL_STORAGE_SYNC_LOG_COLLECTION = u'LOCAL_STORAGE_SYNC_LOG_COLLECTION'

## analytics
SERVER_DOC_ID = u'firebase_doc_id'
MISSING_CATEGORY_UNKNOWN = u'unknown'
SURVEY_DICT_IN_EVENT_OBJECT = u'survey_dict'
PROACTIVE_DICT_IN_PERMISSION_EVENT_OBJECT = u'proactive_dict'
REVOKE_PERMISSION_EVENT_OBJECT = u'revoke_permission_dict'

## country codes
COUNTRY_CODE_LIST = [
    'ca',
    'us',
    'sg',
    'fr',
    'es',
    'hk',
    'gb',
    'za',
    'ar',
    'in',
    'kr'
]