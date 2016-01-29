package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.networking.NetDesc;
import com.ioabsoftware.gameraven.views.RowType;

public class AMPRowView extends TopicRowView {

    public AMPRowView(Context context) {
        super(context);
    }

    public AMPRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AMPRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        myType = RowType.AMP_TOPIC;

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String url = myData.getUrl().substring(0, myData.getUrl().lastIndexOf('/'));
                AllInOneV2.get().getSession().get(NetDesc.BOARD, url);
                return true;
            }
        });
    }
}
