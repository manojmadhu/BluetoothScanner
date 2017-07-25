package com.nipponit.manojm.scanner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.v7.app.AppCompatActivity;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.Filter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.nipponit.manojm.connection.SQLiteDatabaseConnection;

public class ScanedActivity extends AppCompatActivity {

    EditText txtDate;
    Button btnView;
    KeyListener mkeylistener;
    SQLiteDatabaseConnection connection;
    private List<ItemList> Items=new ArrayList<ItemList>();
    ArrayAdapter<ItemList> ListAdapter=null;
    ListView list;
    private String REP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaned);

        Bundle data=getIntent().getExtras();
        REP=data.getString("REP");

        txtDate=(EditText)findViewById(R.id.txtDate);
        btnView=(Button)findViewById(R.id.btnSync);
        mkeylistener=txtDate.getKeyListener();
        txtDate.setKeyListener(null);
        list=(ListView)findViewById(R.id.lstScanned);
    }


    public void showDatePicker(View v){
        DialogFragment newFragment=new DatePickerFragment();
        newFragment.show(getFragmentManager(),"datePicker");
    }

    public void DownloadStock(View v){
        try{
        new SyncScan().execute("");}catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
    }

    public void DownloadStockAll(View v){
        try{
            new SyncScan().execute("All");
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
    }



    class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }


        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String Month=String.format("%02d",(month+1));
            String Date=String.format("%02d",(dayOfMonth));
            txtDate.setText(String.valueOf(year)+"-"+Month+"-"+Date);

        }
    }


    class SyncScan extends AsyncTask<String,Integer,Integer>{

        String Mdate=txtDate.getText().toString();
        ProgressDialog prgDialog=new ProgressDialog(ScanedActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prgDialog.setTitle("Loading Data");
            prgDialog.setMessage("Please wait.. Downloading "+Mdate+" data.");
            prgDialog.setCancelable(false);
            prgDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {

            try {
                if(!Items.isEmpty()){
                    Items.clear();
                }
                connection = new SQLiteDatabaseConnection(getApplicationContext());
                ResultSet RS;
                String type=params[0].toString();
                if(type.equals("All")){
                    RS=connection.Download_Scan_Stock(REP,Mdate,type);
                }else
                {
                    RS = connection.Download_Scan_Stock(REP,Mdate,type);
                }


                if (RS != null) {
                    while (RS.next()){
                        String edate="N/A";
                        if(RS.getString(5).length()==6)
                            edate=RS.getString(5).substring(0,4)+"-"+RS.getString(5).substring(4);
                        ADD(RS.getString(1),RS.getString(2),RS.getString(3),RS.getInt(4),edate);
                    }
                }
            }catch (NetworkOnMainThreadException ex){
                Log.w("Error",ex.getMessage());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            catch (Exception ex){
                Log.w("Error",ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            list.setAdapter(ListAdapter);
            prgDialog.dismiss();
        }
    }


    public void ADD(String desc,String code,String batch,int qty,String exdate){
        Items.add(new ItemList(code,desc,String.valueOf(qty),batch,exdate));
        ListAdapter=new List_Adapter_class();
    }

    private class List_Adapter_class extends ArrayAdapter<ItemList> {

        private ArrayList<ItemList> orgList;
        private ArrayList<ItemList> stockItemlist;
        private List_Adapter_class.ListFilter filter;

        public List_Adapter_class(){
            super(ScanedActivity.this,R.layout.list_row,Items);
            this.orgList=new ArrayList<ItemList>();
            this.orgList.addAll(Items);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            View itemview=convertView;

            if(itemview==null){
                itemview=getLayoutInflater().inflate(R.layout.list_row,parent,false);
            }

            ItemList current=Items.get(position);
            TextView txtdesc=(TextView)itemview.findViewById(R.id.txtmdesc);
            txtdesc.setText(current.getDesc());

            TextView txtcode=(TextView)itemview.findViewById(R.id.txtmcode);
            txtcode.setText(current.getCode());

            TextView txtbatch=(TextView)itemview.findViewById(R.id.txtmbatch);
            txtbatch.setText(current.getBatch());

            TextView txtqty=(TextView)itemview.findViewById(R.id.txtmqty);
            txtqty.setText(current.getQty());

            TextView txtedate=(TextView)itemview.findViewById(R.id.txtmedate);
            txtedate.setText(current.getExdate());



            return itemview;
        }

        @Override
        public Filter getFilter() {
            if(filter==null){
                filter=new List_Adapter_class.ListFilter();
            }
            return filter;
        }

        private class ListFilter extends Filter{

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint.toString().toUpperCase();
                FilterResults results=new FilterResults();
                if(constraint !=null && constraint.toString().length()>0){
                    ArrayList<ItemList> filteredItems=new ArrayList<ItemList>();
                    for(int i=0 , l = orgList.size();i<l;i++){
                        if((orgList.get(i).getDesc().toUpperCase()).contains(constraint.toString().toUpperCase())){
                            ItemList List=new ItemList(
                                    orgList.get(i).getCode(),orgList.get(i).getDesc(),
                                    orgList.get(i).getQty(),orgList.get(i).getBatch(),orgList.get(i).getExdate());
                            filteredItems.add(List);
                        }
                    }

                    results.count=filteredItems.size();
                    results.values=filteredItems;
                }
                else{
                    synchronized (this){
                        results.values=orgList;
                        results.count=orgList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                stockItemlist=(ArrayList<ItemList>)results.values;
                notifyDataSetChanged();
                clear();
                for (int i=0,l=stockItemlist.size();i<l;i++){
                    add(stockItemlist.get(i));
                    notifyDataSetInvalidated();
                }
            }
        }
    }
}
