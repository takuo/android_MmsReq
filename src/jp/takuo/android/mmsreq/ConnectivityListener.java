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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class ConnectivityListener extends BroadcastReceiver {
    private static final String LOG_TAG = "MmsReqListener";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean enableAuto = Preferences.getAutoRequest(context);
        boolean mStatus = Preferences.getSavedStatus(context);
        Date mLastDisconn = Preferences.getSavedDisconnectedAt(context);

        if(!enableAuto ||
                !action.equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION))
                return;

        NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        Log.d(LOG_TAG, "got CONNECTIVITY_ACTION for " + info.getTypeName() + " at " + new Date());

        ConnectivityManager connMgr =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        info = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        Log.d(LOG_TAG, "TYPE_MOBILE: available=" + info.isAvailable() + ", old=" + mStatus);
        if (info.isAvailable() != mStatus) {
            mStatus = info.isAvailable();
            Preferences.setSavedStatus(context, mStatus);

            // should do nothing when background data setting is false
            if (!connMgr.getBackgroundDataSetting())
                return;

            if (mStatus) {
                Preferences.setSavedDisconnectedAt(context, 0);
                String message = null;
                if (mLastDisconn != null && mLastDisconn.compareTo(new Date()) < 5 * 60) {
                    Log.d(LOG_TAG, "Disconnected time less than 5 minutes. do nothing.");
                    return;
                }
                Log.d(LOG_TAG, "Mobile Network is available, should call request.");
                try {
                    Request req = new Request(context, Preferences.getMmsType(context));
                    message = req.requestMMS();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (message != null) {
                    switch (Preferences.getNotificationType(context)) {
                    case Preferences.NOTIFICATION_TOAST:
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        break;
                    case Preferences.NOTIFICATION_BAR:
                        Intent in = new Intent(context, MMSReq.class);
                        PendingIntent pending = PendingIntent.getActivity(context, 0, in, Intent.FLAG_ACTIVITY_NEW_TASK);
                        Notification n = new Notification(R.drawable.icon, message, System.currentTimeMillis());
                        n.setLatestEventInfo(context, "Softbank MmsReq", message, pending);
                        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.notify(R.string.app_name, n);
                        break;
                    default:
                    }
                }
            } else {
                // Lost mobile data connectivity
                Preferences.setSavedDisconnectedAt(context, (new Date()).getTime());
            }
        }
    }
}
