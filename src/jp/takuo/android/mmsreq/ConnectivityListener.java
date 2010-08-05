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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class ConnectivityListener extends BroadcastReceiver {
    private static final String LOG_TAG = "MmsReq";
    private static boolean mAvailable = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean enableAuto = Preferences.getAutoRequest(context);

        if(!enableAuto ||
            !action.equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION))
            return ;
        ConnectivityManager connMgr =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        Log.d(LOG_TAG, "TYPE_MOBILE: available=" + info.isAvailable() + ", old=" + mAvailable);
        Log.d(LOG_TAG, "TYPE_MOBILE: DetailedState=" + info.getDetailedState());
        if (info.isAvailable() != mAvailable) {
            mAvailable = info.isAvailable();
            if (!connMgr.getBackgroundDataSetting())
                return;
            if (mAvailable) {
                String message = null;
                Log.d(LOG_TAG, "Mobile Network is available, should call request.");
                try {
                    Request req = new Request(context, Preferences.getMmsType(context));
                    message = req.requestMMS();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (message != null && 
                    Preferences.getEnableToast(context))
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
