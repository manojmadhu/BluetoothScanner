package com.nipponit.manojm.connection;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by manojm on 7/18/2017.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class SQLiteDatabaseConnection extends SQLiteOpenHelper {

    private static String DB_NAME="DB_SCANNER";
    private static String driver = "net.sourceforge.jtds.jdbc.Driver";
    private static Connection Myconn;
    private static String connString = "";
    private static String Uname = "";
    private static String Pwd = "";

    private static String SYSTEM_STOCK_TABLE = "SYSTEM_STOCK_TABLE";
    private static String SCAN_STOCK_TABLE="SCAN_STOCK_TABLE";
    private static String MATERIAL_MASTER_TABLE="MATERIAL_MASTER_TABLE";
    private static String DEALER_TABLE="DEALER_MASTER";
    private static String USER_TABLE="USER_MASTER";

    private static String Matcode="MATCODE";
    private static String SysQuantity="SYSQUANTITY";
    private static String Description="DESCRIPTION";
    private static String Plant="PLANT";
    private static String Barcode="BARCODE";
    private static String MatBatch="MATBATCH";
    private static String ScanQuantity="SCANQUANTITY";
    private static String User="USER";
    private static String CustomerCode="CUSTOMERCODE";

    private static String DealerCode="DEALERCODE";
    private static String DealerName="DEALERNAME";
    private static String SalesCode="SALESCODE";
    private static String SalesDescription="SALESDESCRIPTION";
    private static String RepCode="REPCODE";
    private static String RepName="REPNAME";

    private static String UserName="USERNAME";
    private static String Password="PASSWORD";



    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    public SQLiteDatabaseConnection(Context context) {
        super(context,DB_NAME,null,1);
    }

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase db) {
        /**Policy for Sync DB in android.**/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);


        String SQLITE_SYS_QRY="CREATE TABLE "+SYSTEM_STOCK_TABLE+" ("+Matcode+" TEXT,"+Description+" TEXT,"+SysQuantity+" NUMERIC,"+Plant+" NUMERIC)";
        String SQLITE_SCN_QRY="CREATE TABLE "+SCAN_STOCK_TABLE+" ("+Barcode+" TEXT,"+Matcode+" TEXT,"+MatBatch+" NUMERIC,"+Description+" TEXT,"+DealerCode+" TEXT,"+ScanQuantity+" NUMERIC,"+User+" TEXT)";
        String SQLITE_MAT_MST="CREATE TABLE "+MATERIAL_MASTER_TABLE+" ("+Matcode+" TEXT,"+Description+" TEXT)";
        String SQLITE_DEA_MST="CREATE TABLE "+DEALER_TABLE+" ("+DealerCode+" NUMERIC,"+DealerName+" TEXT,"+SalesCode+" TEXT,"+SalesDescription+" TEXT,"+RepCode+" NUMERIC,"+RepName+" TEXT)";
        String SQLITE_USR_MST="CREATE TABLE "+USER_TABLE+" ("+UserName+" TEXT,"+Password+" TEXT)";

        db.execSQL(SQLITE_SYS_QRY);
        db.execSQL(SQLITE_SCN_QRY);
        db.execSQL(SQLITE_MAT_MST);
        db.execSQL(SQLITE_DEA_MST);
        db.execSQL(SQLITE_USR_MST);
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+SYSTEM_STOCK_TABLE+"");
        db.execSQL("DROP TABLE IF EXISTS "+SCAN_STOCK_TABLE+"");
        db.execSQL("DROP TABLE IF EXISTS "+MATERIAL_MASTER_TABLE+"");
        db.execSQL("DROP TABLE IF EXISTS "+DEALER_TABLE+"");
        db.execSQL("DROP TABLE IF EXISTS "+USER_TABLE+"");

    }


    public Connection connect(){
        try {
            Class.forName(driver);
            Myconn= DriverManager.getConnection(connString,Uname,Pwd);
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
        return Myconn;
    }


    public ResultSet ReturnSelectData_Host(String Query){
        ResultSet RS=null;
        try{
            Class.forName(driver);
            Myconn= DriverManager.getConnection(connString,Uname,Pwd);
            Statement stmt=Myconn.createStatement();
            RS=stmt.executeQuery(Query);
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
        return RS;
    }



    //<editor-fold desc="DOWNLOADING DATA TASK">

    public void Download_Material_Master(String REP){
        SQLiteDatabase db=this.getWritableDatabase();
        try{
            ResultSet Hst_rs=ReturnSelectData_Host("SELECT [MatCode],[Description] FROM [dbo].[TBL_MaterialMaster]");
            if(Hst_rs!=null){
                while(Hst_rs.next()){
                    ContentValues values=new ContentValues();
                    values.put(Matcode,Hst_rs.getString(1));
                    values.put(Description,Hst_rs.getString(2));
                    db.insert(MATERIAL_MASTER_TABLE,null,values);
                }

                Download_Dealer_Data(REP);
                Download_User_Data();
            }
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
    }

    public void Download_Dealer_Data(String Rep){
        SQLiteDatabase db=this.getWritableDatabase();
        try{
            Statement stmtD=Myconn.createStatement();
            ResultSet Hst_rs=stmtD.executeQuery("SELECT [DealerCode],[DealerName],[Area],[AreaName],[RepCode],[RepName] FROM [dbo].[TBL_DealerMaster] WHERE [RepCode]="+Rep+"");
            if(Hst_rs!=null){
                while(Hst_rs.next()){
                    ContentValues values=new ContentValues();
                    values.put(DealerCode,Hst_rs.getString(1));
                    values.put(DealerName,Hst_rs.getString(2));
                    values.put(SalesCode,Hst_rs.getString(3));
                    values.put(SalesDescription,Hst_rs.getString(4));
                    values.put(RepCode,Hst_rs.getString(5));
                    values.put(RepName,Hst_rs.getString(6));
                    db.insert(DEALER_TABLE,null,values);
                }
            }
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
    }

    public void Download_User_Data(){
        SQLiteDatabase db=this.getWritableDatabase();
        try{
            Statement stmtU=Myconn.createStatement();
            ResultSet Hst_rs=stmtU.executeQuery("SELECT [UserName],[Password] FROM [dbo].[TBL_Users]");
            if(Hst_rs!=null){
                while (Hst_rs.next()){
                    ContentValues values=new ContentValues();
                    values.put(UserName,Hst_rs.getString(1));
                    values.put(Password,Hst_rs.getString(2));
                    db.insert(USER_TABLE,null,values);
                }
            }
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
    }

    public ResultSet Download_Scan_Stock(String REP,String SyDate,String type){
        ResultSet myRslt=null;
        try{
            Class.forName(driver);
            Myconn= DriverManager.getConnection(connString,Uname,Pwd);
            if(Myconn!=null) {
                String Query="";
                if(type.equals("All")){
                    Query="SELECT Description,MatCode,'N/A',sum(ScanQty),'N/A' FROM TBL_ScanStockMaterials WHERE CONVERT(varchar(10),SyncDate,126)='" + SyDate + "' and RepCode='" + REP + "'" +
                            " group by Description,MatCode order by Description asc";
                }else{
                    Query="SELECT Description,MatCode,Batch,sum(ScanQty),SUBSTRING(barcode,27,6) as ExDate FROM TBL_ScanStockMaterials WHERE CONVERT(varchar(10),SyncDate,126)='" + SyDate + "' and RepCode='" + REP + "'" +
                            " group by Batch,Description,MatCode,SUBSTRING(barcode,27,6) order by Description asc";
                }

                Statement stmtSS = Myconn.createStatement();
                ResultSet Hst_rs = stmtSS.executeQuery(Query);
                if (Hst_rs != null) {
                    myRslt = Hst_rs;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return myRslt;
    }
    //</editor-fold>




    //<editor-fold desc="UPLOAD SCAN MATERIALS TO HOST.">
    public boolean Upload_Scan_Data(String DeaCode){
        boolean isTrue=false;
        SQLiteDatabase db=this.getReadableDatabase();
        int LocCount=0,HstCount=0;
        try{
            connect();
            Statement stmt=Myconn.createStatement();

            Cursor cur;
            String[] colmun = {Barcode,Matcode,Description,ScanQuantity,DealerCode,User,MatBatch};
            cur=db.query(SCAN_STOCK_TABLE,colmun,DealerCode+"='"+DeaCode+"'",null,null,null,null);
            LocCount=cur.getCount();
            if(LocCount > 0){
                while (cur.moveToNext()){
                    String barcode=cur.getString(0);
                    String matcode=cur.getString(1);
                    String description=cur.getString(2);
                    String scnQty=cur.getString(3);
                    String ccode=cur.getString(4);
                    String usr=cur.getString(5);
                    String batch=cur.getString(6);

                    String Query="INSERT INTO [dbo].[TBL_ScanStockMaterials] ([Barcode],[MatCode],[Description],[ScanQty],[CustomerCode],[RepCode],[SyncDate],[Batch]) OUTPUT inserted.ID VALUES " +
                            "('"+barcode+"','"+matcode+"','"+description+"',"+scnQty+","+ccode+","+usr+",GETDATE(),"+batch+")";
                    boolean Iinsert = stmt.execute(Query);
                    if(Iinsert==true){
                        HstCount=HstCount+1;
                    }
                }
                if(LocCount==HstCount) {
                    isTrue = true;
                    Delete_Uploaded_Stock(DeaCode);
                }
            }
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
        return isTrue;
    }
    //</editor-fold>


    /** TO GET SCAN ITEM LIST **/
    public Cursor Retrview_Scan_Items(String customer){
        Cursor mycursor=null;
        SQLiteDatabase db=this.getReadableDatabase();
        try{
            String [] column={Description,Matcode,ScanQuantity,MatBatch,"substr("+Barcode+",27,6)"};
            mycursor=db.query(SCAN_STOCK_TABLE,column,DealerCode+"='"+customer+"'",null,null,null,null);
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
        return mycursor;
    }


    /**TO GET DESCRIPTION OF MATERIAL**/
    public String Get_Mat_Description(String matcode){
        String Matdesc="";
        Cursor mycursor;
        SQLiteDatabase db=this.getReadableDatabase();
        try{
            String [] column={Description};
            mycursor=db.query(MATERIAL_MASTER_TABLE,column,Matcode+"='"+matcode+"'",null,null,null,null);
            if(mycursor!=null){
                while (mycursor.moveToNext()){
                    String desc=mycursor.getString(0);
                    Matdesc=desc;
                }
            }
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
        return Matdesc;
    }


    /** CHECK IS INSERT THIS BARCODE **/
    public boolean CheckIsDataAlreadyInDBorNot(String barcode) {
        boolean status = false;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String [] column={Barcode};
            Cursor cur = db.query(SCAN_STOCK_TABLE,column,Barcode+"='"+barcode+"'",null,null,null,null);
            int count = cur.getCount();
            if (count > 0) {
                status = true;
            }
        }catch (Exception ex){Log.w("Error",ex.getMessage());}

        return status;
    }


    /** INSERT SCAN ADTA TO LOCAL **/
    public boolean Insert_Scanned_Data_Local(String barcode,String matcode,String batch,String description,int qty,int totqty,String customer,String user){
        SQLiteDatabase db=this.getWritableDatabase();
        boolean insert=false;
        try{

            Cursor Inscursor=db.rawQuery("SELECT * FROM "+SCAN_STOCK_TABLE+" WHERE "+Matcode+"='"+matcode+"' AND "+MatBatch+"="+batch+" AND "+DealerCode+"='"+customer+"'",null);
            if(Inscursor.getCount()>0){
                ContentValues values=new ContentValues();
                values.put(ScanQuantity,qty);
                db.update(SCAN_STOCK_TABLE,values,Matcode+"='"+matcode+"' AND "+MatBatch+"="+batch+" AND "+DealerCode+"='"+customer+"'",null);
            }
            else{
            ContentValues values=new ContentValues();
            values.put(Barcode,barcode);
            values.put(Matcode,matcode);
            values.put(MatBatch,batch);
            values.put(Description,description);
            values.put(ScanQuantity,totqty);
            values.put(DealerCode,customer);
            values.put(User,user);
            long result=db.insertOrThrow(SCAN_STOCK_TABLE,null,values);

            if(result==-1)
                insert = false;
            else
                insert = true;
            }
        }catch (Exception ex)
        {
            Log.w("Error",ex.getMessage());
        }
        return insert;
    }



    /** TO GET SCANNED QUANTITY OF MATERIAL **/
    public int[] Get_Scanned_Qty(String matcode,String batch,String customer){
        int[] Quantity=new int[2];
        Cursor mycursor_batch;
        Cursor mycursor_material;
        SQLiteDatabase db=this.getReadableDatabase();
        try{
            String[] column={ScanQuantity};
            String QUERY_batchcount= "SELECT SUM("+ScanQuantity+") FROM "+SCAN_STOCK_TABLE+" WHERE "+Matcode+"='"+matcode+"' AND "+MatBatch+"="+batch+" AND "+DealerCode+"='"+customer+"'";
            String QUERY_matcount="SELECT SUM("+ScanQuantity+") FROM "+SCAN_STOCK_TABLE+" WHERE "+Matcode+"='"+matcode+"' AND "+DealerCode+"='"+customer+"'";

            mycursor_batch=db.rawQuery(QUERY_batchcount,null);
            if(mycursor_batch!=null){
                while(mycursor_batch.moveToNext()){
                    Quantity[0]=mycursor_batch.getInt(0);
                }
            }

            mycursor_material=db.rawQuery(QUERY_matcount,null);
            if(mycursor_material!=null){
                while (mycursor_material.moveToNext()){
                    Quantity[1]=mycursor_material.getInt(0);
                }
            }
        }
        catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
        return Quantity;
    }


    /** DELETE UPLOADED MATERIALS **/
    public void Delete_Uploaded_Stock(String cuscode){
        SQLiteDatabase db=this.getWritableDatabase();
        try{
            long d = db.delete(SCAN_STOCK_TABLE,DealerCode+"='"+cuscode+"'",null);

        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
    }

    /** DELETE PREVIOUS MATERIAL MASTER**/
    public void Delete_Mat_master() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            long d = db.delete(MATERIAL_MASTER_TABLE, null, null);

        } catch (Exception ex) {
            Log.w("Error", ex.getMessage());
        }
    }
    /** DELETE USER TABLE **/
    public void Delete_User_master(){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            long d = db.delete(USER_TABLE, null, null);

        } catch (Exception ex) {
            Log.w("Error", ex.getMessage());
        }
    }

    /** DELETE DEALER MASTER**/
    public void Delete_Cus_master(){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            long d = db.delete(DEALER_TABLE, null, null);

        } catch (Exception ex) {
            Log.w("Error", ex.getMessage());
        }
    }







    /** SELECT AREAS **/
    public Cursor Select_Areas(){
        Cursor cur = null;
        SQLiteDatabase db=this.getReadableDatabase();
        try{
            String [] col={SalesDescription};
            cur=db.query(true,DEALER_TABLE,col,null,null,null,null,null,null);

        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
        return cur;
    }


    /** SELECT CUSTOMERS **/
    public Cursor Select_Customers(String area){
        Cursor cur=null;
        SQLiteDatabase db=this.getReadableDatabase();
        try{
            String[] col={DealerCode,DealerName};
            cur=db.query(true,DEALER_TABLE,col,SalesDescription+"='"+area+"'",null,null,null,null,null);
        }catch (Exception ex){
            Log.w("Error",ex.getMessage());
        }
        return cur;
    }

    public boolean Validate_customer(String cus){
        boolean istrue=false;
        try{
            SQLiteDatabase db=this.getReadableDatabase();
            String [] col={DealerName};
            Cursor cur=db.query(DEALER_TABLE,col,DealerCode+"="+cus,null,null,null,null);
            if(cur.getCount()>0){
                istrue=true;
            }
        }catch (Exception ex){Log.w("Error",ex.getMessage());}
        return istrue;
    }
}
