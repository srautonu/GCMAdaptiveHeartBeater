package com.research.ton.newandroidapp;

/**
 * Created by mrahman on 29-Oct-16.
 */

public class Constants {
    //
    // Name of the settings file. This is used from activity as well as services
    //
    public final static String SETTINGS_FILE = "com.research.ton.newandroidapp";

    //
    // NOTE: For each of the notification app types (email/social etc.) we
    // have a settings (count) with the corresponding name. When you add a
    // new settings, make sure to avoid the following names
    // TODO: Provide the names here.
    //

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
    // Number of KA packets sent over the test connection.
    //
    public final static String TEST_KA_COUNT = "testKACount";

    public final static String GCM_KA_COUNT = "gcmKACount";
}
