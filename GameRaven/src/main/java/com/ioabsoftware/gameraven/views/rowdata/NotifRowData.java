package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class NotifRowData extends BaseRowData {

    private String title, time, url;

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }

    private boolean isOld;

    public boolean isOld() {
        return isOld;
    }

    public NotifRowData(String titleIn, String timeIn, String urlIn, boolean isOldIn) {
        title = titleIn;
        time = timeIn;
        url = urlIn;
        isOld = isOldIn;
    }

    @Override
    public RowType getRowType() {
        return RowType.NOTIF;
    }

    @Override
    public String toString() {
        return "title: " + title +
                "\ntime: " + time +
                "\nurl: " + url +
                "\nisOld: " + String.valueOf(isOld);
    }

}
