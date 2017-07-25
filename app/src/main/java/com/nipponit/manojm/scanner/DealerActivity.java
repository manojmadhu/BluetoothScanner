package com.nipponit.manojm.scanner;
import com.nipponit.manojm.connection.SQLiteDatabaseConnection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class DealerActivity extends AppCompatActivity {

    SQLiteDatabaseConnection connection;
    AutoCompleteTextView areaText,customerText;
    ArrayAdapter<String> Areaadapter,Cusadapter;
    String Dcode;
    //ArrayAdapter<customers> Cusadapter;

    private List<customers> customer_list=new ArrayList<>();

    Button btnContinue,btndownload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dealer);

        areaText=(AutoCompleteTextView) findViewById(R.id.area_txt);
        customerText=(AutoCompleteTextView)findViewById(R.id.customer_txt);
        btnContinue=(Button)findViewById(R.id.btnContinue);
        btndownload=(Button)findViewById(R.id.btndownload);

        Areaadapter=new ArrayAdapter<>(DealerActivity.this,android.R.layout.simple_list_item_1);
        Cusadapter=new ArrayAdapter<>(DealerActivity.this,android.R.layout.simple_list_item_1);



        LoadArea();

        areaText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),Areaadapter.getItem(position),Toast.LENGTH_SHORT).show();

                LoadCustomer(Areaadapter.getItem(position));
                customerText.setFocusable(true);
            }
        });

        customerText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data=Cusadapter.getItem(position);
                Dcode=data.substring(0,10);
                customerText.setText(data.substring(11));
            }
        });

        btndownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(DealerActivity.this);
                dialog.setTitle("Downloading Data.");
                dialog.setMessage("Are you sure to download data.? \n This will clear and update previous master data in the system.");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AsyncDownload().execute();
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

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String custxt=customerText.getText().toString();
                connection=new SQLiteDatabaseConnection(getApplicationContext());
                boolean istrue=connection.Validate_customer(Dcode);
                if(!custxt.equals("") && istrue==true) {
                    Intent intCont = new Intent(DealerActivity.this, MainActivity.class);
                    intCont.putExtra("cusCode",Dcode);
                    intCont.putExtra("cusName", customerText.getText());
                    startActivity(intCont);
                }else
                    Toast.makeText(getApplicationContext(),"Please select a customer",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void LoadArea(){
        try{
            connection=new SQLiteDatabaseConnection(getApplicationContext());
            Cursor Acursor=connection.Select_Areas();
            if(Acursor!=null){
                while (Acursor.moveToNext()){
                    Areaadapter.add(Acursor.getString(0));
                }
            }
            areaText.setAdapter(Areaadapter);
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
    }


    private void LoadCustomer(String area){
        try{
            connection=new SQLiteDatabaseConnection(getApplicationContext());
            customerText.setText("");
            Cusadapter.clear();
            Cursor Ccursor=connection.Select_Customers(area);
            if(Ccursor.getCount()>0){
                while (Ccursor.moveToNext()){
                    String ccode=Ccursor.getString(0);
                    String cname = Ccursor.getString(1);
                    Cusadapter.add(ccode+"-"+cname);
                }
            }
            customerText.setAdapter(Cusadapter);
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
    }



    class AsyncDownload extends AsyncTask<String,String,Integer>{
        ProgressDialog prgd=new ProgressDialog(DealerActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prgd.setTitle("Download Data");
            prgd.setMessage("Please wait downloading data.!");
            prgd.setCancelable(false);
            prgd.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            connection=new SQLiteDatabaseConnection(getApplicationContext());

            connection.Delete_Cus_master();
            connection.Delete_Mat_master();
            connection.Delete_User_master();

            connection.Download_Material_Master("00000010");

            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            Toast.makeText(getApplicationContext(),"Download complete.",Toast.LENGTH_SHORT).show();
            prgd.dismiss();
            finish();
            startActivity(getIntent());
        }
    }



}
