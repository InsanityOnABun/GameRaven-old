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

    public GFAQsSetting(String nameOfSetting, String label, String hint, SparseArray<String> values, int currentValue) {
        this.nameOfSetting = nameOfSetting;
        this.label = label;
        this.hint = hint;
        this.values = values;
        this.currentValue = currentValue;
    }

}
