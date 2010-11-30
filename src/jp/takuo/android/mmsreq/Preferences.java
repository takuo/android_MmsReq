/*
 * Copyright (C) 2010 Takuo Kitame
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.takuo.android.mmsreq;

import java.util.Date;

import android.content.Context;
// import android.content.ContentResolver;
import android.preference.PreferenceManager;

public class Preferences {
    private static final String PREFS_AUTO_REQUEST  = "auto_request";
    private static final String PREFS_AUTO_EXIT = "auto_exit";
    private static final String PREFS_NOTIFICATION_TYPE = "notification_type";
    private static final String PREFS_MMS_TYPE  = "mms_type";

    private static final String PREFS_SAVED_STATUS = "saved_status";
    private static final String PREFS_SAVED_DISCONNECTED_AT = "disconnected_at";

    public static final int NOTIFICATION_NONE  = 0;
    public static final int NOTIFICATION_BAR   = 1;
    public static final int NOTIFICATION_TOAST = 2;

    static public int getMmsType(Context context) {
        return PreferenceManager
        .getDefaultSharedPreferences(context)
        .getInt(PREFS_MMS_TYPE, 0);
    }

    static public int getNotificationType(Context context) {
        return PreferenceManager
        .getDefaultSharedPreferences(context)
        .getInt(PREFS_NOTIFICATION_TYPE, 0);
    }

    static public boolean getAutoRequest(Context context) {
        return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean(PREFS_AUTO_REQUEST, false);
    }

    static public boolean getAutoExit(Context context) {
        return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean(PREFS_AUTO_EXIT, true);
    }

    static public boolean getSavedStatus(Context context) {
        return PreferenceManager
        .getDefaultSharedPreferences(context)
        .getBoolean(PREFS_SAVED_STATUS, true);
    }

    static public Date getSavedDisconnectedAt(Context context) {
        long time = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getLong(PREFS_SAVED_DISCONNECTED_AT, 0);
        return (time == 0) ? null : new Date(time);
    }

    static public void setAutoRequest(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
        .edit().putBoolean(PREFS_AUTO_REQUEST, value).commit();
    }

    static public void setAutoExit(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
        .edit().putBoolean(PREFS_AUTO_EXIT, value).commit();
    }

    static public void setSavedStatus(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
        .edit().putBoolean(PREFS_SAVED_STATUS, value).commit();
    }

    static public void setSavedDisconnectedAt(Context context, long value) {
        PreferenceManager.getDefaultSharedPreferences(context)
        .edit().putLong(PREFS_SAVED_DISCONNECTED_AT, value).commit();
    }

    static public void setMmsType(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
        .edit().putInt(PREFS_MMS_TYPE, value).commit();
    }

    static public void setNotificationType(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
        .edit().putInt(PREFS_NOTIFICATION_TYPE, value).commit();
    }
}
