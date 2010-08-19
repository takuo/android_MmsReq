package jp.takuo.android.mmsreq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Clear last status
        Log.d("MmsReq", "Clear saved status");
        Preferences.setSavedDisconnectedAt(context, 0);
        Preferences.setSavedStatus(context, false);
    }
}
