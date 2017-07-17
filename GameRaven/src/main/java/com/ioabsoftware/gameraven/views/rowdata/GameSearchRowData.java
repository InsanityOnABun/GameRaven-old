package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class GameSearchRowData extends BaseRowData {

    private String name, platform, year, url;

    public String getName() {
        return name;
    }

    public String getPlatform() {
        return platform;
    }

    public String getYear() {
        return year;
    }

    public String getUrl() {
        return url;
    }

    public GameSearchRowData(String nameIn, String platformIn, String yearIn, String urlIn) {
        name = nameIn;
        platform = platformIn;
        year = yearIn;
        url = urlIn;
    }

    @Override
    public RowType getRowType() {
        return RowType.GAME_SEARCH;
    }

    @Override
    public String toString() {
        return "name: " + name +
                "\nplatform: " + platform +
                "\nyear: " + year +
                "\nurl: " + url;
    }
}
