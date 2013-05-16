package com.ioabsoftware.DroidFAQs.Networking;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;


import android.os.AsyncTask;

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
    					Map<String, String> dataIn)
    {
    	handler = handlerIn;
    	desc = descIn;
        method = methodIn;
        path = pathIn;
        cookies = cookiesIn;
        data = dataIn;
    }
    
    @Override
    protected void onPreExecute()
    {
    	handler.preExecuteSetup(desc);
    }
	
	@Override
	protected Response doInBackground(Void... params) {
		Response r;
    	
    	if (method.equals(Method.POST))
    		r = post(path, data);
    	else
    	{
    		if (data == null)
    			r = get(path);
    		else
    			r = get(path, data);
    	}
    	return r;
    }
	
	@Override
	protected void onPostExecute(Response res)
	{
		handler.handleNetworkResult(res, desc);
		handler.postExecuteCleanup(desc);
	}
    
    /**
	 * Pass a GET request to the specified page, with the specified data.
	 * @param path The path to send the request to.
	 * @param data The extra data to send.
	 * @return A Document object for the page.
	 * @throws IOException
	 */
	public Response get(String path, Map<String, String> data)
	{
		try {
			Response r = Jsoup
					.connect(path)
					.method(Method.GET)
					.cookies(cookies)
					.data(data)
					.timeout(10000)
					.execute();
			
			return r;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Pass a GET request to the specified page.
	 * @param path The path to send the request to.
	 * @return A Document object for the page.
	 * @throws IOException
	 */
	public Response get(String path)
	{
		try {
			Response r = Jsoup
					.connect(path)
					.method(Method.GET)
					.cookies(cookies)
					.timeout(10000)
					.execute();
			
			return r;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Pass a POST request to the specified page, with the specified data.
	 * @param path The path to send the request to.
	 * @param data The extra data to send.
	 * @return A Document object for the page.
	 * @throws IOException
	 */
	public Response post(String path, Map<String, String> data)
	{
		try {
			Response r = Jsoup
					.connect(path)
					.method(Method.POST)
					.cookies(cookies)
					.data(data)
					.timeout(10000)
					.execute();
			
			return r;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
