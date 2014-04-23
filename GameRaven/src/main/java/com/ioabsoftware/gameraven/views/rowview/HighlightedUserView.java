package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.SettingsHighlightedUsers;
import com.ioabsoftware.gameraven.db.HighlightListDBHelper;
import com.ioabsoftware.gameraven.db.HighlightedUser;
import com.ioabsoftware.gameraven.util.Theming;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.StateDrawable;

public class HighlightedUserView extends BaseRowView implements OnClickListener {

    private SettingsHighlightedUsers hlActivity;

    private HighlightedUser user;

    private LinearLayout colorFrame;
    private TextView nameView, labelView;

    private StateDrawable back;

    public HighlightedUserView(Context context) {
        super(context);
    }

    public HighlightedUserView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HighlightedUserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public HighlightedUserView(SettingsHighlightedUsers hlActivityIn, HighlightedUser userIn) {
        super(hlActivityIn);
        hlActivity = hlActivityIn;

        LayoutInflater inflater = (LayoutInflater) hlActivityIn.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.highlighteduserview, this);

        user = userIn;

        nameView = (TextView) findViewById(R.id.hvName);
        labelView = (TextView) findViewById(R.id.hvLabel);
        colorFrame = (LinearLayout) findViewById(R.id.hvColorFrame);
        findViewById(R.id.hvSep).setBackgroundColor(Theming.accentColor());

        nameView.setText(user.getName());
        labelView.setText(user.getLabel());

        if (user.getColor() != 0)
            colorFrame.setBackgroundColor(user.getColor());

        back = new StateDrawable(new Drawable[] {getResources().getDrawable(R.drawable.selector)});
        back.setMyColor(Color.TRANSPARENT);
        setBackgroundDrawable(back);

        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        HighlightListDBHelper.showHighlightUserDialog(hlActivity, user, null, hlActivity);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.HIGHLIGHTED_USER;
    }

    @Override
    protected void retheme() {
        // nada
    }

    @Override
    public void showView(BaseRowData data) {
        // nada
    }


}
