package com.example.alexiefalu.ble_1;

import android.bluetooth.BluetoothDevice;
    /**
     * Created by Alexie Falu on 24/09/2016.
     */

    public class BTLE_Device {

        private BluetoothDevice bluetoothDevice;
        private  int rssi;

        //Set identifire of remote device
        public BTLE_Device(BluetoothDevice bluetoothDevice) {this.bluetoothDevice = bluetoothDevice;}
        //Return the hardware address of local Bluetooth adapter
        public String getAddress(){return bluetoothDevice.getAddress();}
        //Get the Bluetooth name of Bracelet
        public String getName() { return bluetoothDevice.getName();}
        //Set RSSI
        public void setRSSI(int rssi){this.rssi = rssi;}
        //Get RSSI
        public int getRSSI(){return rssi;}
    }




