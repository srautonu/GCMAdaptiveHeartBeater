package com.gcmadaptiveheartbeater.android;

/**
 * Created by mrahman on 29-Oct-16.
 */

public class Constants {
    //
    // Name of the settings file. This is used from activity as well as services
    //
    public final static String SETTINGS_FILE = "com.gcmadaptiveheartbeater.android";

    public final static String SETTINGS_UPDATED_INTENT = "com.gcmadaptiveheartbeater.android.NOTIFY_SETTINGS_UPDATED";

    //
    // NOTE: For each of the notification app types (email/social etc.) we
    // have a settings (count) with the corresponding name. When you add a
    // new settings, make sure to avoid the names specified in
    // NotificationStats._rgStrCategory
    //

    public final static String DEVICE_NAME = "deviceName";

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
    // Convergence time (minutes) for KA algorithm.
    //
    public final static String CT_KA = "ctKA";

    //
    // Number of KA packets sent over the test connection.
    //
    public final static String TEST_KA_COUNT = "testKACount";

    //
    // Number of KA packets sent over GCM
    //
    public final static String GCM_KA_COUNT = "gcmKACount";

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
}
