/****************************************************************************
<<<<<<< HEAD
 Copyright 2022 Samsung Electronics All Rights Reserved.
=======
 Copyright 2022-2023 Samsung Electronics All Rights Reserved.
>>>>>>> c20e4964656c701ebbb201cad2e36498d7b4bd75

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

package org.lfedge.homeedge.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

@Entity(tableName = "device")
public class Device {
    @PrimaryKey
    @NonNull
    final
    String edge_orch_id;
    @NonNull
    final
    String ip;
    @NonNull
    final
    String port;
    @NonNull
    final
    String platform;
    @NonNull
    final
    String exec_type;

    int rtt;

    @TypeConverters(ListConverter.class)
    List<String> service_list;

    public Device( @NonNull String edge_orch_id, @NonNull String ip, @NonNull String port,
                   @NonNull String platform, @NonNull String exec_type){
        this.edge_orch_id = edge_orch_id;
        this.ip = ip;
        this.port = port;
        this.exec_type = exec_type;
        this.platform = platform;
    }


    @NonNull
    public String getEdge_orch_id() {
        return edge_orch_id;
    }


    @NonNull
    public String getIp() {
        return ip;
    }

    @NonNull
    public String getPort() {
        return port;
    }

    @NonNull
    public String getPlatform() {
        return platform;
    }

    @NonNull
    public String getExec_type() {
        return exec_type;
    }

    public int getRtt() {
        return rtt;
    }

    public void setRtt(int rtt) {
        this.rtt = rtt;
    }

    public List<String> getService_list() {
        return service_list;
    }

    public void setService_list(@NonNull List<String> service_list) {
        this.service_list = service_list;
    }
}
