package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.Session;
import com.ioabsoftware.gameraven.views.ClickableLinksTextView;
import com.ioabsoftware.gameraven.views.rowdata.BaseRowData;
import com.ioabsoftware.gameraven.views.rowdata.MessageRowData;
import com.ioabsoftware.gameraven.views.rowdata.RowType;

public class MessageRowView extends BaseRowView {
	
	TextView user;
	TextView post;
	
	LinearLayout pollWrapper;
	
	ClickableLinksTextView message;
	
	MessageRowData myData;

	public MessageRowView(Context context) {
		super(context);
	}

	public MessageRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public MessageRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	void init(Context context) {
		myType = RowType.MESSAGE;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.msgview, this, true);
        
        user = (TextView) findViewById(R.id.mvUser);
    	post = (TextView) findViewById(R.id.mvPostNumber);
    	
    	pollWrapper = (LinearLayout) findViewById(R.id.mvPollWrapper);
    	
    	message = (ClickableLinksTextView) findViewById(R.id.mvMessage);
    	
    	int px = TypedValue.COMPLEX_UNIT_PX;
    	user.setTextSize(px, user.getTextSize() * AllInOneV2.getTextScale());
		post.setTextSize(px, post.getTextSize() * AllInOneV2.getTextScale());
		message.setTextSize(px, message.getTextSize() * AllInOneV2.getTextScale());
		
		findViewById(R.id.mvTopWrapper).setBackgroundDrawable(AllInOneV2.getMsgHeadSelector().getConstantState().newDrawable());
		
		if (AllInOneV2.isAccentLight())
        	((ImageView) findViewById(R.id.mvMessageMenuIcon)).setImageResource(R.drawable.ic_info_light);
	}

	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		myData = (MessageRowData) data;
		
		user.setText((myData.hasTitles() ? myData.getUser() + myData.getUserTitles() : myData.getUser()));
		post.setText((myData.hasMsgID() ? "#" + myData.getPostNum() + ", " + myData.getPostTime() : myData.getPostTime()));
		
		post.setTextColor(AllInOneV2.getAccentTextColor());
		user.setTextColor(AllInOneV2.getAccentTextColor());
		
		if (myData.hasPoll()) {
			pollWrapper.setVisibility(View.VISIBLE);
			pollWrapper.addView(myData.getPoll());
		}
		
		message.setText(myData.getSpannedMessage());
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
	
	public boolean isEditable() {
		return myData.isEditable();
	}
	
	public String getMessageForQuoting() {
		return myData.getMessageForQuoting();
	}
	
	public String getMessageForEditing() {
		return myData.getMessageForQuoting();
	}

}
