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

package org.lfedge.homeedge.client;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.lfedge.homeedge.common.Utils;
import org.lfedge.homeedge.database.DeviceDatabase;
import org.lfedge.homeedge.server.ServiceInfo;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;

public class APIImpl {
    private static final String TAG = APIImpl.class.getSimpleName();
    final Context mContext;
    public APIImpl(Context context){
        this.mContext = context;
    }
    public void getOrchestrationInfo(String deviceID) {
        String url = Utils.BASE_URL+Utils.API.DISCOVER_REQUEST;
        Log.d(TAG,"getOrchestrationInfo is getting called at " + url);
        Call<Response.OrchestrationResponse> call = Client.getInstance().getMyApi().getOrchestrationInfo(url,deviceID);

        call.enqueue(new Callback<Response.OrchestrationResponse>() {
            @Override
            public void onResponse(@NonNull Call<Response.OrchestrationResponse> call, @NonNull retrofit2.Response<Response.OrchestrationResponse> response) {
                Log.d(TAG,"OnResponse is called" + call.request().url());
                if(response.isSuccessful()){
                    Response.OrchestrationResponse device = response.body();
                    if(device!= null){
                    Log.d(TAG,"Device Platform: "+device.getPlatform() + " ExecutionType:"+device.getExecutionType());
                        if(device.getServiceList()!=null && !device.getServiceList().isEmpty()) {
                            Log.d(TAG,"Updating with service List");
                            DeviceDatabase.databaseWriterExecutor.execute(() -> DeviceDatabase.getDatabase(mContext).
                                    deviceDao().update(deviceID, device.getServiceList()));
                        }
                }}
            }
            @Override
            public void onFailure(@NonNull Call<Response.OrchestrationResponse> call, @NonNull Throwable t) {
                Toast.makeText(mContext.getApplicationContext(), "An error has occurred", Toast.LENGTH_LONG).show();
                Log.e(TAG,"Error Occurred is" +t.getMessage());
            }

        });
    }
    public String getScoreInfo(String deviceID) {
        Log.d(TAG,"getScoreInfo is getting called now");
        JSONObject object = new JSONObject();
        String score = null;
        try {
            String url = Utils.BASE_URL + Utils.API.SCORING_REQUEST;
            object.put("devID",deviceID);
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(object.toString());
            Call<Response.ScoreResponse> call = Client.getInstance().getMyApi().getScoreInfo(url,gsonObject,deviceID);
            retrofit2.Response<Response.ScoreResponse> scoreResponse = call.execute();
            if(scoreResponse.isSuccessful() && scoreResponse.body()!=null && scoreResponse.body().getScore()!=null){
                score = scoreResponse.body().getScore();
                Log.d(TAG,"The score obtained is " + score);

            }else{
                Log.d(TAG,"Call failed!!! "+scoreResponse.errorBody());
               score = "0";
            }
        } catch (JSONException | IOException e) {
           Log.e(TAG,"Error in JSONObject creation "+e.getMessage());
        }
        return score;
    }
    public String ExecuteService(ServiceInfo serviceInfo) {
        Log.d(TAG,"ExecuteService is getting called now");
        String status = null;
        String url = Utils.BASE_URL + Utils.API.INTERNAL_SERVICE_REQUEST;
        JSONObject serviceObject = new JSONObject();
        try {
            serviceObject.put(Utils.SERVICEDATA.REQUESTER, serviceInfo.getRequester());
            serviceObject.put(Utils.SERVICEDATA.SERVICE_NAME,serviceInfo.getServiceName());
            serviceObject.put(Utils.SERVICEDATA.SERVICE_ID,serviceInfo.getServiceID());
            serviceObject.put(Utils.SERVICEDATA.NOTI_URL,serviceInfo.getNotificationTargetURL());
            serviceObject.put(Utils.SERVICEDATA.USER_ARGS,serviceInfo.getUserArgs());
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(serviceObject.toString());
            Call<Response.ServiceResponse> call = Client.getInstance().getMyApi().ExecuteService(url,gsonObject, serviceInfo);

            retrofit2.Response<Response.ServiceResponse> serviceResponse = call.execute();
            if(serviceResponse.isSuccessful() && serviceResponse.body()!=null && serviceResponse.body().getStatus()!=null){
                Log.d(TAG,"Received the response!!!" +serviceResponse.body().getStatus());
                status = serviceResponse.body().getStatus();
            }else{
                Log.e(TAG,"Error in API call ");
                if(serviceResponse.errorBody()!=null)
                    status = serviceResponse.errorBody().string();
                else
                    status = Utils.RESPONSE.NULL_RESPONSE;
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return status;
    }
}
