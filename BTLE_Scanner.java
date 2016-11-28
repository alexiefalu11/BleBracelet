package com.example.alexiefalu.ble_1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

import java.util.Arrays;


//import java.util.logging.Handler;

/**
 * Created by Alexie Falu on 24/09/2016.
 */

public class BTLE_Scanner {

    private MainActivity ma;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private long scanPeriod;
    private int signalStrength;



    public BTLE_Scanner(MainActivity mainActivity, long scanPeriod, int signalStrength)
    {
        ma = mainActivity;

        mHandler = new Handler();

        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) ma.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isScannig(){
        return mScanning;
    }


    public void start(){
        if(!BTLE_Utils.checkBluetooth(mBluetoothAdapter)){
            BTLE_Utils.requestUserBluetooth(ma);
            ma.stopScan();
        }
        else{
            scanLeDevice(true);
        }
    }


    public void stop(){
        scanLeDevice(false);
    }


    private void scanLeDevice(final boolean enable){

        if(enable && !mScanning) {
            BTLE_Utils.toast(ma.getApplicationContext(), "Start Bracelet Scan...");

            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    BTLE_Utils.toast(ma.getApplicationContext(),"Stopping Bracelet Scan...");

                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    ma.stopScan();
                }
            }, scanPeriod);

            mScanning = true;

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback(){
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecords){

                               final int new_rssi = rssi;
                             int flag = scanRecords[5];

                               if (rssi > signalStrength && device.getName().contains("BabyBracelet") && flag == 0x01){
                                   mHandler.post(new Runnable() {
                                       @Override
                                       public void run() {
                               ma.addDevice(device, new_rssi);
                           }
                       });
                    }
                }
            };
    private boolean parseAdvertisementPacket(final byte[] scanRecords){

        byte[] advertisedData = Arrays.copyOf(scanRecords, scanRecords.length);

        int offset = 0;
        while (offset < (advertisedData.length - 2)){
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch(type){
                case 0x02: // Partial list of 16-bit UUIDs

            }
        }
        return true;
    }

}
