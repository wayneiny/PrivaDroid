package com.weichengcao.privadroid.database;

import android.content.Context;
import android.util.Log;

import com.weichengcao.privadroid.PrivaDroidApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static com.weichengcao.privadroid.util.EventUtil.SYNCED;

public class OnDeviceStorageProvider {

    private static final String TAG = OnDeviceStorageProvider.class.getSimpleName();

    public static final String APP_INSTALL_FILE_NAME = "app_install_events.json";
    public static final String APP_UNINSTALL_FILE_NAME = "app_uninstall_events.json";
    public static final String PERMISSION_FILE_NAME = "permission_events.json";
    public static final String DEMOGRAPHIC_FILE_NAME = "demographic_event.json";

    private static JSONObject readJsonEventFromFile(String fileName) {
        File file = new File(PrivaDroidApplication.getAppContext().getFilesDir(), fileName);
        try {
            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(bytes);
            String jsonString = new String(bytes, StandardCharsets.UTF_8);
            return new JSONObject(jsonString);
        } catch (JSONException | IOException e) {
            return new JSONObject();
        }
    }

    private static JSONArray readJsonEventsFromFile(String fileName) {
        File file = new File(PrivaDroidApplication.getAppContext().getFilesDir(), fileName);
        try {
            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(bytes);
            String jsonString = new String(bytes, StandardCharsets.UTF_8);
            return new JSONArray(jsonString);
        } catch (JSONException | IOException e) {
            return new JSONArray();
        }
    }

    /**
     * Add synced:false to event. Only add this for local storage event.
     */
    private static void addSyncedFlag(HashMap<String, String> event) {
        event.put(SYNCED, Boolean.toString(false));
    }

    public static void writeEventToFile(HashMap<String, String> event, String fileName) {
        File file = new File(PrivaDroidApplication.getAppContext().getFilesDir(), fileName);
        JSONArray savedEvents = new JSONArray();
        if (file.exists()) {
            // 1. read from the file first
            savedEvents = readJsonEventsFromFile(fileName);
        }

        // 2. add new event to savedEvents
        addSyncedFlag(event);
        JSONObject newEvent = new JSONObject(event);
        savedEvents.put(newEvent);

        // 2. update file
        try {
            FileOutputStream outputStream = PrivaDroidApplication.getAppContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(savedEvents.toString().getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot find file " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to file " + fileName);
        }
    }

    /**
     * Demographic event
     */
    public static void writeDemographicEventToFile(HashMap<String, String> event) {
        JSONObject newEvent = new JSONObject(event);

        try {
            FileOutputStream outputStream = PrivaDroidApplication.getAppContext().openFileOutput(DEMOGRAPHIC_FILE_NAME, Context.MODE_PRIVATE);
            outputStream.write(newEvent.toString().getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot find file " + DEMOGRAPHIC_FILE_NAME);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to file " + DEMOGRAPHIC_FILE_NAME);
        }
    }
}
