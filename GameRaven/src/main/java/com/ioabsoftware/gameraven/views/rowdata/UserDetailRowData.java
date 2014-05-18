package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class UserDetailRowData extends BaseRowData {

    private String name, ID, level, creation, lVisit, sig, karma, amp, tagKey, tagText, tagPath;

    public String getName() {
        return name;
    }

    public String getID() {
        return ID;
    }

    public String getLevel() {
        return level;
    }

    public String getCreation() {
        return creation;
    }

    public String getLastVisit() {
        return lVisit;
    }

    public String getSig() {
        return sig;
    }

    public String getKarma() {
        return karma;
    }

    public String getAMP() {
        return amp;
    }

    public String getTagKey() {
        return tagKey;
    }

    public String getTagText() {
        return tagText;
    }

    public String getTagPath() {
        return tagPath;
    }

    @Override
    public RowType getRowType() {
        return RowType.USER_DETAIL;
    }

    public UserDetailRowData(String nameIn, String IDIn, String levelIn, String creationIn,
                             String lVisitIn, String sigIn, String karmaIn, String ampIn,
                             String tagKeyIn, String tagTextIn, String tagPathIn) {
        name = nameIn;
        ID = IDIn;
        level = levelIn;
        creation = creationIn;
        lVisit = lVisitIn;
        sig = sigIn;
        karma = karmaIn;
        amp = ampIn;
        tagKey = tagKeyIn;
        tagText = tagTextIn;
        tagPath = tagPathIn;
    }

    @Override
    public String toString() {
        return "name: " + name +
                "\nID: " + ID +
                "\nlevel: " + level +
                "\ncreation: " + creation +
                "\nlVisit: " + lVisit +
                "\nsig: " + sig +
                "\nkarma: " + karma +
                "\namp: " + amp +
                "\ntagKey: " + tagKey +
                "\ntagText: " + tagText +
                "\ntagPath: " + tagPath;
    }

}
