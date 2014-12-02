package com.ioabsoftware.gameraven.views;

import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;

import com.ioabsoftware.gameraven.util.Theming;

import org.jetbrains.annotations.NotNull;

public class SpoilerBackgroundSpan extends BackgroundColorSpan {

    TextPaint textPaint;
    int backColor, textColor, linkColor;
    int revealedColor, hiddenColor;

    public SpoilerBackgroundSpan(int hidden, int revealed) {
        super(hidden);
        hiddenColor = hidden;
        revealedColor = revealed;

        backColor = hiddenColor;
        textColor = hiddenColor;
        linkColor = hiddenColor;
    }

    public void reveal() {
        backColor = revealedColor;
        linkColor = Theming.colorPrimary();
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