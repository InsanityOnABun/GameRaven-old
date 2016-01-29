package com.ioabsoftware.gameraven.views;

import android.content.Intent;
import android.net.Uri;
import android.text.style.ClickableSpan;
import android.view.View;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.networking.NetDesc;
import com.ioabsoftware.gameraven.networking.Session;

public class MessageLinkSpan extends ClickableSpan {

    String url;
    AllInOneV2 aio;

    public MessageLinkSpan(String urlIn, AllInOneV2 aioIn) {
        url = Session.buildURL(urlIn, NetDesc.UNSPECIFIED);
        aio = aioIn;
    }

    @Override
    public void onClick(View arg0) {
        NetDesc desc = Session.determineNetDesc(url);
        if (desc != NetDesc.UNSPECIFIED)
            aio.getSession().get(desc, url);
        else
            aio.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

}
