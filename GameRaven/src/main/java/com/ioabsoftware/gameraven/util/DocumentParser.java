package com.ioabsoftware.gameraven.util;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.TransformFuture;
import com.koushikdutta.async.parser.AsyncParser;
import com.koushikdutta.async.parser.ByteBufferListParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class DocumentParser implements AsyncParser<Document> {
    @Override
    public Future<Document> parse(DataEmitter emitter) {
        return new ByteBufferListParser().parse(emitter)
                .then(new TransformFuture<Document, ByteBufferList>() {
                    @Override
                    protected void transform(ByteBufferList result) throws Exception {
                        setComplete(Jsoup.parse(result.readString()));
                    }
                });
    }

    @Override
    public void write(DataSink sink, Document value, CompletedCallback completed) {
        new ByteBufferListParser().write(sink, new ByteBufferList(value.outerHtml().getBytes()), completed);
    }
}
