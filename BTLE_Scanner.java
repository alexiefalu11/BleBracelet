package com.example.alexiefalu.ble_1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

import java.io.IOException;
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
    private int RSSI;
    private String name;
    private BluetoothDevice ble2;

    public BTLE_Scanner(MainActivity mainActivity, long scanPeriod, int signalStrength)
    {
        ma = mainActivity;

        mScanning = false;

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
          //  BTLE_Utils.toast(ma.getApplicationContext(), "Start Bracelet Scan...");

            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
             //       BTLE_Utils.toast(ma.getApplicationContext(), "Stop Bracelet Scan...");

                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    ma.stopScan();

             //       mScanning = true;
             //       mBluetoothAdapter.startLeScan(mLeScanCallback);
             //       mHandler.postDelayed(this,5000);

                }
            }, scanPeriod);


            mScanning = true;

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        else if(!enable){
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback(){
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecords){

                            final int new_rssi = rssi;
                            final int flag = scanRecords[2]& 0xFF;
                                name = device.getName();
                                RSSI = new_rssi;

                  //  ma.addDevice(device, new_rssi);
                               if (new_rssi > signalStrength && /*device.getName().contains("BabyBracelet")&& */flag==0x01){
                                   mHandler.post(new Runnable() {
                                       @Override
                                       public void run() {
                               ma.addDevice(device, new_rssi);
                              //             setBle(device);
                           }
                       });
                    }

                }

            };
    private int parseAdvertisementPacket(byte[] scanRecords){

        int currentPos = 0;
        int advertiseFlag = -1;

        while (currentPos < scanRecords.length) {

            int length = scanRecords[currentPos++] & 0xFF;

            int dataLength = length - 1;

            int fieldType = scanRecords[currentPos++] & 0xFF;
            switch (fieldType) {
                case 0x11:
                    advertiseFlag = scanRecords[currentPos] & 0xFF;
                    break;
                default:
                    break;
            }
            currentPos += dataLength;
        }


        return advertiseFlag;
    }
    public int getRSSI(){
        return RSSI;
    }
    public String getName(){
        return name;
    }
    public void makeEmpty(){

        ma.addDevice(getBle(), 0);
    }
    public BluetoothDevice getBle(){
        return ble2;
    }
    public void setBle(BluetoothDevice ble){
         ble2 = ble;
    }

}
