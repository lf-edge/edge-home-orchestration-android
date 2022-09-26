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

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import org.lfedge.homeedge.client.APIImpl;
import org.lfedge.homeedge.common.Utils;
import org.lfedge.homeedge.database.DeviceDatabase;
import org.lfedge.homeedge.model.Device;
import org.lfedge.homeedge.database.DeviceDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NsdHelper {

    final Context mContext;

    final NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    public static final String SERVICE_TYPE = "_orchestration._tcp.";
    public static final String TAG = "NsdHelper";
    public String mServiceName;
    NsdServiceInfo mResolvedService;
    NsdServiceInfo mRegisteredService;
    static boolean isDiscoveryRunning = false;

    public NsdHelper(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mServiceName = Utils.generateDeviceUUID(context);
        Log.d(TAG,"The DeviceID generated is " + mServiceName);
    }

    public void initializeNsd() {
        Log.d(TAG,"Initialize NSD");
        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();

        //mNsdManager.init(mContext.getMainLooper(), this);
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains(Utils.Constants.BASE_SERVICE_NAME)){
                    Log.d(TAG,"Resolving the service");
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if (mRegisteredService == service) {
                    mRegisteredService = null;
                }
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
                if(isDiscoveryRunning){
                    mNsdManager.discoverServices(
                            SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
                }

            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                String execType=null,platform=null;
                Log.d(TAG, "Resolve Succeeded. " + serviceInfo);
                if(serviceInfo.getAttributes().containsKey(Utils.Constants.EXEC_TYPE)) {
                    execType = new String(serviceInfo.getAttributes().get(Utils.Constants.EXEC_TYPE));
                    Log.d(TAG, "Exectype:" + execType);
                }
                if(serviceInfo.getAttributes().containsKey(Utils.Constants.PLATFORM)) {
                    platform = new String(serviceInfo.getAttributes().get(Utils.Constants.PLATFORM));
                    Log.d(TAG, "Platform:" + platform);
                }

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mResolvedService = serviceInfo;
                //Adding the Device Info to database
                DeviceDao deviceDao = DeviceDatabase.getDatabase(mContext).deviceDao();
                Device device = deviceDao.getDevicebyId(serviceInfo.getServiceName());
                if(platform!=null && execType!=null && device==null && serviceInfo.getHost().getHostAddress() != null) {
                    Log.d(TAG,"Creating a device to insert in DB");
                    Device edgedevice = new Device(serviceInfo.getServiceName(), serviceInfo.getHost().getHostAddress(), Integer.toString(Utils.API.INTERNAL_PORT), platform, execType);
                    DeviceDatabase.databaseWriterExecutor.execute(() -> deviceDao.insert(edgedevice));
                }else{
                    Log.i(TAG,"device already exist;hence not adding" + device);
                }

                //Calling getOrchestrationInfo to add it to db
                if(platform!=null && platform.equals("docker")) {
                    Log.d(TAG,"Calling Orchestration API");
                    Utils.setOrchestrationInfoAPI(serviceInfo.getHost().getHostAddress());

                    APIImpl apiiml = new APIImpl(mContext);
                    apiiml.getOrchestrationInfo(serviceInfo.getServiceName());
                    //apiiml.getScoreInfo(serviceInfo.getServiceName());
                }

            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                Log.d(TAG,"Service Registered Successfully");
                mServiceName = NsdServiceInfo.getServiceName();
                mRegisteredService = NsdServiceInfo;
            }
            
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }
            
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }
            
        };
    }
    public void registerService(int port) {
        Log.d(TAG,"Registering the services");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if (mRegisteredService != null) {
                Toast.makeText(mContext, Utils.Constants.RE_REGISTER_MSG, Toast.LENGTH_SHORT).show();
                return;
            }

            NsdServiceInfo serviceInfo = new NsdServiceInfo();
            serviceInfo.setPort(port);
            serviceInfo.setServiceName(mServiceName);
            serviceInfo.setServiceType(SERVICE_TYPE);
            //Setting the platform anf exectype
            serviceInfo.setAttribute(Utils.Constants.EXEC_TYPE, "android");
            serviceInfo.setAttribute(Utils.Constants.PLATFORM, "android");
            mNsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        });
        
    }
    public void discoverServices() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if (isDiscoveryRunning){
                Log.d(TAG,"Discovery already running");
                stopDiscovery();
            }else{
                Log.d(TAG,"Calling Service Discovery");
                mNsdManager.discoverServices(
                        SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
                isDiscoveryRunning = true;
            }
        });

    }
    
    public void stopDiscovery() {
        Log.d(TAG,"Stopping the discovery");
        if(mDiscoveryListener!=null){
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mResolvedService;
    }
    
    public void tearDown() {

        if(mRegisteredService != null){
            mNsdManager.unregisterService(mRegistrationListener);
        }
        if(mDiscoveryListener!=null && isDiscoveryRunning){
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            isDiscoveryRunning = false;
        }
    }
}
