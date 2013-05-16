package com.ioabsoftware.DroidFAQs.Networking;

import org.jsoup.Connection.Response;


public class History {

	private String path;
	public String getPath() {
		return path;}


	private HandlesNetworkResult.NetDesc desc;
	public HandlesNetworkResult.NetDesc getDesc() {
		return desc;}


	private Response res;
	public Response getRes() {
		return res;}


	private int vertPos;
	public int getVertPos() {
		return vertPos;}

	public History(String pathIn, HandlesNetworkResult.NetDesc descIn, Response resIn, int vertPosIn) {
		path = pathIn;
		desc = descIn;
		res = resIn;
		vertPos = vertPosIn;
	}
}
