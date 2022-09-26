/****************************************************************************
 Copyright 2022-2023 Samsung Electronics All Rights Reserved.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

package org.lfedge.homeedge;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.lfedge.homeedge.common.Utils;
import org.lfedge.homeedge.server.HTTPServer;

import java.io.IOException;

public class HomeEdgeService extends Service {
    public static final String TAG="HomeEdgeService";
    public static Context mContext;
    NsdHelper mNsdHelperHelper;
    HTTPServer httpServer;
    private BroadcastReceiver broadcastReceiverNetworkState;
    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        HomeEdgeService getService() {
            return HomeEdgeService.this;
        }
    }

    @Override
    public void onCreate() {
        mContext = this;
        // INIT BROADCAST RECEIVER TO LISTEN NETWORK STATE CHANGED
        initBroadcastReceiverNetworkStateChanged();
    }
    private void initBroadcastReceiverNetworkStateChanged() {
        final IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filters.addAction("android.net.wifi.STATE_CHANGE");
        broadcastReceiverNetworkState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG,"Wifi states has changed");
                String IP = Utils.getDeviceIP(context);
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (!wifiManager.isWifiEnabled()) {
                    Log.d(TAG,"The wifi is not connected");
                    //no connection
                    if(httpServer!=null && httpServer.isAlive())
                    {
                        httpServer.stop();
                    }
                    httpServer = null;
                    return;
                }
                //Connected to another network
                if(httpServer!= null && httpServer.isAlive()) {
                    Log.d(TAG,"HTTP Server is not null");
                    String oldIP = httpServer.getDeviceIP();
                    Log.d(TAG,"The IP now is " + IP);

                    if(IP!=null && !IP.equals(oldIP)){
                        Log.i(TAG,"IP has changed!!!");
                        httpServer.stop();
                        httpServer = null;
                        //Starting the server
                        serverStart(IP,Utils.API.EXTERNAL_PORT);
                        serverStart(IP,Utils.API.INTERNAL_PORT);
                    }
                }else{
                    //Starting the fresh instance or after the network is lost and reconnected
                    Log.d(TAG,"Starting server");
                    if(IP!=null) {
                        serverStart(IP, Utils.API.EXTERNAL_PORT);
                        serverStart(IP, Utils.API.INTERNAL_PORT);
                    }
                }
            }
        };
        super.registerReceiver(broadcastReceiverNetworkState, filters);
    }
    private void serverStart(String hostname,int port){
        httpServer = new HTTPServer(hostname,port);
        try {
            if(httpServer.isAlive())
                Log.i(TAG,"HttpServer is already alive");
            else {
                Log.d(TAG,"Staring the Server");
                httpServer.start();
                Log.d(TAG,"The Server is running on "+httpServer.getHostname());
            }
        } catch (IOException e) {
            Log.e(TAG,"IOException Raised" + e.getMessage());
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mNsdHelperHelper = new NsdHelper(this);
        mNsdHelperHelper.initializeNsd();
        //Not required so commented
        // mNsdHelperHelper.registerService(Utils.API.PORT);
        mNsdHelperHelper.discoverServices();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Being destroyed.");
        if(httpServer != null)
            httpServer.stop();
        super.onDestroy();
        //unregistering the receiver on App Kill
        super.unregisterReceiver(broadcastReceiverNetworkState);
        Log.d(TAG, "Being stopped.");
        if (mNsdHelperHelper != null){
            mNsdHelperHelper.tearDown();
            mNsdHelperHelper = null;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}