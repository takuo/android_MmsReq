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

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MMSReq extends Activity {

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private TextView mTextResult;
    private CheckBox mCheckBox;
    private Spinner mSpinnerAPN;
    private Spinner mSpinnerNotif;

    private Request mRequest;
    private boolean mResult = false;
    private NotificationManager mNotifMgr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.main);
        mTextResult = (TextView) findViewById(R.id.t_result);
        mSpinnerAPN = (Spinner) findViewById(R.id.spinner_apn);
        mSpinnerNotif = (Spinner) findViewById(R.id.spinner_notification);
        Button b = (Button) findViewById(R.id.b_request);
        mNotifMgr = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifMgr.cancel(R.string.app_name);

        b.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mRequest = new Request(mContext, Preferences.getMmsType(mContext));
                AsyncRequest req = new AsyncRequest();
                req.execute();
            }
        });

        mCheckBox = (CheckBox) findViewById(R.id.check_auto_exit);
        mCheckBox.setChecked(Preferences.getAutoExit(mContext));
        mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.setAutoExit(mContext, isChecked);
            }
        });
        mCheckBox = (CheckBox) findViewById(R.id.check_auto_request);
        mCheckBox.setChecked(Preferences.getAutoRequest(mContext));
        mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.setAutoRequest(mContext, isChecked);
            }
        });

        mSpinnerNotif.setSelection(Preferences.getNotificationType(mContext));
        mSpinnerNotif.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Spinner spinner = (Spinner) parent;
                int val = (int)spinner.getSelectedItemPosition();
                Preferences.setNotificationType(mContext, val);
            }
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        mSpinnerAPN.setSelection(Preferences.getMmsType(mContext));
        mSpinnerAPN.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Spinner spinner = (Spinner) parent;
                int val = (int)spinner.getSelectedItemPosition();
                Preferences.setMmsType(mContext, val);
            }
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    // Background Task class
    class AsyncRequest extends AsyncTask<Void, String, String> {
        public AsyncRequest() {
            mProgressDialog = new ProgressDialog(MMSReq.this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
       }

        @Override
        protected String doInBackground(Void... params) {
            return requestMMS();
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(mProgressDialog != null &&
                mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            if (mResult && Preferences.getAutoExit(mContext)) {
                switch (Preferences.getNotificationType(mContext)) {
                case Preferences.NOTIFICATION_TOAST:
                    Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
                    break;
                case Preferences.NOTIFICATION_BAR:
                    Intent in = new Intent(mContext, MMSReq.class);
                    PendingIntent pending = PendingIntent.getActivity(mContext, 0, in, Intent.FLAG_ACTIVITY_NEW_TASK);
                    Notification n = new Notification(R.drawable.icon, result, System.currentTimeMillis());
                    n.setLatestEventInfo(mContext, "Softbank MmsReq", result, pending);
                    mNotifMgr.notify(R.string.app_name, n);
                    break;
                }
                finish();
            } else {
                DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");
                mTextResult.setText(result + "\n" + df.format(new Date()));
            }
        }

        protected void onPreExecute() {
            super.onPreExecute();
            mTextResult.setText("");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setTitle(R.string.start_request);
            mProgressDialog.show();
        }

        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setMessage(progress[0]);
        }

        protected String requestMMS() {
            String message = null;
            try {
                publishProgress(getString(R.string.connect_to_mobile));
                mRequest.getConnectivity();
                publishProgress(getString(R.string.connect_to_server));
                mRequest.tryConnect();
                publishProgress(getString(R.string.request_to_server));
                message = mRequest.httpRequest();
                mResult = true;
            } catch (Request.NoConnectivityException e) {
                if (message == null)
                    message = getString(R.string.failed_to_connect);
                message = message + "\n" + e.getMessage();
            } catch (Request.NoRouteToHostException e) {
                if (message == null)
                    message = getString(R.string.failed_to_connect);
                message = message + "\n" + e.getMessage();
            } catch (Request.ConnectTimeoutException e) {
                if (message == null)
                    message = getString(R.string.failed_to_connect);
                message = message + "\n" + e.getMessage();
            } catch (Exception e) {
                if (message == null)
                    message = getString(R.string.failed_to_connect);
                message = message + "\n" + e.getMessage();
                e.printStackTrace();
            } finally {
                mRequest.disconnect();
            }
            return message;
        }
    } /* AsyncRequest */
}
