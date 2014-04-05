package com.ioabsoftware.gameraven.db;

import com.ioabsoftware.gameraven.networking.HandlesNetworkResult;


public class History {

    private String path;

    public String getPath() {
        return path;
    }


    private HandlesNetworkResult.NetDesc desc;

    public HandlesNetworkResult.NetDesc getDesc() {
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

    public History(String pathIn, HandlesNetworkResult.NetDesc descIn, byte[] resBodyAsBytesIn, int vertPosIn[]) {
        path = pathIn;
        desc = descIn;
        resBodyAsBytes = resBodyAsBytesIn;
        vertPos = vertPosIn;
    }
}
