package com.ioabsoftware.gameraven.networking;

import android.os.AsyncTask;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;

public class NetworkTask extends AsyncTask<Void, Void, Response> {

    private Method method;
    private String path;
    private HandlesNetworkResult.NetDesc desc;
    private Map<String, String> cookies, data;
    private HandlesNetworkResult handler;

    public NetworkTask(HandlesNetworkResult handlerIn,
                       HandlesNetworkResult.NetDesc descIn,
                       Method methodIn,
                       Map<String, String> cookiesIn,
                       String pathIn,
                       Map<String, String> dataIn) {
        handler = handlerIn;
        desc = descIn;
        method = methodIn;
        path = pathIn;
        cookies = cookiesIn;
        data = dataIn;
    }

    @Override
    protected void onPreExecute() {
        handler.preExecuteSetup(desc);
    }

    @Override
    protected Response doInBackground(Void... params) {
        Response r;

        if (method.equals(Method.POST))
            r = post(path, data);
        else
            r = get(path, data);
        return r;
    }

    @Override
    protected void onPostExecute(Response res) {
        handler.handleNetworkResult(res, desc);
        handler.postExecuteCleanup(desc);
    }

    /**
     * Pass a GET request to the specified page, with the specified data.
     *
     * @param path The path to send the request to.
     * @param data The extra data to send, or null if none.
     * @return A Document object for the page.
     * @throws java.io.IOException
     */
    public Response get(String path, Map<String, String> data) {
        try {
            Response r;
            if (data != null) {
                r = Jsoup
                        .connect(path)
                        .method(Method.GET)
                        .cookies(cookies)
                        .data(data)
                        .timeout(10000)
                        .ignoreHttpErrors(true)
                        .execute();
            } else {
                r = Jsoup
                        .connect(path)
                        .method(Method.GET)
                        .cookies(cookies)
                        .timeout(10000)
                        .ignoreHttpErrors(true)
                        .execute();
            }

            return r;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Pass a POST request to the specified page, with the specified data.
     *
     * @param path The path to send the request to.
     * @param data The extra data to send.
     * @return A Document object for the page.
     * @throws java.io.IOException
     */
    public Response post(String path, Map<String, String> data) {
        try {
            Response r = Jsoup
                    .connect(path)
                    .method(Method.POST)
                    .cookies(cookies)
                    .data(data)
                    .timeout(10000)
                    .ignoreHttpErrors(true)
                    .execute();

            return r;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
