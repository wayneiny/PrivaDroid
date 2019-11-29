import time
import os

INTERVAL = 60*60*2     # 2 hours

def start_server():
    import subprocess
    from subprocess import check_output
    try:
        process_id = int(check_output(['lsof', '-t', '-i:5001', '-sTCP:LISTEN']))
        print('Killing server process id={}'.format(process_id))
        subprocess.run(['kill', '-9', str(process_id)])
    except:
        print('No existing server.')
    os.system('/home/vmuser/Desktop/venvs/privadroid/bin/python /home/vmuser/Desktop/Github/ActivityBuddyBundle/privadroid/flask_rest_api_server.py &')


while True:
    start_server()
    time.sleep(INTERVAL)
