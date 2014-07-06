package com.ioabsoftware.gameraven.views;

import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;

import org.jetbrains.annotations.NotNull;

public class SpoilerSpan extends BackgroundColorSpan {

    TextPaint textPaint;
    int backColor, textColor, linkColor;

    public SpoilerSpan(int bColor, int tColor, int lColor) {
        super(bColor);
        backColor = bColor;
        textColor = tColor;
        linkColor = lColor;
    }

    public void reveal(int bColor, int tColor, int lColor) {
        backColor = bColor;
        textColor = tColor;
        linkColor = lColor;
        updateDrawState(textPaint);
    }

    @Override
    public void updateDrawState(@NotNull TextPaint ds) {
        textPaint = ds;
        ds.bgColor = backColor;
        ds.setColor(textColor);
        ds.linkColor = linkColor;
    }
}