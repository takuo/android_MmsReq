package jp.takuo.android.mmsreq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Preferences.getAutoRequest(context)) {
            context.startService(new Intent(context, MmsReqService.class));
        }
    }
}
