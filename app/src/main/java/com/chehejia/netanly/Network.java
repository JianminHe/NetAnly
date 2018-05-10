package com.chehejia.netanly;

import java.util.Date;

/**
 * Created by chj1090 on 2018/5/9.
 */

public class Network {

    private String date;
    private long rx;
    private long tx;

    public Network(String date, long rx, long tx) {
        this.date = date;
        this.rx = rx;
        this.tx = tx;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getRx() {
        return rx;
    }

    public void setRx(long rx) {
        this.rx = rx;
    }

    public long getTx() {
        return tx;
    }

    public void setTx(long tx) {
        this.tx = tx;
    }
}
