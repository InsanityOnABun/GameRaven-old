package com.ioabsoftware.gameraven.views;

import android.text.style.URLSpan;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.RichTextUtils;

public class UrlSpanConverter implements RichTextUtils.SpanConverter<URLSpan, MessageLinkSpan> {
	@Override
	public MessageLinkSpan convert(URLSpan span) {
		return(new MessageLinkSpan(span.getURL(), AllInOneV2.get()));
	}
}