package com.ioabsoftware.gameraven.views.rowdata;

public class GameSearchData extends BaseRowData {

	private String name, platform;
	public String getName() {return name;}
	public String getPlatform() {return platform;}
	
	public GameSearchData(String nameIn, String platformIn) {
		name = nameIn;
		platform = platformIn;
	}
	
	@Override
	public RowType getRowType() {
		return RowType.GAME_SEARCH;
	}

}
