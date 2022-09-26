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

package org.lfedge.homeedge.orchestrationapi;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.util.Log;

import org.lfedge.homeedge.HomeEdgeService;
import org.lfedge.homeedge.client.APIImpl;
import org.lfedge.homeedge.client.APIListener;
import org.lfedge.homeedge.common.Utils;
import org.lfedge.homeedge.database.DeviceDatabase;
import org.lfedge.homeedge.model.Device;
import org.lfedge.homeedge.server.ServiceInfo;
import org.lfedge.homeedge.database.DeviceDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class ExternalHandler {
    public static class PostServiceHandler extends RouterNanoHTTPD.GeneralHandler  {
        private static final String TAG = PostServiceHandler.class.getSimpleName();
        HashMap<String,String> scoreList;
        DeviceDao deviceDao;
        final String[] result = {null,null};
        ServiceInfo serviceInfo;
        String execType;
        void initialize(){
            deviceDao = DeviceDatabase.getDatabase(HomeEdgeService.mContext).deviceDao();
            scoreList = new HashMap<>();
            serviceInfo = new ServiceInfo();
        }
        @Override
        public Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            Log.d(TAG,"PostServiceHandler is called");
            initialize();
            serviceInfo.setRequester(session.getHeaders().get("user-agent"));
            try {
                HashMap<String,String> files = new HashMap<>();
                session.parseBody(files);
                JSONObject object  = new JSONObject(files.get("postData"));
                if(object==null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, Utils.API.TEXT_CONTENT_TYPE, Utils.RESPONSE.REQUEST_CANNOT_BE_EMPTY);
                }
                    serviceInfo.setServiceID(1F);
                    serviceInfo.setServiceName(object.get("ServiceName").toString());

                    //getCandidates
                    getCandidates(serviceInfo.getServiceName(), () -> {
                        Log.d(TAG, "Completed getCandidates!!!!");
                        sortScoreList();
                        result[0] = callExecServices(object);
                        Log.d(TAG, "Result here is " + result[0]);
                        if (result[0] != null && result[0].equals("Started"))
                            result[0] = "ERROR_NONE";
                        result[1] = createResponse(result[0]);
                        Log.d(TAG, "Result here is " + result[1]);
                    });

            } catch (IOException e) {
                Log.e(TAG,"Error is io error" +e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,Utils.API.CONTENT_TYPE,e.getMessage());
            } catch (NanoHTTPD.ResponseException e) {
                Log.e(TAG,"Error is NanoHTTO Response" +e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,Utils.API.CONTENT_TYPE,e.getMessage());
            } catch (JSONException e) {
                Log.e(TAG,"Error is JSONException" +e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,Utils.API.CONTENT_TYPE,e.getMessage());
            }
            while(true){
                if(result[1]!=null)
                    break;
            }
            return newFixedLengthResponse(Response.Status.OK, Utils.API.TEXT_CONTENT_TYPE, result[1]);
        }
        void getCandidates(String serviceName, APIListener listener) {
            List<Device> deviceList =deviceDao.getDevicebyService(serviceName);
            APIImpl apiiml = new APIImpl(HomeEdgeService.mContext);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                for(int i=0;i<deviceList.size();i++) {
                    Utils.setOrchestrationInfoAPI(deviceList.get(i).getIp());
                    String id = deviceList.get(i).getEdge_orch_id();
                    Device device = deviceDao.getDevicebyId(id);
                    Utils.setOrchestrationInfoAPI(device.getIp());
                    Log.d(TAG,"Bse url is" + Utils.BASE_URL);
                    String score = apiiml.getScoreInfo(id);
                    Log.d(TAG,"Id is :"+id + "score::"+score);
                    scoreList.put(id,score);
                }
                if(listener!=null)listener.onOperationCompleted();
            });

        }
        void sortScoreList(){
            Log.d(TAG,"sortScoreList is called!!");
            ArrayList<String> list = new ArrayList<>();
            for (Map.Entry<String, String> entry : scoreList.entrySet()) {
                list.add(entry.getValue());
            }
            Collections.sort(list);
            for (Map.Entry<String, String> entry : scoreList.entrySet()) {
                if (entry.getValue().equals(list.get(0))) {
                    Device device = deviceDao.getDevicebyId(entry.getKey());
                    serviceInfo.setNotificationTargetURL(device.getIp());
                    Utils.setOrchestrationInfoAPI(device.getIp());
                    break;
                }
            }
        }
        String callExecServices(JSONObject object){
            JSONArray serviceData;
            try {
                serviceData = object.getJSONArray("ServiceInfo");
                JSONObject executionData = serviceData.getJSONObject(0);
                execType = executionData.getString("ExecutionType");
                JSONArray userArgs = executionData.getJSONArray("ExecCmd");
                //Adding exectype to jsonarray
                userArgs.put(execType);
                serviceInfo.setUserArgs(userArgs);
                APIImpl apiiml = new APIImpl(HomeEdgeService.mContext);
                result[0] = apiiml.ExecuteService(serviceInfo);
                return result[0];
            } catch (JSONException e) {
                return e.getMessage();
            }

        }
        String createResponse(String msg){
            JSONObject respMsg = new JSONObject();
            JSONObject remoteinfo = new JSONObject();
            try {
                respMsg.put("Message",msg);
                remoteinfo.put("ExecutionType",execType);
                remoteinfo.put("Target",serviceInfo.getNotificationTargetURL());
                respMsg.put("RemoteTargetInfo",remoteinfo);
                respMsg.put("ServiceName",serviceInfo.getServiceName());
                return String.valueOf(respMsg);
            } catch (JSONException e) {
                Log.e(TAG,"JSONException"+e.getMessage());
                return null;
            }

        }
    }

}
