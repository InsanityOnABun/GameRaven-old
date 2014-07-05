package com.ioabsoftware.gameraven.views;

import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.view.View;

import org.jetbrains.annotations.NotNull;

public class SpoilerClickSpan extends ClickableSpan {

    int color, defTextColor;
    int start, end;

    public SpoilerClickSpan(int start, int end, int color, int defTextColor) {
        this.start = start;
        this.end = end;
        this.color = color;
        this.defTextColor = defTextColor;
    }

    @Override
    public void onClick(View widget) {
        ((Spannable) ((ClickableLinksTextView) widget).getText()).setSpan(new BackgroundColorSpan(color), start, end, 0);
    }

    @Override
    public void updateDrawState(@NotNull TextPaint ds) {
        ds.setColor(defTextColor);
        ds.setUnderlineText(false);
    }
}