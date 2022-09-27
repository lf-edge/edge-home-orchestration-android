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

import java.util.List;

public class Response {

   public static class OrchestrationResponse {
       private final String ExecutionType;
       private final String Platform;
       private final List<String> ServiceList;

       public OrchestrationResponse(String ExecutionType, String Platform, List<String> ServiceList) {
           this.ExecutionType = ExecutionType;
           this.Platform = Platform;
           this.ServiceList = ServiceList;
       }

       public String getPlatform() {
           return Platform;
       }

       public String getExecutionType() {
           return ExecutionType;
       }

       public List<String> getServiceList() {
           return ServiceList;
       }
   }
   //Capture the response for api/v1/scoringmgr/score
    public static class ScoreResponse {
        final String ScoreValue;
        public ScoreResponse(String score) {
            this.ScoreValue = score;
        }

        public String getScore() {
            return ScoreValue;
        }
    }
    public static class ServiceResponse {
        final String Status;
        public ServiceResponse(String status) {
            this.Status = status;
        }

        public String getStatus() {
            return Status;
        }
    }
}
