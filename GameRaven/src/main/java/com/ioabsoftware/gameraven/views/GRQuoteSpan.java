package com.ioabsoftware.gameraven.views;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.style.QuoteSpan;

import com.ioabsoftware.gameraven.util.Theming;

public class GRQuoteSpan extends QuoteSpan {

    private static final int WIDTH = 4;
    private static final int GAP = 4;
    private final int COLOR = Theming.accentColor();

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(COLOR);
    }

    public int getColor() {
        return COLOR;
    }

    public int getLeadingMargin(boolean first) {
        return WIDTH + GAP;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(COLOR);

        c.drawRect(x, top, x + dir * WIDTH, bottom, p);

        p.setStyle(style);
        p.setColor(color);
    }

}