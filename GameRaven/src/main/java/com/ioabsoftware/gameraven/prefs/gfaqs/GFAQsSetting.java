package com.ioabsoftware.gameraven.prefs.gfaqs;

import android.util.SparseArray;

public class GFAQsSetting {
    private String nameOfSetting;
    public String getNameOfSetting() {return nameOfSetting;}

    private String label;
    public String getLabel() {return label;}

    private String hint;
    public String getHint() {return hint;}

    private SparseArray<String> values;
    public SparseArray<String> getValues() {return values;}

    private int currentValue;
    public int getCurrentValue() {return currentValue;}

    public String getCurrentValueLabel(int key) {return values.get(key);}

    public GFAQsSetting(String[] attrs, SparseArray<String> values, int currentValue) {
        nameOfSetting = attrs[0];
        label = attrs[1];
        hint = attrs[2];
        this.values = values;
        this.currentValue = currentValue;
    }

}
