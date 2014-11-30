package com.ioabsoftware.gameraven.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.StateListDrawable;

import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.util.Theming;

public class SelectorItemDrawable extends StateListDrawable {

    private static PorterDuffColorFilter pressedFilter;

    public SelectorItemDrawable(Context c) {
        super();

        if (pressedFilter == null)
            pressedFilter = new PorterDuffColorFilter(Theming.colorPrimary(), PorterDuff.Mode.SRC_ATOP);

        if (!Theming.usingLightTheme())
            addState(new int[]{}, c.getResources().getDrawable(R.drawable.item_background));
        else
            addState(new int[]{}, c.getResources().getDrawable(R.drawable.item_background_light));
    }

    @Override
    protected boolean onStateChange(int[] states) {
        boolean isClicked = false;
        for (int state : states) {
            if (state == android.R.attr.state_pressed) {
                isClicked = true;
            }
        }

        if (isClicked)
            setColorFilter(pressedFilter);
        else
            clearColorFilter();

        return super.onStateChange(states);
    }
}