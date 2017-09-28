package com.blue.bean;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Administrator on 2017/8/28.
 */

public class MessageBean implements Serializable{
    private int type;
    private String msg;
    private byte[] stream;
    private String id,name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public byte[] getStream() {
        return stream;
    }

    public void setStream(byte[] stream) {
        this.stream = stream;
    }

    @Override
    public String toString() {
        return "MessageBean{" +
                "type=" + type +
                ", msg='" + msg + '\'' +
                ", stream=" + Arrays.toString(stream) +
                '}';
    }
}
