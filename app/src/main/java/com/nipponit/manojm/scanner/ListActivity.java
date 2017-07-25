package com.nipponit.manojm.scanner;

import com.nipponit.manojm.connection.SQLiteDatabaseConnection;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private List<ItemList> Items=new ArrayList<ItemList>();
    ArrayAdapter<ItemList> ListAdapter=null;
    ListView list;
    EditText txtSearch;
    String Dcode;

    SQLiteDatabaseConnection MyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Bundle data=getIntent().getExtras();
        Dcode=data.getString("Dcode");

        new Sync_ScanItem().execute();

        list=(ListView)findViewById(R.id.listItems);
        txtSearch=(EditText)findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    ListAdapter.getFilter().filter(s.toString());
                }catch (Exception  ex){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    class Sync_ScanItem extends AsyncTask<String,Integer,Integer>{

        ProgressDialog prgDialog=new ProgressDialog(ListActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            prgDialog.setTitle("Loading Data");
            prgDialog.setMessage("Please wait.. Loading data.");
            prgDialog.setCancelable(false);
            prgDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try{
                Cursor cur;
                MyDB=new SQLiteDatabaseConnection(getApplicationContext());
                cur = MyDB.Retrview_Scan_Items(Dcode);
                if(cur!=null){
                    while(cur.moveToNext()){
                        String edate="N/A";
                        if(cur.getString(4).length()==6)
                            edate=cur.getString(4).substring(0,4).toString()+"-"+cur.getString(4).substring(4).toString();
                     ADD(cur.getString(0),cur.getString(1),cur.getString(2),cur.getString(3),edate);
                    }
                }

            }catch (Exception ex){
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


    public void ADD(String desc,String code,String qty,String batch,String exdate){
        Items.add(new ItemList(code,desc,qty,batch,exdate));
        ListAdapter=new List_Adapter_class();
    }

    private class List_Adapter_class extends ArrayAdapter<ItemList>{

        private ArrayList<ItemList> orgList;
        private ArrayList<ItemList> stockItemlist;
        private ListFilter filter;

        public List_Adapter_class(){
            super(ListActivity.this,R.layout.list_row,Items);
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

            TextView txtqty=(TextView)itemview.findViewById(R.id.txtmqty);
            txtqty.setText(current.getQty());

            TextView txtbatch=(TextView)itemview.findViewById(R.id.txtmbatch);
            txtbatch.setText(current.getBatch());

            TextView txtedate=(TextView)itemview.findViewById(R.id.txtmedate);
            txtedate.setText(current.getExdate());

            return itemview;
        }

        @Override
        public Filter getFilter() {
            if(filter==null){
                filter=new ListFilter();
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
