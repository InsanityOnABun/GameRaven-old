package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.method.ArrowKeyMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.util.Theming;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.ClickableLinksTextView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.MessageRowData;
import com.koushikdutta.ion.Ion;

public class MessageRowView extends BaseRowView implements View.OnClickListener {

    View topWrapper;

    ImageView avatar;
    TextView overflowIcon;

    TextView user;
    TextView post;

    LinearLayout pollWrapper;

    ClickableLinksTextView message;

    private static float userTextSize = 0;
    private static float postTextSize, messageTextSize;

    MessageRowData myData;

    MessageHeaderDrawable headerSelector;

    boolean isHighlighted = false;
    boolean isShowingPoll = false;
    boolean isUsingAvatars = false;
    boolean isDeleted = false;

    public MessageRowView(Context context) {
        super(context);
    }

    public MessageRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.MESSAGE;
        LayoutInflater.from(context).inflate(R.layout.msgview, this, true);

        topWrapper = findViewById(R.id.mvTopWrapper);

        avatar = (ImageView) findViewById(R.id.mvAvatar);
        overflowIcon = (TextView) findViewById(R.id.mvMessageMenuIcon);

        user = (TextView) findViewById(R.id.mvUser);
        post = (TextView) findViewById(R.id.mvPostNumber);

        pollWrapper = (LinearLayout) findViewById(R.id.mvPollWrapper);

        message = (ClickableLinksTextView) findViewById(R.id.mvMessage);

        if (userTextSize == 0) {
            userTextSize = user.getTextSize();
            postTextSize = post.getTextSize();
            messageTextSize = message.getTextSize();
        }

        ShapeDrawable d = new ShapeDrawable();
        d.getPaint().setColor(Theming.colorPrimary());

        headerSelector = new MessageHeaderDrawable(new Drawable[]{d});
        topWrapper.setBackgroundDrawable(headerSelector);
        topWrapper.setOnClickListener(this);
    }

    @Override
    protected void retheme() {
        user.setTextSize(PX, userTextSize * myScale);
        post.setTextSize(PX, postTextSize * myScale);
        message.setTextSize(PX, messageTextSize * myScale);

        message.setLinkTextColor(Theming.colorAccent());
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (MessageRowData) data;

        if (isDeleted != myData.isDeleted()) {
            isDeleted = myData.isDeleted();
            if (isDeleted) {
                findViewById(R.id.mvDeletedMessageWrapper).setVisibility(View.VISIBLE);
                findViewById(R.id.mvDMSep).setBackgroundColor(Theming.colorPrimary());
                topWrapper.setVisibility(View.GONE);
                pollWrapper.setVisibility(View.GONE);
                message.setVisibility(View.GONE);
            } else {
                findViewById(R.id.mvDeletedMessageWrapper).setVisibility(View.GONE);
                topWrapper.setVisibility(View.VISIBLE);
                pollWrapper.setVisibility(View.VISIBLE);
                message.setVisibility(View.VISIBLE);
            }
        }

        if (isDeleted) {
            ((TextView)findViewById(R.id.mvDMNum)).setText(myData.getPostNum());
            return;
        }

        topWrapper.setClickable(myData.topClickable());
        if (myData.topClickable())
            overflowIcon.setVisibility(View.VISIBLE);
        else
            overflowIcon.setVisibility(View.INVISIBLE);

        user.setText(myData.getUser() + myData.getUserTitles());
        post.setText(myData.getPostNum() + ", Posted " + myData.getPostTime());

        if (myData.hasPoll()) {
            isShowingPoll = true;
            pollWrapper.removeAllViews();
            pollWrapper.addView(myData.getPoll());
            pollWrapper.setVisibility(View.VISIBLE);
        } else if (isShowingPoll) {
            isShowingPoll = false;
            pollWrapper.setVisibility(View.GONE);
            pollWrapper.removeAllViews();
        }

        if (myData.getHLColor() == 0) {
            if (isHighlighted) {
                isHighlighted = false;
                headerSelector.clearHighlightColor();
            }
        } else {
            isHighlighted = true;
            headerSelector.setHighlightColor(myData.getHLColor());
        }

        if (isUsingAvatars != globalIsUsingAvatars) {
            isUsingAvatars = globalIsUsingAvatars;
            if (isUsingAvatars) {
                avatar.setVisibility(View.VISIBLE);
            } else {
                avatar.setVisibility(View.GONE);
            }
        }

        if (isUsingAvatars)
            Ion.with(avatar)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_default)
                    .load(myData.getAvatarUrl());

        message.setText(myData.getSpannedMessage());

        message.setMovementMethod(ArrowKeyMovementMethod.getInstance());
        message.setTextIsSelectable(true);
        // the autoLink attribute must be removed, if you hasn't set it then ok, otherwise call textView.setAutoLink(0);

        headerSelector.invalidateDrawable(headerSelector);
    }

    /**
     * @return selected text, or null if no text is selected
     */
    public String getSelection() {
        int start = message.getSelectionStart();
        int end = message.getSelectionEnd();
        if (start != end) {
            if (start > end) {
                int temp = end;
                end = start;
                start = temp;
            }
            return message.getText().subSequence(start, end).toString();
        } else
            return null;
    }

    @Override
    public void onClick(View v) {
        AllInOneV2.get().messageMenuClicked(this);
    }

    public String getMessageDetailLink() {
        return myData.getMessageDetailLink();
    }

    public String getUserDetailLink() {
        return myData.getUserDetailLink();
    }

    public boolean isEdited() {
        return myData.isEdited();
    }

    public String getMessageID() {
        return myData.getMessageID();
    }

    public String getUser() {
        return myData.getUser();
    }

    public String getPostNum() {
        return myData.getPostNum();
    }

    public String getMessageForQuoting() {
        return myData.getMessageForQuoting();
    }

    public String getMessageForEditing() {
        return myData.getMessageForEditing();
    }

    private static boolean globalIsUsingAvatars;

    public static void setUsingAvatars(boolean set) {
        globalIsUsingAvatars = set;
    }

    public boolean canReport() {
        return myData.canReport();
    }

    public boolean canDelete() {
        return myData.canDelete();
    }

    public boolean canEdit() {
        return myData.canEdit();
    }

    public boolean canQuote() {
        return myData.canQuote();
    }







    public class MessageHeaderDrawable extends LayerDrawable {

        private int myColor, myClickedColor;

        public MessageHeaderDrawable(Drawable[] layers) {
            super(layers);
            clearHighlightColor();
        }

        public void setHighlightColor(int myColorIn) {
            myColor = myColorIn;

            float[] hsv = new float[3];
            Color.colorToHSV(myColor, hsv);
            hsv[2] *= 0.8f;
            myClickedColor = Color.HSVToColor(hsv);

            onStateChange(getState());
        }

        public void clearHighlightColor() {
            myColor = Theming.colorPrimary();
            myClickedColor = Theming.colorPrimaryDark();

            onStateChange(getState());
        }

        @Override
        protected boolean onStateChange(int[] states) {
            boolean isClicked = false;
            for (int state : states) {
                if (state == android.R.attr.state_focused || state == android.R.attr.state_pressed) {
                    isClicked = true;
                }
            }

            if (isClicked) {
                super.setColorFilter(myClickedColor, PorterDuff.Mode.SRC);
            } else {
                super.setColorFilter(myColor, PorterDuff.Mode.SRC);
            }

            return super.onStateChange(states);
        }

        @Override
        public boolean isStateful() {
            return true;
        }

    }

}
