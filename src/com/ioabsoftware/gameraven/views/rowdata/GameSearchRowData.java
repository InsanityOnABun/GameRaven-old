package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

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
