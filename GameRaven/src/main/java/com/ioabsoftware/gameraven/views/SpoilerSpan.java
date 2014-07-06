package com.ioabsoftware.gameraven.views;

import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;

import org.jetbrains.annotations.NotNull;

public class SpoilerSpan extends BackgroundColorSpan {

    int color;

    public SpoilerSpan(int color) {
        super(color);
        this.color = color;
    }

    @Override
    public void updateDrawState(@NotNull TextPaint ds) {
        ds.setColor(color);
        ds.linkColor = color;
        ds.bgColor = color;
    }
}