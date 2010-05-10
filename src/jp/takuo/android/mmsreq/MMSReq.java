/*
 * Copyright (C) 2010 Takuo Kitame
 * Copyright (C) 2007-2010 The Android Open Source Project
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
import android.os.Bundle;

import android.content.Context;

import android.net.ConnectivityManager;
import android.util.Log;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MMSReq extends Activity {
    private static final String LOG_TAG = "MMSReq";

    private static final String MMS_PROXY_HOST = "smileweb.softbank.ne.jp";
    private static final int    MMS_PROXY_PORT = 8080;
    private static final String MMS_URL        = "http://mail/cgi-ntif/mweb_ntif_res.cgi?jpn=1";
    private static final String USER_AGENT     = "smailhelp";

    private static final int APN_ALREADY_ACTIVE     = 0;
    private static final int APN_REQUEST_STARTED    = 1;
    // private static final int APN_TYPE_NOT_AVAILABLE = 2;
    // private static final int APN_REQUEST_FAILED     = 3;

    private static ProgressDialog mProgressDialog;
    private static TextView mTextResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTextResult = (TextView) findViewById(R.id.t_result);
        Button b = (Button) findViewById(R.id.b_request);
        ClickListener listener =  new ClickListener();
        b.setOnClickListener(listener);
    }

    class ClickListener implements OnClickListener {
        public void onClick(View v) {
            AsyncRequest req = new AsyncRequest();
            req.execute();
        }
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
            Log.d(LOG_TAG, "onPostExecute");
            super.onPostExecute(result);
            if(mProgressDialog != null &&
                mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
                mProgressDialog = null;
              }
            DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");
            mTextResult.setText(result + ": " + df.format(new Date()));
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

        protected int beginMmsConnectivity(ConnectivityManager ConnMgr) throws IOException {
            Log.d(LOG_TAG, "startUsingNetworkFeature: MOBILE, enableMMS");
            int result = ConnMgr.startUsingNetworkFeature(
            ConnectivityManager.TYPE_MOBILE, "enableMMS");

            Log.d(LOG_TAG, "beginMmsConnectivity: result=" + result);

            switch (result) {
                case APN_ALREADY_ACTIVE:
                case APN_REQUEST_STARTED:
                    return result;
            }

            throw new IOException("Cannot establish MMS connectivity");
        }

        protected void ensureRoute(ConnectivityManager ConnMgr) throws IOException {
            int addr = lookupHost(MMS_PROXY_HOST);
            if (addr == -1) {
               throw new IOException("Cannot resolve host: " + MMS_PROXY_HOST);
            } else {
                // FIXME: should be ConnectivityManager.TYPE_MOBILE_MMS
                if (!ConnMgr.requestRouteToHost(2, addr) )
                    throw new IOException("Cannot establish route to :" + addr);
            }
        }

        protected HttpResponse requestHttp() throws ClientProtocolException, IOException{
            HttpGet reqGet = new HttpGet(MMS_URL);
            HttpClient client = new DefaultHttpClient();
            HttpParams params = client.getParams();
            ConnRouteParams.setDefaultProxy(params, new HttpHost(MMS_PROXY_HOST, MMS_PROXY_PORT));
            HttpProtocolParams.setUserAgent(params, USER_AGENT);
            reqGet.setParams(params);
            return (HttpResponse) client.execute(reqGet);
        }

        protected String requestMMS() {
            ConnectivityManager ConnMgr;
            Context ctx;
            ctx = getApplicationContext();
            ConnMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            String message = null;
            try {
                if (!ConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable()) {
                    message = getString(R.string.not_available);
                    throw new IOException("Cannot establish Network for MMS Request");
                  }
                publishProgress(getString(R.string.connect_to_mobile));
                while(true) {
                    int result = beginMmsConnectivity(ConnMgr);
                    if (result != APN_ALREADY_ACTIVE) {
                        Log.d(LOG_TAG, "Extending MMS connectivity returned " +  result +
                                " instead of APN_ALREADY_ACTIVE, waiting for ready");
                        // Just wait for connectivity startup without
                        // any new request of APN switch.
                        Thread.sleep(1500);
                    } else {
                        break;
                       }
                  }
                publishProgress(getString(R.string.connect_to_server));
                Thread.sleep(500);
                ensureRoute(ConnMgr);
                publishProgress(getString(R.string.request_to_server));
                HttpResponse res = requestHttp();
                StatusLine status = res.getStatusLine();
                if (status.getStatusCode() != 200) {
                    message = getString(R.string.error_from_server) + " HTTP:" + status.getStatusCode();
                    Log.e(LOG_TAG, "HTTP Response: " + status.getStatusCode() + ", " + status.getReasonPhrase());
                } else {
                    message = getString(R.string.request_successed);
                    Log.d(LOG_TAG, "HTTP Response: 200, " + status.getReasonPhrase());
                  }
            } catch (Exception e) {
                if (message == null)
                    message = getString(R.string.failed_to_connect);
                Log.e(LOG_TAG, e.toString());
            } finally {
                Log.d(LOG_TAG, "stopUsingNetworkFeature: MOBILE, enableMMS");
                ConnMgr.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");
              }
            return message;
        }
    } /* AsyncRequest */

    /**
     * Look up a host name and return the result as an int. Works if the argument
     * is an IP address in dot notation. Obviously, this can only be used for IPv4
     * addresses.
     * @param hostname the name of the host (or the IP address)
     * @return the IP address as an {@code int} in network byte order
     */
     private static int lookupHost(String hostname) {
         InetAddress inetAddress;
         try {
             inetAddress = InetAddress.getByName(hostname);
         } catch (UnknownHostException e) {
             return -1;
         }
         Log.d(LOG_TAG, "Resolved Address: " + inetAddress.toString());
         byte[] addrBytes;
         int addr;
         addrBytes = inetAddress.getAddress();
         addr = ((addrBytes[3] & 0xff) << 24)
                    | ((addrBytes[2] & 0xff) << 16)
                    | ((addrBytes[1] & 0xff) << 8)
                    |  (addrBytes[0] & 0xff);
         return addr;
    }
}
