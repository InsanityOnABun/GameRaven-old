package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class BoardRowData extends BaseRowData {

    public static enum BoardType {
        NORMAL, SPLIT, LIST
    }

    private String name, desc, lastPost, tCount, mCount, url;

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getLastPost() {
        return lastPost;
    }

    public String getTCount() {
        return tCount;
    }

    public String getMCount() {
        return mCount;
    }

    public String getUrl() {
        return url;
    }

    private BoardType boardType;

    public BoardType getBoardType() {
        return boardType;
    }

    public BoardRowData(String nameIn, String descIn, String lastPostIn,
                        String tCountIn, String mCountIn, String urlIn, BoardType type) {
        name = nameIn;
        desc = descIn;
        lastPost = lastPostIn;
        tCount = tCountIn;
        mCount = mCountIn;
        url = urlIn;
        boardType = type;
    }

    @Override
    public RowType getRowType() {
        return RowType.BOARD;
    }

}
