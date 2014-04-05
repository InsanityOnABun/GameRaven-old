package com.ioabsoftware.gameraven.db;

import java.util.Locale;

public class HighlightedUser {

    private String name;

    public String getName() {
        return name;
    }

    public String getNameToLower() {
        return name.toLowerCase(Locale.US);
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    private String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String labelIn) {
        label = labelIn;
    }

    private int id;

    public int getID() {
        return id;
    }

    public void setID(int idIn) {
        id = idIn;
    }

    private int color;

    public int getColor() {
        return color;
    }

    public void setColor(int colorIn) {
        color = colorIn;
    }

    public HighlightedUser(String nameIn, String labelIn, int colorIn) {
        name = nameIn;
        label = labelIn;
        color = colorIn;
    }

    public HighlightedUser(int idIn, String nameIn, String labelIn, int colorIn) {
        id = idIn;
        name = nameIn;
        label = labelIn;
        color = colorIn;
    }

}
