package com.ioabsoftware.gameraven.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

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
        init(c);

        switch (myType) {
            case GAME_SEARCH:
            case BOARD:
            case TOPIC:
            case AMP_TOPIC:
            case TRACKED_TOPIC:
            case MESSAGE:
            case PM:
            case PM_DETAIL:
                setBackgroundDrawable(new SelectorDrawable(getContext()));
                break;
            case USER_DETAIL:
            case HIGHLIGHTED_USER:
            case HEADER:
            case ADMOB_AD:
            case GFAQS_AD:
                break;
        }

        preRetheme();
    }

    private void preRetheme() {
        myColor = Theming.accentColor();
        myScale = Theming.textScale();
        retheme();
    }

    public void beginShowingView(BaseRowData data) {
        if (Theming.accentColor() != myColor || Theming.textScale() != myScale) {
            SelectorDrawable.rebuildColorFilter();
            preRetheme();
        }
        showView(data);
    }

    protected abstract void init(Context context);

    protected abstract void retheme();

    protected abstract void showView(BaseRowData data);

}
