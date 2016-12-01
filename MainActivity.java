package com.example.alexiefalu.ble_1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;
import android.os.Handler;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    DatabaseHelper myDB;
    private final static String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_ENABLE_BT = 1;

    private HashMap<String, BTLE_Device> mBTDevicesHashMap;
    private ArrayList<BTLE_Device> mBTDevicesArrayList;
    private ListAdapter_BTLE_Device adapter;
    private Button btn_Scan;
  //  private ClipData.Item
    private ListView listView;
    private boolean scan = false;
    private Runnable myRunnable;
    private Handler mHandler;
    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private BTLE_Scanner mBTLeScanner;
    private int RSSI;
    MediaPlayer media = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            BTLE_Utils.toast(getApplicationContext(),"BLE not supported");
            finish();
        }

        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        mBTLeScanner = new BTLE_Scanner(this, 3000, -250);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();

        adapter = new ListAdapter_BTLE_Device(this, R.layout.btle_device_list_item, mBTDevicesArrayList);
        mHandler = new Handler();

       // media = MediaPlayer.create(this,R.raw.baby_llanto);
       // media.setLooping(true);

        listView = new ListView(this);
        listView.setAdapter(adapter);

        myDB = new DatabaseHelper(this);
     //   listView.setOnItemClickListener(this);

        btn_Scan = (Button) findViewById(R.id.btn_scan);
        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);
        btn_Scan.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.alerts_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId()){
            case R.id.history_button:
                startActivity(new Intent(MainActivity.this,ViewListAlerts.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart(){
        super.onStart();

        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    protected void onPause(){
        super.onPause();

        stopScan();
    }

    protected void onStop(){
        super.onStop();

        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {   //Check which request we' re responding to
            case REQUEST_ENABLE_BT:
                //Make sure the request was successful
                if(resultCode == Activity.RESULT_OK)
                {
                    Toast.makeText(getApplicationContext(),"Bluetooth Service Active",Toast.LENGTH_LONG).show();
                }
                //Request was rejected , Close app
                else
                {
                    Toast.makeText(getApplicationContext(),"Bluetooth Service Rejected, App was Closed",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {

    }

    @Override
    public void onClick(View v)
    {
        media = MediaPlayer.create(this,R.raw.baby_llanto);
        media.setLooping(true);
        mHandler.removeCallbacks(myRunnable);
        switch (v.getId())
        {
            case R.id.btn_scan:
                BTLE_Utils.toast(getApplicationContext(),"Scan Button Pressed");

                if(!mBTLeScanner.isScannig()){


                    mHandler.post(
                     myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            startScan();
                            RSSI = mBTLeScanner.getRSSI();
                            if(RSSI > -100){
                                scan = true;
                                mHandler.postDelayed(this,100);
                            }
                            else if(RSSI <= -100){
                                String newEntry = mBTLeScanner.getName();
                                AddData(newEntry);
                                scan = false;
                                btn_Scan.setText("Press to Scan Again");
                                mHandler.removeCallbacks(this);
                                mBTDevicesArrayList.clear();
                                mBTDevicesHashMap.clear();
                               // mBTLeScanner.makeEmpty();
                                Vibrator vibrator;
                             //   AudioAttributes audio;
                             //   audio = new AudioAttributes();


                                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                media.start();
                                AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
                                vibrator.vibrate(new long[]{0,500,500,500,500,500,500,500,500,500,500,500},-1);
                                a_builder.setMessage("Bracelet is outside of safe zone ").setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                               dialog.cancel();
                                                media.stop();
                                            }
                                        });
                                AlertDialog alert = a_builder.create();
                                alert.setTitle("RANGE WARNING!!");
                                alert.show();
                            }



                        }});
                    mBTDevicesArrayList.clear();
                    mBTDevicesHashMap.clear();
                    }

                else{
                    scan = false;
                    stopScan();
                }
                break;
            default:
                break;
        }
    }

    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();

        if (!mBTDevicesHashMap.containsKey(address)){
            BTLE_Device btle_device = new BTLE_Device(device);
            btle_device.setRSSI(rssi);

            mBTDevicesHashMap.put(address, btle_device);
            mBTDevicesArrayList.add(btle_device);
        }

        else{
            if(rssi>-127){
            mBTDevicesHashMap.get(address).setRSSI(rssi);}
            }
        adapter.notifyDataSetChanged();
    }

    public void startScan(){
        if(scan){
            btn_Scan.setText("Press to stop Scanning");
        }
        else {
            btn_Scan.setText("Scanning...");
            BTLE_Utils.toast(this, "Start Bracelet Scan...");
        }
        mBTDevicesArrayList.clear();
        mBTDevicesHashMap.clear();

        mBTLeScanner.start();
    }

    public void stopScan(){
        if(!scan) {
            BTLE_Utils.toast(this, "Stop Bracelet Scan...");
            btn_Scan.setText("Scan Again Bracelet");
        }
        mBTLeScanner.stop();
    }

    public void AddData(String newEntry){
        boolean insertData = myDB.addData(newEntry);
    }
}

