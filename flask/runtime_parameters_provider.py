from firebase_cloudstore_provider import get_firestore_database
from event_constants import *

def get_all_runtime_parameters_events():
    '''
    Returns { <parameter_name>: <parameter_python_dict> }
    '''
    db = get_firestore_database()
    docs = db.collection(RUNTIME_PARAMETERS_COLLECTION).stream()
    runtime_parameters_dict = {}
    for doc in docs:
        parameter_name = doc.id
        runtime_parameters_dict[parameter_name] = doc.to_dict()
    return runtime_parameters_dict