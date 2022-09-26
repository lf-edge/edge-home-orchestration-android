/****************************************************************************
 Copyright 2022 Samsung Electronics All Rights Reserved.

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

package org.lfedge.homeedge.common;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.UUID;

public class Utils {
    public static final String TAG = "Utils";
    public static String BASE_URL = null;

    public static final String deviceIDFile  = "orchestration_deviceID.txt";

    public static String generateDeviceUUID(Context mContext){
        String deviceID;
        deviceID = readDeviceUUIDfromFile(mContext);
        if(deviceID.isEmpty()){
            String uniqueId = UUID.randomUUID().toString();
            Log.d(TAG,"UUID generated is "+uniqueId);
            writeDeviceIDtoFile(mContext, uniqueId);
            return Constants.BASE_SERVICE_NAME + uniqueId;
        }else
            return deviceID;
    }

    private static void writeDeviceIDtoFile(Context mContext, String uniqueID){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mContext.openFileOutput
                    (deviceIDFile, Context.MODE_PRIVATE));
            outputStreamWriter.write(uniqueID);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.getMessage());
        }
    }

    private static String readDeviceUUIDfromFile(Context mContext){
        try {
            FileInputStream fis = mContext.openFileInput(deviceIDFile);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = Constants.BASE_SERVICE_NAME + sb.toString();
            Log.d(TAG, "id : " + result);
            return result;
        } catch (IOException e) {
            return "";
        }
    }

    public static String getDeviceIP(Context mContext){
        WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }
        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
            Log.d(TAG,"The IP resolved is " + ipAddressString);
        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to get host address." + ex.getMessage());
            ipAddressString = null;
        }
        return ipAddressString;
    }
    public static void setOrchestrationInfoAPI(String host){
        BASE_URL = API.PROTOCOL+host+":"+ API.INTERNAL_PORT+ API.ROOT_PATH;
    }
    public static class Constants{
        public static final String EXEC_TYPE = "ExecType";
        public static final String PLATFORM = "Platform";
        public static final String BASE_SERVICE_NAME  = "edge-orchestration-";
        public  static  final String RE_REGISTER_MSG = "Service already registered.To re-register close the application and launch again";
    }
    public interface API{
        String ROOT_PATH="/api/v1/";
        int INTERNAL_PORT = 56002;
        int EXTERNAL_PORT = 56001;
        String PING_REQUEST = "ping";
        String PING_RESPONSE="pong";
        String SERVICE_REQUEST = "orchestration/services";
        String CONTENT_TYPE="application/json; charset=UTF-8";
        String DISCOVER_REQUEST ="discoverymgr/orchestrationinfo";
        String SCORING_REQUEST ="scoringmgr/score";
        String PROTOCOL = "http://";
        String INTERNAL_SERVICE_REQUEST ="servicemgr/services" ;
        String NOTIFICATION_REQUEST = "servicemgr/services/notification/1";
        String TEXT_CONTENT_TYPE="text/plain";
    }

    public interface SERVICEDATA{
        String REQUESTER="Requester";
        String SERVICE_NAME="ServiceName";
        String SERVICE_ID = "ServiceID";
        String NOTI_URL = "NotificationTargetURL";
        String USER_ARGS = "UserArgs";
    }
    public interface RESPONSE{
        String REQUEST_CANNOT_BE_EMPTY = "Request cannot be empty";
        String NULL_RESPONSE = "No Response";
    }
}
