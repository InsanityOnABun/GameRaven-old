package com.ioabsoftware.gameraven.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.util.Theming;

public abstract class BaseRowView extends LinearLayout {

    protected RowType myType = null;

    protected int myColor = 0;
    protected float myScale = 0;

    protected final int PX = TypedValue.COMPLEX_UNIT_PX;

    public BaseRowView(Context context) {
        super(context);
        preInit(context);
    }

    public BaseRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        preInit(context);
    }

    public BaseRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        preInit(context);
    }

    private void preInit(Context c) {
        myColor = Theming.accentColor();
        myScale = Theming.textScale();
        init(c);
    }

    protected Drawable getSelector() {
        StateDrawable s = new StateDrawable(new Drawable[]{getResources().getDrawable(R.drawable.selector)});
        s.setMyColor(Color.TRANSPARENT);
        return s;
    }

    public void beginShowingView(BaseRowData data) {
        if (Theming.accentColor() != myColor || Theming.textScale() != myScale) {
            myColor = Theming.accentColor();
            myScale = Theming.textScale();
            retheme(myColor, myScale);
        }
        showView(data);
    }

    protected abstract void init(Context context);

    protected abstract void retheme(int color, float scale);

    protected abstract void showView(BaseRowData data);

}
