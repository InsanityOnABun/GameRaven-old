package com.ioabsoftware.gameraven.views.rowdata;

public class GameSearchRowData extends BaseRowData {

	private String name, platform, url;
	public String getName() {return name;}
	public String getPlatform() {return platform;}
	public String getUrl() {return url;}
	
	public GameSearchRowData(String nameIn, String platformIn, String urlIn) {
		name = nameIn;
		platform = platformIn;
		url = urlIn;
	}
	
	@Override
	public RowType getRowType() {
		return RowType.GAME_SEARCH;
	}

}
