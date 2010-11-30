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
/*
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.net.Uri;
*/
public class Preferences {
    private static final String PREFS_AUTO_REQUEST  = "auto_request";
    private static final String PREFS_AUTO_EXIT = "auto_exit";
    private static final String PREFS_ENABLE_TOAST = "enable_toast";
    private static final String PREFS_MMS_TYPE  = "mms_type";

    private static final String PREFS_SAVED_STATUS = "saved_status";
    private static final String PREFS_SAVED_DISCONNECTED_AT = "disconnected_at";
/*
    private static final String APN_TYPE_MMS = "mms";
    private static final String APN_TYPE_ALL = "*";

    private static final String[] APN_PROJECTION = {
            "apn",             // 0
            "type",            // 1
    };
    private static final Uri CONTENT_URI = Uri.parse("content://telephony/carriers");
    private static final int COLUMN_APN          = 0;
    private static final int COLUMN_TYPE         = 1;

    private static final String TAG = "MmsReq";

    static private boolean isValidApnType(String types, String requestType) {
        // If APN type is unspecified, assume APN_TYPE_ALL.
        if (TextUtils.isEmpty(types)) {
            return true;
        }

        for (String t : types.split(",")) {
            if (t.equals(requestType) || t.equals(APN_TYPE_ALL)) {
                return true;
            }
        }
        return false;
    }

    static private int getMmsTypeInternal(Context context) {
        String apn = null;
        String selection = null;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Uri.withAppendedPath(CONTENT_URI,"current"),
                APN_PROJECTION, selection, null, null);

        if (cursor == null) {
            Log.e(TAG, "Apn is not found in Database!");
            return -1;
        }

        boolean sawValidApn = false;
        try {
            while (cursor.moveToNext() && TextUtils.isEmpty(apn)) {
                // Read values from APN settings
                if (isValidApnType(cursor.getString(COLUMN_TYPE), APN_TYPE_MMS)) {
                    sawValidApn = true;
                    apn = cursor.getString(COLUMN_APN);
                }
            }
        } finally {
            cursor.close();
        }
        if (sawValidApn && TextUtils.isEmpty(apn)) {
            Log.e(TAG, "Invalid APN setting: APN is empty");
        }
        if (!TextUtils.isEmpty(apn)) {
            if (apn.equalsIgnoreCase("smile.world")) {
                Log.d(TAG, "MMS APN Detect: smile.world");
                return Request.APN_PROFILE_SMILE;
            } else if (apn.equalsIgnoreCase("mailwebservice.softbank.ne.jp")) {
                Log.d(TAG, "MMS APN Detect: sbmms");
                return Request.APN_PROFILE_SBMMS;
            } else if (apn.equalsIgnoreCase("open.softbank.ne.jp")) {
                Log.d(TAG, "MMS APN Detect: mmsopen");
                return Request.APN_PROFILE_OPEN;
             }
        }
        return -1;
    }
*/

    static public int getMmsType(Context context) {
        return PreferenceManager
        .getDefaultSharedPreferences(context)
        .getInt(PREFS_MMS_TYPE, 0);
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

    static public boolean getEnableToast(Context context) {
        return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean(PREFS_ENABLE_TOAST, true);
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

    static public void setEnableToast(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
        .edit().putBoolean(PREFS_ENABLE_TOAST, value).commit();
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

}
