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

import org.lfedge.homeedge.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class InternalHandler{

    //class to handle the ping request api/v1/ping
    public static class PingHandler extends RouterNanoHTTPD.GeneralHandler {
        private static final String TAG = PingHandler.class.getSimpleName();

        @Override
        public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            Log.d(TAG, "PingHandler is called" + session.getHeaders().get("host"));
            return newFixedLengthResponse(Response.Status.OK, Utils.API.CONTENT_TYPE, Utils.API.PING_RESPONSE);
        }
    }
    public static class NotifyHandler extends RouterNanoHTTPD.GeneralHandler {
        private static final String TAG = PingHandler.class.getSimpleName();
        @Override
        public Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            Log.d(TAG,"NotifyHandler Post is called");
            HashMap<String,String> files = new HashMap<>();
            try {
                session.parseBody(files);
                JSONObject object  = new JSONObject(files.get("postData"));
                if(object!=null) {
                    String serviceId = object.getString("ServiceID");
                    String status = object.getString("Status");
                }
            } catch (IOException e) {
                Log.e(TAG,"IOException " + e.getMessage());
            } catch (NanoHTTPD.ResponseException e) {
                Log.e(TAG,"ResponseException " + e.getMessage());
            } catch (JSONException e) {
                Log.e(TAG,"JSONException " + e.getMessage());
            }

            return newFixedLengthResponse(Response.Status.OK, Utils.API.CONTENT_TYPE, "");
        }
        }
}