package logic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

import util.Config;
import util.Crawler;
import util.Printer;
import util.SQLHelper;
import util.URLGetter;

public class Sucker implements Callable<ArrayList<String>> {
	private Config conf = null;
	private Crawler crawl = null;
	private SQLHelper todb = null;
	private boolean webapp = false;
	private String fbid = null;

	public Sucker(Config conf, String fbid, Crawler crawl, SQLHelper todb) {
		this.conf = conf;
		this.fbid = fbid;
		// This causes trouble with plenty of race conditions, workaround
		this.crawl = crawl;
		this.todb = todb;
	}

	private ArrayList<String> doUser() {
		URLGetter patientZero = new URLGetter(conf.getFacebookProfileURL()
				+ this.fbid, conf.getCookies());
		String zeroPage = null;
		try {
			zeroPage = patientZero.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, String> details = crawl.getDetails(zeroPage);
		ArrayList<String> friends = crawl.getFriends(zeroPage, this.fbid);
		try {
			todb.setConfig(conf);
			todb.insertUser(details);
			todb.insertFriends(friends, this.fbid);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		Printer.print("[!] Wrote user: " + details.get("name"), webapp);

		return friends;
	}

	@Override
	public ArrayList<String> call() throws Exception {
		return doUser();
	}
}