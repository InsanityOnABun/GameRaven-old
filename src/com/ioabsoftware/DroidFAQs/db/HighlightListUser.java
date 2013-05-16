package com.ioabsoftware.DroidFAQs.db;

import java.util.Locale;

public class HighlightListUser {

	private String name, label;
	private int color;
	
	public HighlightListUser(String nameIn, String labelIn, int colorIn) {
		name = nameIn.toLowerCase(Locale.US);
		label = labelIn;
		color = colorIn;
	}
}
