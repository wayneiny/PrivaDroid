# PrivaDroid Flask Server Handbook

## Start Server

We use Google Firebase as our datastore. In order to access Firebase, you need to create a [service account](https://console.firebase.google.com/u/0/project/privadroidproductiontest/settings/serviceaccounts/adminsdk) on Firebase console and generate a private key to connect to Firebase. Store the private key in the same folder as your Python scripts.

Next, install [Flask](https://flask.palletsprojects.com/en/1.1.x/installation/) and [Python Firebase admin sdk](https://firebase.google.com/docs/firestore/quickstart). 

Finally, you can start the Flask server by the following commands. Local server runs at 127.0.0.1:5001. 

```bash
> cd ActivityBuddyBundle/privadroid
> python flask_rest_api_server.py
```

## API Documentation

### User Join Event

This event is sent to server when a user joins our experiment. It contains user's phone make, model, Android version, carrier, country code, phone locale, PrivaDroid version, advertising id and time. An example is like the following:

```json
{
  "ad_id": "b42474ab-a84e-41cb-8e6a-4ab801fb4d38",
  "android_version": "9",
  "carrier": "Jio 4G",
  "country_code": "in",
  "locale": "eng",
  "logged_time": "2019-10-20T01:33:30.724+05:30",
  "make": "Xiaomi",
  "model": "Redmi Note 5 Pro",
  "privadroid_version": "0.21"
}
```

#### func get_user_ad_id_to_join_events()

This function takes no parameter and returns a dictionary of user advertising id to join event mappings. Example return value:

```json
{
  "b42474ab-a84e-41cb-8e6a-4ab801fb4d38": {
    "ad_id": "b42474ab-a84e-41cb-8e6a-4ab801fb4d38",
    "android_version": "9",
    "carrier": "Jio 4G",
    "country_code": "in",
    "locale": "eng",
    "logged_time": "2019-10-20T01:33:30.724+05:30",
    "make": "Xiaomi",
    "model": "Redmi Note 5 Pro",
    "privadroid_version": "0.21"
  }
}
```

#### func get_all_real_users_join_events(all_join_events={})

This function filters out our test users. Test users come from either Android emulators or real phones. I have logged the advertising ids of our test real phones and those will be filtered out. Android emulator will have the `carrier` value as `Android` so we can easily filter those out. The input `all_join_events` is the result of `get_user_ad_id_to_join_events()`.

#### func compile_joined_users_by_category_analytics(all_real_user_join_events={})

This function counts the number of users in each category value for each category. The input is the result of `get_all_real_users_join_events(all_join_events={})`. An example output for `android_version` catergory is as following and we do this for `carrier`, `country_code`, `locale`, `make` and `model` as well:

```json
{
  "android_version": {
    "9": 47,
    "10": 2,
    "6.0": 3,
    "6.0.1": 3,
    "7.0": 13,  
    "7.1.1": 3, 
    "7.1.2": 3,
    "8.0.0": 5,
    "8.1.0": 16
  },
  "total_number_of_joined_users": 95
}
```

#### endpoint /joinedusersjson

This endpoint will return the result of functions `get_all_real_users_join_events(all_join_events={})` and `ompile_joined_users_by_category_analytics(all_real_user_join_events={})`. An exmple is:

```json
{
  "join_user_analytics": "<ompile_joined_users_by_category_analytics>",
  "unique_join_events": "<get_all_real_users_join_events>"
}
```

### Demographic Survey Event

This event represents the response event of the demographic survey.

#### func get_user_ad_id_to_demographic_events()

This function returns a user ad id to event object dictionary. An example return value:

```json
{
  "13b40a43-fdc7-4fca-b96f-28ea96d7690c": {
    "ad_id": "13b40a43-fdc7-4fca-b96f-28ea96d7690c",
    "age": "Between 20 and 30",
    "country": "India",
    "daily_usage": "Between 5 and 6 hours",
    "education": "Doctorate (e.g. PhD)",
    "gender": "Male",
    "income": "$75000 - $99999",
    "industry": "College, University, and Adult Education",
    "logged_time": "2019-10-02T00:40:49.856+05:30",
    "status": "Employed part time (up to 39 hours per week)"
  }
}
```

#### func compile_demographic_by_category_analytics(all_demographic_events={})

This function takes in the dictionary result of `get_user_ad_id_to_demographic_events()` and returns a dictionary containing analytic values similar to `compile_joined_users_by_category_analytics(all_real_user_join_events={})`. An example of the analytic data is:

```json
{
  "age": {
    "above 60": 1,
    "below 20": 20,
    "between 20 and 30": 42,
    "between 30 and 40": 6
  },
  "total_number_of_users_answered": 69
}
```

### App Install And Survey Event

This section contains both App Install event and App Install Survey event. 

#### func get_doc_id_to_app_install_events()

This function returns a dictionary containing server doc id to app install event object mappings. App install event contains a `survey_dict` value, which is a app install survey event. If the survey is not answered, then `survey_dict` is null and `survey_id` is "". An example of return value is as following:

```json
{
  "WVFe0V5tya259BUMYqBg": {
    "ad_id": "03a929e5-2236-4e7d-ae16-22e7a888ffdc",
    "app_name": "SAFE",
    "app_version": "1.0",
    "firebase_doc_id": "WVFe0V5tya259BUMYqBg",
    "logged_time": "2019-10-15T01:59:55.219+05:30",
    "package_name": "com.lucideus.safe",
    "survey_dict": {
      "ad_id": "03a929e5-2236-4e7d-ae16-22e7a888ffdc",
      "event_server_id": "WVFe0V5tya259BUMYqBg",
      "firebase_doc_id": "NtEDynv4hfLjuOqIOtYr",
      "install_factors": "The app is free / price is reasonable, App functionality",
      "know_permission_required": "Yes",
      "logged_time": "2019-10-15T02:04:55.141+05:30",
      "permission_think_required": "I don't know",
      "why_install": "I want to try it out"
    },
    "survey_id": "NtEDynv4hfLjuOqIOtYr"
  },
  "tpaDDzqqS9huhGiH6lzG": {
    "ad_id": "03a929e5-2236-4e7d-ae16-22e7a888ffdc",
    "app_name": "DigiLocker",
    "app_version": "5.1.9",
    "firebase_doc_id": "tpaDDzqqS9huhGiH6lzG",
    "logged_time": "2019-10-15T08:01:04.651+05:30",
    "package_name": "com.digilocker.android",
    "survey_dict": null,
    "survey_id": ""
  }
}
```

#### func get_app_install_events_by_user_ad_id()

This function returns a dictionary containing user ad id to a list of app install events. An example is as follows:

```json
{
  "03a929e5-2236-4e7d-ae16-22e7a888ffdc": [
    {
      "ad_id": "03a929e5-2236-4e7d-ae16-22e7a888ffdc",
      "app_name": "SAFE",
      "app_version": "1.0",
      "firebase_doc_id": "WVFe0V5tya259BUMYqBg",
      "logged_time": "2019-10-15T01:59:55.219+05:30",
      "package_name": "com.lucideus.safe",
      "survey_dict": {
        "ad_id": "03a929e5-2236-4e7d-ae16-22e7a888ffdc",
        "event_server_id": "WVFe0V5tya259BUMYqBg",
        "firebase_doc_id": "NtEDynv4hfLjuOqIOtYr",
        "install_factors": "The app is free / price is reasonable, App functionality",
        "know_permission_required": "Yes",
        "logged_time": "2019-10-15T02:04:55.141+05:30",
        "permission_think_required": "I don't know",
        "why_install": "I want to try it out"
      },
      "survey_id": "NtEDynv4hfLjuOqIOtYr"
    },
    {
      "ad_id": "03a929e5-2236-4e7d-ae16-22e7a888ffdc",
      "app_name": "DigiLocker",
      "app_version": "5.1.9",
      "firebase_doc_id": "tpaDDzqqS9huhGiH6lzG",
      "logged_time": "2019-10-15T08:01:04.651+05:30",
      "package_name": "com.digilocker.android",
      "survey_dict": null,
      "survey_id": ""
    }
  ]
}
```

#### func get_doc_id_to_app_install_survey_events()

This function returns a dictionary of document id to app install survey event mappings. An example return value is as follows:

```json
{
  "WVFe0V5tya259BUMYqBg": {
    "ad_id": "03a929e5-2236-4e7d-ae16-22e7a888ffdc",
    "event_server_id": "WVFe0V5tya259BUMYqBg",
    "firebase_doc_id": "NtEDynv4hfLjuOqIOtYr",
    "install_factors": "The app is free / price is reasonable, App functionality",
    "know_permission_required": "Yes",
    "logged_time": "2019-10-15T02:04:55.141+05:30",
    "permission_think_required": "I don't know",
    "why_install": "I want to try it out"
  }
}
```

#### get_app_install_and_survey_analytics(app_install_events_by_user={})

This function calculates some analytics data about the app install and app install survey events. The calculated analytics contains

- the percentage of surveyed app install events (total, across different demographic groups)
- average app installs per user (total, across different demographic groups)
- the most popular install reasons (total, across different demogrpahic groups)
- the most popular install factors (total, acroos different demographic groups)
- the percentage of people knowing the permissions requested by the app (total, across different demographic groups)
- number of app installs across different android versions (to validate app)
- the most popular guessed permission requested by app (total, across different demographic groups)