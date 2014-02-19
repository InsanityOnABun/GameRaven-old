package com.ioabsoftware.gameraven.db;

import org.jsoup.nodes.Document;

import com.ioabsoftware.gameraven.networking.HandlesNetworkResult;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;


public class History {

	private String path;
	public String getPath() {
		return path;}


	private HandlesNetworkResult.NetDesc desc;
	public HandlesNetworkResult.NetDesc getDesc() {
		return desc;}


	private Document doc;
	public Document getDoc() {
		return doc;}


	private int vertPos[];
	public int[] getVertPos() {
		return vertPos;}

	public History(String pathIn, HandlesNetworkResult.NetDesc descIn, Document docIn, int vertPosIn[]) {
		path = pathIn;
		desc = descIn;
		doc = docIn;
		vertPos = vertPosIn;
	}
}
