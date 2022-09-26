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

package org.lfedge.homeedge.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.lfedge.homeedge.model.Device;

import java.util.List;

@Dao
public interface DeviceDao {

    @Insert(onConflict  = OnConflictStrategy.REPLACE)
    void  insert(Device device);

    @Query("SELECT * FROM device")
    List<Device> getAllDevices();

    @Query("DELETE FROM device")
    void deleteAll();

    @Query("DELETE FROM device where edge_orch_id = :edge_orch_id")
    void deleteDevice(String edge_orch_id);

    @Query("SELECT * FROM device WHERE edge_orch_id = :edge_orch_id")
    Device getDevicebyId(String edge_orch_id);

    @Query("SELECT service_list FROM device WHERE edge_orch_id = :edge_orch_id")
    List<String> getServiceList(String edge_orch_id);

    @Query("UPDATE device SET service_list = :service_list WHERE edge_orch_id =:edge_orch_id")
    void update(String edge_orch_id, List<String> service_list);

    @Query("SELECT * FROM device WHERE service_list LIKE '%' || :service_name || '%'")
    List<Device> getDevicebyService(String service_name);
}
