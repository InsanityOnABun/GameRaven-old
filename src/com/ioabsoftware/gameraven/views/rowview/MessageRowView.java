package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.method.ArrowKeyMovementMethod;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.ClickableLinksTextView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.MessageRowData;

public class MessageRowView extends BaseRowView implements View.OnClickListener {
	
	View topWrapper;
	
	TextView user;
	TextView post;
	
	LinearLayout pollWrapper;
	
	ClickableLinksTextView message;
	
	MessageRowData myData;
	
	boolean isHighlighted = false;
	boolean isShowingPoll = false;

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
	protected void init(Context context) {
		myType = RowType.MESSAGE;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.msgview, this, true);
        
        topWrapper = findViewById(R.id.mvTopWrapper);
        
        user = (TextView) findViewById(R.id.mvUser);
    	post = (TextView) findViewById(R.id.mvPostNumber);
    	
    	pollWrapper = (LinearLayout) findViewById(R.id.mvPollWrapper);
    	
    	message = (ClickableLinksTextView) findViewById(R.id.mvMessage);
    	
    	int px = TypedValue.COMPLEX_UNIT_PX;
    	user.setTextSize(px, user.getTextSize() * AllInOneV2.getTextScale());
		post.setTextSize(px, post.getTextSize() * AllInOneV2.getTextScale());
		message.setTextSize(px, message.getTextSize() * AllInOneV2.getTextScale());
		
		topWrapper.setBackgroundDrawable(AllInOneV2.getMsgHeadSelector());
		topWrapper.setOnClickListener(this);
        
        message.setLinkTextColor(AllInOneV2.getAccentColor());
		
		post.setTextColor(AllInOneV2.getAccentTextColor());
		user.setTextColor(AllInOneV2.getAccentTextColor());
		
		if (AllInOneV2.isAccentLight())
        	((ImageView) findViewById(R.id.mvMessageMenuIcon)).setImageResource(R.drawable.ic_info_light);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		myData = (MessageRowData) data;
		
		user.setText((myData.hasTitles() ? myData.getUser() + myData.getUserTitles() : myData.getUser()));
		post.setText((myData.hasMsgID() ? "#" + myData.getPostNum() + ", " + myData.getPostTime() : myData.getPostTime()));
		
		if (myData.hasPoll()) {
			isShowingPoll = true;
			pollWrapper.setVisibility(View.VISIBLE);
			pollWrapper.addView(myData.getPoll());
		}
		else if (isShowingPoll) {
			isShowingPoll = false;
			pollWrapper.setVisibility(View.GONE);
		}
		
		if (myData.getHLColor() == 0) {
			if (isHighlighted) {
				isHighlighted = false;
				topWrapper.setBackgroundDrawable(AllInOneV2.getMsgHeadSelector());
			}
		}
        else {
        	isHighlighted = true;
        	float[] hsv = new float[3];
    		Color.colorToHSV(myData.getHLColor(), hsv);
        	if (AllInOneV2.getSettingsPref().getBoolean("useWhiteAccentText", false)) {
    			// color is probably dark
    			if (hsv[2] > 0)
    				hsv[2] *= 1.2f;
    			else
    				hsv[2] = 0.2f;
    		}
    		else {
    			// color is probably bright
    			hsv[2] *= 0.8f;
    		}
    		
    		int msgSelectorColor = Color.HSVToColor(hsv);
    		
    		StateListDrawable hlSelector = new StateListDrawable();
    		hlSelector.addState(new int[] {android.R.attr.state_focused}, new ColorDrawable(msgSelectorColor));
    		hlSelector.addState(new int[] {android.R.attr.state_pressed}, new ColorDrawable(msgSelectorColor));
    		hlSelector.addState(StateSet.WILD_CARD, new ColorDrawable(myData.getHLColor()));
    		
    		topWrapper.setBackgroundDrawable(hlSelector);
        }
		
		message.setText(myData.getSpannedMessage());
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	message.setMovementMethod(ArrowKeyMovementMethod.getInstance());
        	message.setTextIsSelectable(true);
            // the autoLink attribute must be removed, if you hasn't set it then ok, otherwise call textView.setAutoLink(0);
        }
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

	@Override
	public void onClick(View v) {
		AllInOneV2.get().messageMenuClicked(this);
	}

}
