package com.ioabsoftware.gameraven.db;

import com.ioabsoftware.gameraven.networking.NetDesc;


public class History {

    private String path;

    public String getPath() {
        return path;
    }


    private NetDesc desc;

    public NetDesc getDesc() {
        return desc;
    }


    private byte[] resBodyAsBytes;

    public byte[] getResBodyAsBytes() {
        return resBodyAsBytes;
    }


    private int vertPos[];

    public int[] getVertPos() {
        return vertPos;
    }

    public History(String pathIn, NetDesc descIn, byte[] resBodyAsBytesIn, int vertPosIn[]) {
        path = pathIn;
        desc = descIn;
        resBodyAsBytes = resBodyAsBytesIn;
        vertPos = vertPosIn;
    }
}
