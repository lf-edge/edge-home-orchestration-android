package org.lfedge.homeedge.common;

import android.content.Context;
import android.util.Log;

import org.lfedge.homeedge.client.APIImpl;
import org.lfedge.homeedge.database.DeviceDao;
import org.lfedge.homeedge.database.DeviceDatabase;
import org.lfedge.homeedge.model.Device;

import java.util.Iterator;
import java.util.List;

public class RoundTripTime implements Runnable{
    Context mContext;
    public static final String TAG="RTT";
    public RoundTripTime(Context mcontext){
        this.mContext = mcontext;
    }

    @Override
    public void run() {
        DeviceDao deviceDao = DeviceDatabase.getDatabase(mContext).deviceDao();
        List<Device> deviceList = deviceDao.getAllDevices();
        Log.d(TAG,"Device List size is " +deviceList.size());
        Iterator<Device> deviceIterator = deviceList.iterator();
        while(deviceIterator.hasNext()) {
            Device device = deviceIterator.next();
            Log.d(TAG,"Device obtained is" +device.getEdge_orch_id());
            Utils.setOrchestrationInfoAPI(device.getIp());
            String targetURL = Utils.BASE_URL + Utils.API.PING_REQUEST;
            APIImpl api = new APIImpl(mContext);
            String response = null;
            try {
                int tryLimit = 12;
                while(response==null && tryLimit>0){
                    response = api.getPing(targetURL);
                    tryLimit--;
                    Thread.currentThread().sleep(5000);
                }
                if(response==null)
                {   Log.d(TAG,"Deleting the device" +device.getEdge_orch_id());
                    deviceDao.deleteDevice(device.getEdge_orch_id());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
