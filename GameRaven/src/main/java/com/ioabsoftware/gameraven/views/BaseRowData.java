package com.ioabsoftware.gameraven.views;

public abstract class BaseRowData {
    public static enum ReadStatus {
        UNREAD, READ, NEW_POST
    }

    public abstract RowType getRowType();

    @Override
    public abstract String toString();
}