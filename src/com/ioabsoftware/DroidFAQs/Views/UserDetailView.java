package com.ioabsoftware.DroidFAQs.Views;

import com.ioabsoftware.DroidFAQs.AllInOneV2;
import com.ioabsoftware.gameraven.R;

import android.content.Context;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UserDetailView extends LinearLayout {

	public UserDetailView(final AllInOneV2 aio, final String name, String ID, String level, String creation, 
						  String lVisit, String sig, String karma, String AMP) {
		super(aio);
		
		LayoutInflater inflater = (LayoutInflater) aio.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.userdetailview, this);
        
        ((TextView) findViewById(R.id.udID)).setText(ID);
        ((TextView) findViewById(R.id.udLevel)).setText(Html.fromHtml(level));
        ((TextView) findViewById(R.id.udCreation)).setText(creation);
        ((TextView) findViewById(R.id.udLVisit)).setText(lVisit);
        ((TextView) findViewById(R.id.udKarma)).setText(karma);
        ((TextView) findViewById(R.id.udAMP)).setText(AMP);
        
        if (sig != null) {
        	((TextView) findViewById(R.id.udSig)).setText(Html.fromHtml(sig));
        	Linkify.addLinks(((TextView) findViewById(R.id.udSig)), Linkify.WEB_URLS);
        }
        else
        	((RelativeLayout) findViewById(R.id.udSigWrapper)).setVisibility(View.GONE);
        
        ((Button) findViewById(R.id.udSendPM)).setText("Send PM to " + name);
        ((Button) findViewById(R.id.udSendPM)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				aio.pmSetup(name, null, null);
			}
		});
	}
}
