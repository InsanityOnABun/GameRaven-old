package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
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
import com.ioabsoftware.gameraven.views.SelectorSolidDrawable;
import com.ioabsoftware.gameraven.views.rowdata.MessageRowData;
import com.koushikdutta.ion.Ion;

public class MessageRowView extends BaseRowView implements View.OnClickListener {

    View topWrapper;

    ImageView avatar;

    TextView user;
    TextView post;

    LinearLayout pollWrapper;

    ClickableLinksTextView message;

    private static float userTextSize = 0;
    private static float postTextSize, messageTextSize;

    MessageRowData myData;

    SelectorSolidDrawable headerSelector;

    boolean isHighlighted = false;
    boolean isShowingPoll = false;
    boolean isUsingAvatars = false;

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
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.msgview, this, true);

        topWrapper = findViewById(R.id.mvTopWrapper);

        avatar = (ImageView) findViewById(R.id.mvAvatar);

        user = (TextView) findViewById(R.id.mvUser);
        post = (TextView) findViewById(R.id.mvPostNumber);

        pollWrapper = (LinearLayout) findViewById(R.id.mvPollWrapper);

        message = (ClickableLinksTextView) findViewById(R.id.mvMessage);

        if (userTextSize == 0) {
            userTextSize = user.getTextSize();
            postTextSize = post.getTextSize();
            messageTextSize = message.getTextSize();
        }

        headerSelector = new SelectorSolidDrawable(new Drawable[]{getResources().getDrawable(R.drawable.msghead)});
        topWrapper.setBackgroundDrawable(headerSelector);
        topWrapper.setOnClickListener(this);

        if (Theming.isAccentLight())
            ((ImageView) findViewById(R.id.mvMessageMenuIcon)).setImageResource(R.drawable.ic_info_light);
    }

    @Override
    protected void retheme() {
        user.setTextSize(PX, userTextSize * myScale);
        post.setTextSize(PX, postTextSize * myScale);
        message.setTextSize(PX, messageTextSize * myScale);

        message.setLinkTextColor(myColor);
        headerSelector.setMyColor(myColor);

        post.setTextColor(Theming.accentTextColor());
        user.setTextColor(Theming.accentTextColor());
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (MessageRowData) data;

        topWrapper.setClickable(myData.topClickable());

        user.setText((myData.hasTitles() ? myData.getUser() + myData.getUserTitles() : myData.getUser()));
        post.setText((myData.hasMsgID() ? "#" + myData.getPostNum() + ", " + myData.getPostTime() : myData.getPostTime()));

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
                headerSelector.setMyColor(Theming.accentColor());
            }
        } else {
            isHighlighted = true;
            headerSelector.setMyColor(myData.getHLColor());
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
                    .load("http://weblab.cs.uml.edu/~rdupuis/gamefaqs-avatars/avatars/" + myData.getUser().replace(" ", "%20") + ".png");

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
        if (start != end)
            return message.getText().subSequence(start, end).toString();
        else
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

    public boolean isEditable() {
        return myData.isEditable();
    }

    public String getMessageForQuoting() {
        return myData.getMessageForQuoting();
    }

    public String getMessageForEditing() {
        return myData.getMessageForEditing();
    }

    public boolean hasSpoilers() {
        return myData.hasSpoilers();
    }

    public void revealSpoilers() {
        myData.revealSpoilers((Spannable) message.getText());
    }

    public boolean spoilersAreRevealed() {
        return myData.spoilersAreRevealed();
    }

    private static boolean globalIsUsingAvatars;

    public static void setUsingAvatars(boolean set) {
        globalIsUsingAvatars = set;
    }

}
