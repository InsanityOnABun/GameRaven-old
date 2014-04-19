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

public class DocumentParser implements AsyncParser<FinalDoc> {
    @Override
    public Future<FinalDoc> parse(DataEmitter emitter) {
        return new ByteBufferListParser().parse(emitter)
                .then(new TransformFuture<FinalDoc, ByteBufferList>() {
                    @Override
                    protected void transform(ByteBufferList result) throws Exception {
                        byte[] bytes = result.getAllByteArray();
                        setComplete(new FinalDoc(bytes, Jsoup.parse(new String(bytes))));
                    }
                });
    }

    @Override
    public void write(DataSink sink, FinalDoc value, CompletedCallback completed) {
        new ByteBufferListParser().write(sink, new ByteBufferList(value.bytes), completed);
    }
}
