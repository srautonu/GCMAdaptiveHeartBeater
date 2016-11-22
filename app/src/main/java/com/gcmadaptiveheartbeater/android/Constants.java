package com.gcmadaptiveheartbeater.android;

/**
 * Created by mrahman on 29-Oct-16.
 */

public class Constants {

    //
    // Intent/action names
    //
    public final static String ACTION_SEND_DATA_KA = "com.gcmadaptiveheartbeater.android.SEND_GCM_KA";
    public final static String ACTION_SEND_TEST_KA           = "com.gcmadaptiveheartbeater.android.SEND_TEST_KA";
    public final static String ACTION_START_KA_TESTING       = "com.gcmadaptiveheartbeater.android.START_KA_TESTING";

    public final static String ACTION_HANDLE_SETTINGS_UPDATE = "com.gcmadaptiveheartbeater.android.HANDLE_SETTINGS_UPDATE";

    //
    // Name of the settings file. This is used from activity as well as services
    //
    public final static String SETTINGS_FILE = "com.gcmadaptiveheartbeater.android";


    //
    // Settings names
    //
    //
    // NOTE: For each of the notification app types (email/social etc.) we
    // have a settings (count) with the corresponding name. When you add a
    // new settings, make sure to avoid the names specified in
    // NotificationStats._rgStrCategory
    //

    public final static String DEVICE_NAME = "deviceName";

    public final static String EXP_MODEL = "expModel";


    //
    // Last known good keep-alive interval (in minutes)
    //
    public final static String LKG_KA = "lkgKA";

    //
    // Last known bad keep-alive interval (in minutes) - This is the minimum interval that is
    // known to not work as a KA interval.
    //
    public final static String LKB_KA = "lkbKA";

    //
    // keep-alive interval (in minutes) used in the data connection
    //
    public final static String DATA_KA = "dataKA";

    //
    // Number of KA packets sent over the test connection.
    //
    public final static String TEST_KA_COUNT = "testKACount";

    //
    // Number of KA packets sent over GCM
    //
    public final static String GCM_KA_COUNT = "gcmKACount";

    //
    // Experiment start time stamp
    //
    public final static String EXP_START_TIMESTAMP = "expStartTimeStamp";

    //
    // Experiment start time stamp
    //
    public final static String EXP_END_TIMESTAMP = "expEndTimeStamp";

    //
    // Time stamp of last KA packet in the test connection
    //
    public final static String TEST_KA_TIMESTAMP = "testKATimeStamp";

    //
    // Time stamp of last KA packet in GCM
    //
    public final static String GCM_KA_TIMESTAMP = "gcmKATimeStamp";

    //
    // Whether an experiment is in progress
    //
    public final static String EXP_IN_PROGRESS = "expInProgress";

    //
    // Experiment model Ids
    //
    public final static int EXP_MODEL_ADAPTIVE = 1;
    public final static int EXP_MODEL_FIXED_RATE = 2;
}
