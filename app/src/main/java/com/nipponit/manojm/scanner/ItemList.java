package com.nipponit.manojm.scanner;

/**
 * Created by manojm on 7/19/2017.
 */

public class ItemList {
    private String code;
    private String desc;
    private String qty;
    private String batch;
    private String exdate;

    public ItemList(String Mcode,String Mdesc,String Mqty,String Mbatch,String Exdate){
        super();
        this.setCode(Mcode);
        this.setDesc(Mdesc);
        this.setQty(Mqty);
        this.setBatch(Mbatch);
        this.setExdate(Exdate);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getBatch(){
        return batch;
    }
    public void setBatch(String batch){
        this.batch=batch;
    }

    public String getExdate(){return exdate;}
    public void setExdate(String exdate){this.exdate=exdate;}
}
