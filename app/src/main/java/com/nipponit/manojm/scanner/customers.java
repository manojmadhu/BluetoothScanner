package com.nipponit.manojm.scanner;

/**
 * Created by manojm on 7/21/2017.
 */

public class customers {
    private String Ccode;
    private String Cname;

    public customers(String ccode,String cname){
        super();
        this.setCcode(ccode);
        this.setCname(cname);
    }


    public String getCcode() {
        return Ccode;
    }

    public void setCcode(String ccode) {
        Ccode = ccode;
    }

    public String getCname() {
        return Cname;
    }

    public void setCname(String cname) {
        Cname = cname;
    }
}
