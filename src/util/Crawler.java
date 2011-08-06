package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

public class Crawler {

	private String common = "[&#;a-zA-Z0-9_\\-\\ ,/äöüßÄÖÜøéèêôáóíČŁâç\\.]{1,}";
	private Pattern findNameRegex = Pattern.compile("<title>" + common);
	private String htmlTags = "<[^>]*>";
	private Pattern sexRegex = Pattern
			.compile("Geschlecht</th><td class=\"data\">.");
	private String picPath = "http://profile.ak.fbcdn.net/hprofile-ak-[a-z0-9]{1,}/";
	private Pattern singleRegex = Pattern
			.compile("Beziehungsstatus</th><td class=\"data\">[a-zA-Z0-9\\_\\-\\ ]{1,}");
	private Pattern singleRegexWith = Pattern
			.compile("Beziehungsstatus</th><td class=\"data\">[a-zA-Z0-9\\_\\-\\ ]{1,}<[^>]*>"
					+ common);
	private Pattern livesRegex = Pattern.compile("Wohnt in <[^>]*>" + common);
	private Pattern birthRegex = Pattern
			.compile("Geboren am [0-9\\.\\ a-zA-Z]{1,}");
	private Pattern mailRegex = Pattern
			.compile("E-Mail</th><td class=\"data\"><ul class=\"uiList\"><li class=\"uiListItem  uiListVerticalItemBorder\">[a-zA-Z0-9\\_\\-\\.]{1,}@[a-zA-Z0-9\\_\\-\\.]{1,}");
	private Pattern wantsRegex = Pattern
			.compile("Interessiert an</th><td class=\"data\">" + common);
	private Pattern originRegex = Pattern
			.compile("Aus <[^>]*>[a-zA-Z0-9\\_\\-\\ äöüßÖÄÜø,/]{1,}");
	private Pattern foneRegex = Pattern
			.compile("Telefon</th><td class=\"data\"><ul class=\"uiList\"><li class=\"uiListItem  uiListVerticalItemBorder\">[\\ \\.0-9\\+\\-]{1,}");
	// private Pattern facebookIDRegex = Pattern
	// .compile("profile.php\\?id=[0-9]{1,}&amp;sk=info");
	private Pattern friendCountRegex = Pattern.compile("Freunde \\([0-9]{1,4}");
	private Pattern friendListRegex = Pattern
			.compile("addfriend.php\\?id=[0-9]{1,}");
	private Pattern defaultPic = Pattern
			.compile("http://profile.ak.fbcdn.net/static-ak/rsrc.php/[a-zA-Z0-9/\\.]{1,}gif");

	private Config conf;

	private String fbid = null;

	public Crawler(Config conf) {
		this.conf = conf;
	}

	private String getName(String search) {
		String name = getRegex(search, findNameRegex);
		if (name == null) {
			System.out.println("[X] Name not found for " + fbid + " in: "
					+ search);
			return "NULL";
		}
		return name.replaceAll(htmlTags, "");

	}

	private String getSex(String search) {
		String sex = getRegex(search, sexRegex);
		if (sex == null) {
			return "0";
		} else
			return sex.replaceAll("Geschlecht</th><td class=\"data\">", "");
	}

	private String getSingle(String search) {
		String single = getRegex(search, singleRegex);
		if (single == null) {
			return "NULL";
		}
		single = single.replaceAll("Beziehungsstatus</th><td class=\"data\">",
				"");

		if (single.contains("mit")) {
			single = getRegex(search, singleRegexWith).replaceAll(
					"Beziehungsstatus", "");
			single = single.replaceAll("<[^>]*>", "");
		}

		return single;
	}

	private String getLives(String search) {
		String lives = getRegex(search, livesRegex);
		if (lives == null) {
			return "NULL";
		}
		return lives.replaceAll("Wohnt in <[^>]*>", "");
	}

	private String getBirth(String search) {
		String birth = getRegex(search, birthRegex);
		if (birth == null) {
			return "NULL";
		}
		return birth.replaceAll("<[^>]*>", "").replaceAll("Geboren am ", "");
	}

	private String getMail(String search) {
		String mail = getRegex(search, mailRegex);
		if (mail == null) {
			return "NULL";
		}
		return mail
				.replaceAll(
						"E-Mail</th><td class=\"data\"><ul class=\"uiList\"><li class=\"uiListItem  uiListVerticalItemBorder\">",
						"");
	}

