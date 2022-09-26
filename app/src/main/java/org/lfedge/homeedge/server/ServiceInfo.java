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

package org.lfedge.homeedge.server;

import org.json.JSONArray;

public class ServiceInfo {
    String Requester;
    String NotificationTargetURL;
    String ServiceName;
    Float ServiceID;
    JSONArray UserArgs;

    public void setRequester(String requester) {
        Requester = requester;
    }

    public void setNotificationTargetURL(String notificationTargetURL) {
        NotificationTargetURL = notificationTargetURL;
    }

    public void setServiceName(String serviceName) {
        ServiceName = serviceName;
    }

    public void setServiceID(Float serviceID) {
        ServiceID = serviceID;
    }

    public String getRequester() {
        return Requester;
    }

    public String getNotificationTargetURL() {
        return NotificationTargetURL;
    }

    public String getServiceName() {
        return ServiceName;
    }

    public Float getServiceID() {
        return ServiceID;
    }

    public JSONArray getUserArgs() {
        return UserArgs;
    }

    public void setUserArgs(JSONArray userArgs) {
        UserArgs = userArgs;
    }
}
