package com.ioabsoftware.gameraven.util;

import org.jsoup.nodes.Document;

public class FinalDoc {

    public byte[] bytes;
    public Document doc;

    public FinalDoc(byte[] bytes, Document doc) {
        this.bytes = bytes;
        this.doc = doc;
    }
}
