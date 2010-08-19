package jp.takuo.android.mmsreq;

import java.util.Date;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MmsReqService extends Service {
    private static final String LOG_TAG = "MmsReqService";
    private static boolean mStatus = false;
    private static boolean mEnableService = false;
    private Date mLastDisconnected = null;
    private BroadcastReceiver mReceiver;

    public class LocalBinder extends Binder {
        MmsReqService getService() {
            return MmsReqService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    private class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            NetworkInfo target = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            Log.d(LOG_TAG, "got CONNECTIVITY_ACTION for " + target.getTypeName() + " at " + new Date());

            if(!mEnableService ||
                    !action.equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION))
                    return;

            ConnectivityManager connMgr =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            Log.d(LOG_TAG, "Connectivity Changed: status="+info.isAvailable()+", old="+mStatus);
            if (info.isAvailable() != mStatus) {
                mStatus = info.isAvailable();

                // should do nothing when background data setting is false
                if (!connMgr.getBackgroundDataSetting())
                    return;

                if (mStatus) {
                    String message = null;
                    if (mLastDisconnected != null && mLastDisconnected.compareTo(new Date()) < 10 * 60) {
                        Log.d(LOG_TAG, "Disconnected time less than 10 minutes. do nothing.");
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
                    if (message != null && 
                        Preferences.getEnableToast(context))
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                } else {
                    // Lost mobile data connectivity
                    mLastDisconnected = new Date();
                }
            }
        }
    }

    @Override
    public void onCreate() {
        ConnectivityManager connMgr =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        mStatus = info.isAvailable();
        mLastDisconnected = null;
        mReceiver = new ConnectivityReceiver();
        Log.d(LOG_TAG, "Service created.");
        mEnableService = Preferences.getAutoRequest(this);
        registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Service started.");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "Service destroied.");
        unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