	private String getWants(String search) {
		String wants = getRegex(search, wantsRegex);
		if (wants == null) {
			return "NULL";
		}
		return wants.replaceAll("Interessiert an</th><td class=\"data\">", "");
	}

	private String getOrigin(String search) {
		String origin = getRegex(search, originRegex);
		if (origin == null) {
			return "NULL";
		}

		return origin.replaceAll("Aus <[^>]*>", "");
	}

	private String getFone(String search) {
		String fone = getRegex(search, foneRegex);
		if (fone == null) {
			return "NULL";
		}
		return fone
				.replaceAll(
						"Telefon</th><td class=\"data\"><ul class=\"uiList\"><li class=\"uiListItem  uiListVerticalItemBorder\">",
						"");
	}

	private String getFBID(String search) {
		return this.fbid;
	}

	private String getPicture(String search) {
		String fbid = getFBID(search);
		String userpicPath = getRegex(
				search,
				Pattern.compile(picPath + "[0-9]{1,}_" + fbid
						+ "_[0-9]{1,}_..jpg"));
		if (userpicPath == null) {
			userpicPath = getRegex(search, defaultPic);
			if (userpicPath == null) {
				userpicPath = "http://profile.ak.fbcdn.net/static-ak/rsrc.php/v1/yL/r/HsTZSDw4avx.gif";
			}
		}

		PicGetter url = new PicGetter(userpicPath, conf.getCookies());
		byte[] bytes = null;
		try {
			bytes = url.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Base64().encodeToString(bytes).replaceAll("\r\n", "");
	}

	private int getFriendCount(String search) {
		String counting = getRegex(search, friendCountRegex);
		if (counting == null) {
			return 0;
		}
		return new Integer(counting.replaceAll("Freunde \\(", ""));
	}

	public ArrayList<String> getFriends(String search) {
		int friendCount = getFriendCount(search);
		ArrayList<String> futureFriends = new ArrayList<String>();
		for (int i = 0; i <= friendCount + 60; i += 59) {
			String friendurl = "http://www.facebook.com/ajax/browser/list/friends/all/?uid="
					+ fbid + "&offset=" + i + "&dual=1&__a=1";
			try {
				futureFriends.add(new URLGetter(friendurl, conf.getCookies())
						.call());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String dirty = null;
		for (String futureFriend : futureFriends) {

			dirty += futureFriend.replaceAll("addfriend.php",
					"\r\naddfriend.php");
		}
		ArrayList<String> cleaned = new ArrayList<String>();
		for (String clean : getAllRegex(dirty, friendListRegex)) {
			String temp = clean.replaceAll("addfriend.php\\?id=", "");
			if (!cleaned.contains(temp)) {
				cleaned.add(temp);
			}
		}
		return cleaned;
	}

	public void setFBID(String fbid) {
		this.fbid = fbid;
	}

	public Map<String, String> getDetails(String page) {
		Map<String, String> detailMap = new HashMap<String, String>();
		Long crawltime = System.currentTimeMillis() / 1000L;
		detailMap.put("crawltime", crawltime.toString());
		detailMap.put("name", getName(page));
		detailMap.put("sex", getSex(page));
		detailMap.put("single", getSingle(page));
		detailMap.put("lives", getLives(page));
		detailMap.put("birth", getBirth(page));
		detailMap.put("mail", getMail(page));
		detailMap.put("wants", getWants(page));
		detailMap.put("origin", getOrigin(page));
		detailMap.put("fone", getFone(page));
		detailMap.put("fbid", getFBID(page));
		detailMap.put("pic", getPicture(page));
		return detailMap;

	}

	private String getRegex(String searchMe, Pattern regex) {
		Matcher m = regex.matcher(searchMe);
		String result = null;
		if (m.find()) {
			result = m.group();
		}
		return result;
	}

	private ArrayList<String> getAllRegex(String searchMe, Pattern regex) {
		// System.out.println(searchMe);
		Matcher m = regex.matcher(searchMe);
		ArrayList<String> result = new ArrayList<String>();
		while (m.find()) {
			result.add(m.group());
		}
		return result;
	}
}
