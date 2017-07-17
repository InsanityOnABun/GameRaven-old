package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.NetDesc;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.GameSearchRowData;

public class GameSearchRowView extends BaseRowView {

    TextView platform, year, name;

    private static float nameTextSize = 0;
    private static float platformTextSize, yearTextSize;

    GameSearchRowData myData;

    public GameSearchRowView(Context context) {
        super(context);
    }

    public GameSearchRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameSearchRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.GAME_SEARCH;
        LayoutInflater.from(context).inflate(R.layout.gamesearchview, this, true);

        platform = (TextView) findViewById(R.id.gsPlatform);
        year = (TextView) findViewById(R.id.gsYear);
        name = (TextView) findViewById(R.id.gsName);

        if (nameTextSize == 0) {
            nameTextSize = name.getTextSize();
            yearTextSize = year.getTextSize();
            platformTextSize = platform.getTextSize();
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().getSession().get(NetDesc.BOARD, myData.getUrl());
            }
        });
    }

    @Override
    protected void retheme() {
        name.setTextSize(PX, nameTextSize * myScale);
        year.setTextSize(PX, yearTextSize * myScale);
        platform.setTextSize(PX, platformTextSize * myScale);
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (GameSearchRowData) data;

        name.setText(myData.getName());
        year.setText(myData.getYear());
        platform.setText(myData.getPlatform());
    }

}
