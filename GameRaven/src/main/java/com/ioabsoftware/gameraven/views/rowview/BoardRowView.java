package com.ioabsoftware.gameraven.views.rowview;

import android.app.AlertDialog;
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
import com.ioabsoftware.gameraven.views.rowdata.BoardRowData;
import com.ioabsoftware.gameraven.views.rowdata.BoardRowData.BoardType;

public class BoardRowView extends BaseRowView {

    private TextView desc, lastPost, tpcMsgDetails, name;

    private static float nameTextSize = 0;
    private static float descTextSize, lpTextSize, detailsTextSize;

    BoardRowData myData;

    public BoardRowView(Context context) {
        super(context);
    }

    public BoardRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoardRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.BOARD;
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.boardview, this, true);

        desc = (TextView) findViewById(R.id.bvDesc);
        lastPost = (TextView) findViewById(R.id.bvLastPost);
        tpcMsgDetails = (TextView) findViewById(R.id.bvTpcMsgDetails);
        name = (TextView) findViewById(R.id.bvName);

        if (nameTextSize == 0) {
            nameTextSize = name.getTextSize();
            detailsTextSize = tpcMsgDetails.getTextSize();
            lpTextSize = lastPost.getTextSize();
            descTextSize = desc.getTextSize();
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myData.getUrl() == null) {
                    AlertDialog.Builder b = new AlertDialog.Builder(BoardRowView.this.getContext());
                    b.setTitle("Cannot Access " + myData.getName());
                    b.setMessage(myData.getName() +
                            " cannot be accessed, most likely due to user level requirements.");
                    b.setPositiveButton("Ok", null);
                    b.create().show();
                } else {
                    if (myData.getBoardType() == BoardType.LIST) {
                        AllInOneV2.get().getSession().get(NetDesc.BOARD_LIST, myData.getUrl(), null);
                    } else {
                        AllInOneV2.get().getSession().get(NetDesc.BOARD, myData.getUrl(), null);
                    }
                }
            }
        });
    }

    @Override
    protected void retheme() {
        desc.setTextSize(PX, descTextSize * myScale);
        lastPost.setTextSize(PX, lpTextSize * myScale);
        tpcMsgDetails.setTextSize(PX, detailsTextSize * myScale);
        name.setTextSize(PX, nameTextSize * myScale);
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (BoardRowData) data;

        name.setText(myData.getName());

        String descText = myData.getDesc();
        if (descText != null) {
            desc.setVisibility(View.VISIBLE);
            desc.setText(descText);
        } else
            desc.setVisibility(View.INVISIBLE);

        switch (myData.getBoardType()) {
            case NORMAL:
                tpcMsgDetails.setVisibility(View.VISIBLE);
                lastPost.setText("Last Post: " + myData.getLastPost());
                tpcMsgDetails.setText("Tpcs: " + myData.getTCount() + "; Msgs: " + myData.getMCount());
                break;
            case SPLIT:
                lastPost.setText("--Split List--");
                tpcMsgDetails.setVisibility(View.INVISIBLE);
                break;
            case LIST:
                lastPost.setText("--Board List--");
                tpcMsgDetails.setVisibility(View.INVISIBLE);
                break;
        }
    }

}
