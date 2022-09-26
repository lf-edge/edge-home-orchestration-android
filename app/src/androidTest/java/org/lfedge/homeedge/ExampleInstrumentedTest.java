<<<<<<< HEAD
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

=======
>>>>>>> c20e4964656c701ebbb201cad2e36498d7b4bd75
package org.lfedge.homeedge;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.lfedge.homeedge.database.DeviceDatabase;
import org.lfedge.homeedge.model.Device;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.test", appContext.getPackageName());
    }

    @Test
    public void insertDeviceEntry(){
        Device device = new Device("Test","107.108.1.1","8080","android", "android");
        DeviceDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext()).deviceDao().insert(device);

        List<Device> devices = DeviceDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext()).
                deviceDao().getAllDevices();

        assertEquals("Match", "107.108.1.1", devices.get(0).getIp());

        DeviceDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext()).
                deviceDao().deleteAll();
    }

    @Test
    public void getDeviceEntry(){
        Device device = new Device("Test","107.108.1.1","8080","android", "android");
        DeviceDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext()).deviceDao().insert(device);

        Device tempdevice = DeviceDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext()).
                deviceDao().getDevicebyId("Test");

        assertEquals("Match", "107.108.1.1", tempdevice.getIp());

        DeviceDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext()).
                deviceDao().deleteAll();
    }

    @Test
    public void updateDeviceEntry(){
        Device device = new Device("Test","107.108.1.1","8080","android", "android");
        DeviceDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext()).deviceDao().insert(device);

        List<String> serv1 = new ArrayList<>();
        serv1.add("AI");
        serv1.add("FaceRecog");

        DeviceDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext()).
                deviceDao().update("Test", serv1);

        Device tempdevice = DeviceDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext()).
                deviceDao().getDevicebyId("Test");

        assertEquals("Match", "Docker", tempdevice.getPlatform());

        DeviceDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().getTargetContext()).
                deviceDao().deleteAll();
    }
}