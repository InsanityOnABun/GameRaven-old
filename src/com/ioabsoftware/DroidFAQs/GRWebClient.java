package com.ioabsoftware.DroidFAQs;

import java.util.HashMap;

import com.ioabsoftware.DroidFAQs.HandlesNetworkResult.NetDesc;
import com.ioabsoftware.DroidFAQs.Networking.Session;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class GRWebClient extends WebViewClient {
	
	private GRAllInOne grAIO;
	
	private static boolean doPictureListener = false;
	public static boolean doPictureListener()
	{return doPictureListener;}
	public static void disablePictureListener()
	{doPictureListener = false;}
	
	public GRWebClient(GRAllInOne aio) {
		grAIO = aio;
	}

	@Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		// board example: http://m.gamefaqs.com/boards/400-current-events
		// topic example: http://m.gamefaqs.com/boards/400-current-events/64300205
		grAIO.wtl("clicked url: " + url);

		if (url.endsWith("GAMERAVEN_HOME")) {
			grAIO.getCurrSession().get(NetDesc.BOARD_JUMPER, Session.ROOT + "/boards", null);
			return true;
		}
		
		if (url.endsWith("GAMERAVEN_POST_ON_TOPIC")) {
			grAIO.postOnTopicSetup(false);
			return true;
		}
		
		if (url.contains("GAMERAVEN_VOTE_TOPICPOLL")) {
			int nameStart = url.indexOf("?name=") + 6;
			int nameEnd = url.indexOf('&', nameStart);
			int valueStart = url.indexOf("&value=") + 7;
			int valueEnd = url.indexOf('&', valueStart);
			int keyStart = url.indexOf("&key=") + 5;
			int keyEnd = url.length();
			
			String name = url.substring(nameStart, nameEnd);
			String value = url.substring(valueStart, valueEnd);
			String key = url.substring(keyStart, keyEnd);
			
			String path = url.substring(0, url.indexOf("GAMERAVEN_VOTE_TOPICPOLL"));
			
			HashMap<String, String> data = new HashMap<String, String>();
			data.put(name, value);
			data.put("key", key);
			
			grAIO.getCurrSession().post(NetDesc.TOPIC, path, data);
			return true;
		}

		if (url.endsWith("GAMERAVEN_POST_ON_BOARD")) {
			grAIO.postOnBoardSetup();
			return true;
		}

		if (url.endsWith("GAMERAVEN_SEARCH_BOARD")) {
			grAIO.onSearchRequested();
			return true;
		}

		if (url.endsWith("GAMERAVEN_SEARCH_GAMES")) {
			grAIO.onSearchRequested();
			return true;
		}
		
		if (url.contains("GAMERAVEN_QUOTE-")) {
			int x = url.indexOf("GAMERAVEN_QUOTE-") + 16;
			int id = Integer.parseInt(url.substring(x));
			grAIO.quoteSetup(id);
			return true;
		}
		
		if (url.contains("GAMERAVEN_MESSAGE_DETAIL")) {
			String parsedUrl = url.substring(0, url.indexOf("GAMERAVEN_MESSAGE_DETAIL"));
			grAIO.getCurrSession().get(NetDesc.MSG_DETAIL, parsedUrl, null);
			return true;
		}
		
		if (url.contains("GAMERAVEN_EDIT_POST")) {
			grAIO.editPostSetup();
			return true;
		}
		
		if (url.contains("GAMERAVEN_MARK_POST")) {
			grAIO.wtl("Mark post link clicked");
			int markPostFlag = url.indexOf("GAMERAVEN_MARK_POST");
			String extraData = url.substring(markPostFlag + 19);
			int sep = extraData.indexOf('?');
			String reason = extraData.substring(0, sep);
			String key = extraData.substring(sep + 1);
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("reason", reason);
			data.put("key", key);

			grAIO.wtl("firing mark post network action");
			grAIO.getCurrSession().post(NetDesc.MARK_MSG, grAIO.getCurrSession().getLastPath() + "?action=mod", data);
			return true;
		}
		
		if (url.contains("GAMERAVEN_DELETE_POST")) {
			grAIO.wtl("Delete post link clicked");
			int deletePostFlag = url.indexOf("GAMERAVEN_DELETE_POST");
			String key = url.substring(deletePostFlag + 21);
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("YES", "Delete this Post");
			data.put("key", key);

			grAIO.wtl("firing delete post network action");
			grAIO.getCurrSession().post(NetDesc.DELETE_MSG, grAIO.getCurrSession().getLastPath() + "?action=delete", data);
			return true;
		}
		
		if (url.contains("GAMERAVEN_CLOSE_TOPIC")) {
			grAIO.wtl("Mark post link clicked");
			int closeTopicFlag = url.indexOf("GAMERAVEN_CLOSE_TOPIC");
			String key = url.substring(closeTopicFlag + 21);
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("YES", "Close This Topic");
			data.put("key", key);

			grAIO.wtl("firing mark post network action");
			grAIO.getCurrSession().post(NetDesc.CLOSE_TOPIC, grAIO.getCurrSession().getLastPath() + "?action=closetopic", data);
			return true;
		}
		
		
		// check if this is an AMP list url
		if (url.contains("GAMERAVEN_AMP_LIST")) {
			grAIO.getCurrSession().get(NetDesc.AMP_LIST, buildAMPLink(), null);
			return true;
		}
		
		
		// check if this is a send pm link
		if (url.contains("GAMERAVEN_SEND_PM")) {
			int start = url.indexOf("GAMERAVEN_SEND_PM") + 17;
			int firstSep = url.indexOf("~-_", start);
			int secondSep = url.indexOf("~-_", firstSep + 3);
			int finalSep = url.indexOf("~-_", secondSep + 3);
			
			String to = url.substring(start, firstSep);
			String subject = url.substring(firstSep + 3, secondSep);
			String message = url.substring(secondSep + 3, finalSep);
			
			grAIO.pmSetup(to, subject, message);
			return true;
		}
		
		
		// check if this is a PM link
		if (url.contains("/pm/")) {
			if (url.contains("?id=")) {
				grAIO.getCurrSession().get(NetDesc.READ_PM, url, null);
			}
			else {
				grAIO.getCurrSession().get(NetDesc.PM_INBOX, url, null);
			}
			return true;
		}
		
		
		// check if this is an different AMP page
		if (url.contains("myposts.php")) {
			grAIO.getCurrSession().get(NetDesc.AMP_LIST, url, null);
			return true;
		}
		
		if (url.contains("www.gamefaqs.com")) {
			url = url.replace("www.gamefaqs.com", "m.gamefaqs.com");
		}
		
		if (url.contains("http://gamefaqs.com")) {
			url = url.replace("http://gamefaqs.com", "http://m.gamefaqs.com");
		}
		
		// Check if this isn't a GFAQs url
		if (!url.startsWith(Session.ROOT)) {
			//Pass it to the system, doesn't match our domain
	        Intent intent = new Intent(Intent.ACTION_VIEW);
	        intent.setData(Uri.parse(url));
	        grAIO.startActivity(intent);
	        return true;
		}
		
		// check if this is a board or topic url
		if (url.contains("boards")) {
			
			if (url.contains("boardlist.php")) {
				grAIO.getCurrSession().get(NetDesc.BOARD_LIST, url, null);
				return true;
			}
			
			String boardUrl = url.substring(url.indexOf("boards"));
			 if (boardUrl.contains("/")) {
				 
				 String slashUrl = boardUrl.substring(boardUrl.indexOf("/") + 1);
				 if (slashUrl.contains("/")) {
					 
					 // should be a topic
					 grAIO.getCurrSession().get(NetDesc.TOPIC, url, null);
				 }
				 
				 else {
					 
					 // should be a board
					 grAIO.getCurrSession().get(NetDesc.BOARD, url, null);
				 }
			 }
			 else {
				 
				 // should be home
				 grAIO.getCurrSession().get(NetDesc.BOARD_JUMPER, url, null);
			 }
			 
			 return true;
		}
        
		// check if this is a game search
		if (url.contains("/search/index.html")) {
			 grAIO.getCurrSession().get(NetDesc.GAME_SEARCH, url, null);
			 return true;
		}
		
		else
			return false;
    }
	
	@Override
	public void onPageFinished (WebView view, String url) {
		doPictureListener = true;
	}
	
	public static String buildAMPLink() {
		return "http://m.gamefaqs.com/boards/myposts.php?lp=" + GRAllInOne.getSettingsPref().getString("ampSortOption", "-1");
	}
}
