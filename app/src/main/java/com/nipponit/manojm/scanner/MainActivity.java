package com.nipponit.manojm.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nipponit.manojm.connection.SQLiteDatabaseConnection;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {

    SQLiteDatabaseConnection dbconnection;
    String CustomerCode="",User="111";
    ArrayAdapter<String> listAdapter;

    private Menu menu;

    TextView txt,lblcusname;
    EditText txtmatdesc,txtmatcode,txtmatbatch,txtExMonth,txtmqty,txtbqty,txtsqty;
    Button btnSend,btnViewScan,btnSysStock;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    protected static final int DEVICE_DISCONNECT=2;
    IntentFilter filter;
    BroadcastReceiver receiver;
    String tag = "debugging";

    RelativeLayout RL;

    private BluetoothSocket mmSocket;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.i(tag, "in handler");
            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    // DO something
                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_SHORT).show();
                    String s = "successfully connected";
                    connectedThread.write(s.getBytes());
                    Log.i(tag, "connected");

                    menu.getItem(0).setIcon(getResources().getDrawable(R.mipmap.ic_bluetooth_connected_white_36dp));

                    break;
                case MESSAGE_READ:

                    byte[] readBuf = (byte[])msg.obj;
                    StringBuffer sb=new StringBuffer();
                    String string = new String(readBuf);
                    sb.append(string);


                    //txt.setText(sb);
                    break;
                case DEVICE_DISCONNECT:
                   menu.getItem(0).setIcon(getResources().getDrawable(R.mipmap.ic_bluetooth_white_36dp));
                    //txt.setText("DEVICE DISCONNECTED.");
                    break;
            }
        }
    };


    private class ConnectedThread extends Thread {


        //private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            //myDb=new SQLITEDATABASE(getApplication());
            byte[] buffer = new byte[1024]; // buffer store for the stream
            final StringBuffer sb = new StringBuffer();
            int bytes; // bytes returned from read()

            try {
                // Keep listening to the InputStream until an exception occurs
                while (true) {
                    try {
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);


                        // Send the obtained bytes to the UI activity
//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();

                        String recived = new String(buffer, 0, bytes);
                        sb.append(recived);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (sb.lastIndexOf("*") != -1) {

                                    if(sb.length() == 37){
                                        if(sb.substring(0,2).equals("47")) {

                                            String Barcode = sb.toString();
                                            String Matcode = sb.substring(2, 14);
                                            String Batch = sb.substring(14, 24);
                                            int Qty = Integer.parseInt(sb.substring(24, 26));
                                            String ExDate = sb.substring(26,32);

                                            ExDate = (ExDate.substring(0,4)+"-"+ExDate.substring(4,6));

                                            sb.setLength(0);
                                            Insert_Scan_Data_Local(Barcode.substring(0, 36), Matcode,Qty,Batch,ExDate);

                                        }
                                    }



                                    else if (sb.length() == 31) {

                                        if(sb.substring(0,2).equals("47")) {

                                            String Barcode = sb.toString();
                                            String Matcode = sb.substring(2, 14);
                                            String Batch = sb.substring(14, 24);
                                            int Qty = Integer.parseInt(sb.substring(24, 26));

                                            sb.setLength(0);
                                            Insert_Scan_Data_Local(Barcode.substring(0, 30), Matcode, Qty,Batch,"");

                                        }

                                    } else if (sb.length() == 29) {

                                        if (sb.substring(0, 2).equals("47")) {
                                            String Barcode = sb.toString();
                                            String Matcode = sb.substring(2, 14);
                                            String Batch = sb.substring(14, 24);

                                            sb.setLength(0);
                                            Insert_Scan_Data_Local(Barcode.substring(0, 28), Matcode, 1,Batch,"");
                                        }

                                    } else {
                                        sb.setLength(0);
                                        txt.setText("Scanned invalid barcode");
                                        txt.setTextColor(Color.RED);
                                        ClearFields();
                                    }
                                }


                            }



                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                        ClearFields();
                        sb.setLength(0);
                        mHandler.obtainMessage(DEVICE_DISCONNECT,2).sendToTarget();


                    }

                }
            }catch (Exception ex){
                ex.printStackTrace();
            }


        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                mmOutStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }


    }


    private void ClearFields(){
        txt.setText("");
        txtmatdesc.setText("");
        txtmatcode.setText("");
        txtmatbatch.setText("");
        txtsqty.setText("");
        txtExMonth.setText("");
        txtbqty.setText("");

    }


    private void initialize() {
        // TODO Auto-generated method stub

        //listView=(ListView)findViewById(R.id.listView);
        //listView.setOnItemClickListener(this);

        try {


            listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);

            //listView.setAdapter(listAdapter);

            btAdapter = BluetoothAdapter.getDefaultAdapter();
            pairedDevices = new ArrayList<String>();
            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            devices = new ArrayList<BluetoothDevice>();
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // TODO Auto-generated method stub
                    String action = intent.getAction();

                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        devices.add(device);
                        String s = "";
                        for (int a = 0; a < pairedDevices.size(); a++) {
                            if (device.getName().equals(pairedDevices.get(a))) {
                                //append
                                s = "(Paired)";
                                break;
                            }
                        }

                        listAdapter.add(device.getName() + " " + s + " " + "\n" + device.getAddress());
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        // run some code
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        // run some code


                    } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                        if (btAdapter.getState() == btAdapter.STATE_OFF) {
                            turnOnBT();
                        }
                    }

                }
            };

            registerReceiver(receiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            registerReceiver(receiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(receiver, filter);
        }catch (Exception ex){

        }
    }

    private void turnOnBT() {
        // TODO Auto-generated method stub
        Intent intent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    private void startDiscovery() {
        // TODO Auto-generated method stub
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();

    }

    private void getPairedDevices() {
        // TODO Auto-generated method stub
        devicesArray = btAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray){
                pairedDevices.add(device.getName());

            }
        }
    }


    class ConnectingDevice extends AsyncTask<Integer,Integer,String>{

        ProgressDialog prgDialog=new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            prgDialog.setMessage("Connecting to Device...");
            prgDialog.show();
        }

        @Override
        protected String doInBackground(Integer... params) {

            try {
                int position = params[0];

                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }
                if (listAdapter.getItem(position).contains("Paired")) {

                    BluetoothDevice selectedDevice = devices.get(position);
                    ConnectThread connect = new ConnectThread(selectedDevice);
                    connect.start();
                    Log.i(tag, "in click listener");
                } else {
                    Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception ex)
            {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            prgDialog.dismiss();
        }
    }


    private class ConnectThread extends Thread {

        //private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {

            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.i(tag, "construct");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.i(tag, "get socket failed");

            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();
            Log.i(tag, "connect - run");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();

                Log.i(tag, "connect - succeeded");


//                Intent intact=new Intent(Main_Activity.this,MainActivity2.class);
//                startActivity(intact);



                startThreadConnected(mmSocket);

            } catch (IOException connectException) {	Log.i(tag, "connect failed");
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)

            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }



        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void startThreadConnected(BluetoothSocket socket){

        ConnectedThread myThreadConnected = new ConnectedThread(socket);
        myThreadConnected.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras=getIntent().getExtras();
        CustomerCode = extras.get("cusCode").toString();
        String cusname = extras.get("cusName").toString();


        /** Initialize filed **/
        txt=(TextView)findViewById(R.id.txt);
        txtmatdesc=(EditText)findViewById(R.id.txtMatDesc);
        txtmatcode=(EditText)findViewById(R.id.txtMatCode);
        txtmatbatch=(EditText)findViewById(R.id.txtMatBatch);
        txtExMonth=(EditText)findViewById(R.id.txtMatExMonth);
//        txtmqty=(EditText)findViewById(R.id.txtMQty);
        txtbqty=(EditText)findViewById(R.id.txtBQty);
        txtsqty=(EditText)findViewById(R.id.txtSQty);
        lblcusname=(TextView)findViewById(R.id.lblcusName);
        lblcusname.setText(cusname);



        /** Initialize bluetooth configuration **/
        initialize();
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!btAdapter.isEnabled()) {
                turnOnBT();
            }

            getPairedDevices();
            startDiscovery();
        }



        /** Button click events **/
        btnSend=(Button)findViewById(R.id.btnSend);
        btnViewScan=(Button)findViewById(R.id.btnViewScan);
        btnSysStock=(Button)findViewById(R.id.btnSysStock);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Uploading Scanned Data");
                dialog.setMessage("Are you sure to send scanned data.?");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Upload_Scan_Data().execute();
                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        btnViewScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listIntent=new Intent(MainActivity.this,ListActivity.class);
                listIntent.putExtra("Dcode",CustomerCode);
                startActivity(listIntent);

            }
        });


        btnSysStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sysint=new Intent(MainActivity.this,ScanedActivity.class);
                sysint.putExtra("REP","111");
                startActivity(sysint);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu=menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id==R.id.action_favorite){
            try {

                final AlertDialog alertz = new AlertDialog.Builder(MainActivity.this).create();


                alertz.setTitle("Select Device");

                View convertView = (View) getLayoutInflater().inflate(R.layout.list_devices, null);
                alertz.setView(convertView);
                ListView lv = (ListView) convertView.findViewById(R.id.lstdevices);

                lv.setAdapter(listAdapter);


                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        new ConnectingDevice().execute(position);

                        alertz.dismiss();
                    }
                });


                alertz.show();

            }catch (Exception ex){

            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub

        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }
        if(listAdapter.getItem(position).contains("Paired")){

            BluetoothDevice selectedDevice = devices.get(position);
            ConnectThread connect = new ConnectThread(selectedDevice);
            connect.start();
            Log.i(tag, "in click listener");
        }
        else{
            Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private static long back_pressed;
    @Override
    public void onBackPressed() {

        if (back_pressed + 2000 > System.currentTimeMillis()) {

            try {

                if(mmSocket!=null)
                    mmSocket.close();

                Toast.makeText(getBaseContext(), "Bluetooth disconnected.!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }





    class Upload_Scan_Data extends AsyncTask<String,String,String>{

        ProgressDialog prgDialog=new ProgressDialog(MainActivity.this);
        boolean Isupload=false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prgDialog.setMessage("Please wait.Uploading Scanned Data");
            prgDialog.setTitle("Data Uploading");
            prgDialog.setCancelable(false);
            prgDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            dbconnection=new SQLiteDatabaseConnection(getApplicationContext());
            Isupload=dbconnection.Upload_Scan_Data(CustomerCode);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            prgDialog.dismiss();
            if(Isupload==true) {
                Toast.makeText(getApplicationContext(), "Successfully Uploaded Scan Materials.", Toast.LENGTH_SHORT).show();
                ClearFields();
            }
            else
                Toast.makeText(getApplicationContext(),"Error On Material Uploading.",Toast.LENGTH_SHORT).show();
        }
    }


    private void Insert_Scan_Data_Local(String barcode,String matcode,int qty,String batch,String exmonth){
        try
        {
            ClearFields();

            dbconnection=new SQLiteDatabaseConnection(getApplicationContext());
            int[] ScnQuantity = new int[2];
            int _batchQty=0,_matQty=0;

            boolean IsScanned = dbconnection.CheckIsDataAlreadyInDBorNot(barcode);
            if(IsScanned==false) {

                String Description = dbconnection.Get_Mat_Description(matcode);
                txtmatdesc.setText(Description);
                txtmatcode.setText(matcode);
                txtmatbatch.setText(batch);
                txtExMonth.setText(exmonth);

                ScnQuantity = dbconnection.Get_Scanned_Qty(matcode,batch,CustomerCode);
                _batchQty=ScnQuantity[0];
                _matQty=ScnQuantity[1];

                _batchQty = _batchQty + qty;
                _matQty=_matQty+qty;

                txtsqty.setText( String.valueOf(_matQty));
                txtbqty.setText(String.valueOf(_batchQty));

                boolean InsertOk=dbconnection.Insert_Scanned_Data_Local(barcode,matcode,batch,Description,_batchQty,qty,CustomerCode,User);
                if(InsertOk==true)
                    Toast.makeText(getApplicationContext(),"Done",Toast.LENGTH_SHORT).show();


            }else
            {
                txt.setText("Already Scanned Material.");
                txt.setTextColor(Color.RED);
            }


        }catch (Exception ex){
                Log.w("error",ex.getMessage());
        }
    }

}
