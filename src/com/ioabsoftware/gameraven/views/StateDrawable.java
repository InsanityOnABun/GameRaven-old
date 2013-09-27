package com.ioabsoftware.gameraven.views;

import com.ioabsoftware.gameraven.AllInOneV2;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.Color;
import android.graphics.PorterDuff;

public class StateDrawable extends LayerDrawable {

	private int myColor, myClickedColor;
	
    public StateDrawable(Drawable[] layers) {
        super(layers);
    }

	public void setMyColor(int myColorIn) {
		myColor = myColorIn;
		
		if (myColor == Color.TRANSPARENT) {
			myClickedColor = AllInOneV2.getAccentColor();
		}
		else {
			float[] hsv = new float[3];
			Color.colorToHSV(myColor, hsv);
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
			
			myClickedColor = Color.HSVToColor(hsv);
		}
		
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
        } 
        else {
            super.setColorFilter(myColor, PorterDuff.Mode.SRC);
        }
        
        return super.onStateChange(states);
    }

    @Override
    public boolean isStateful() {
        return true;
    }

}
