package logic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import util.Config;
import util.Crawler;
import util.GraphCreator;
import util.Printer;
import util.SQLHelper;
import util.URLGetter;
import util.WriteToFile;

public class Main {
	private static Config conf = null;
	private static Crawler crawl = null;
	private static SQLHelper todb = null;
	private static boolean webapp = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 2) {
			System.out
					.println("[X] Usage: succulent <FACEBOOKID> <PATH TO CONFIG FILE>");
			System.exit(1);
		}

		if (args.length == 3) {
			webapp = true;
		}
		// Configuration of succulent
		try {
			conf = new Config();
			conf.doConfig(args[1]);
		} catch (IOException e) {
			Printer.print("[X] Configuration error! Is the config file '"
					+ args[1] + "' there?", webapp);
			System.exit(1);
		} catch (Exception ex) {
			Printer.print("[X] Unknown error: ", webapp);
			ex.printStackTrace();
			System.exit(1);
		}

		if (conf == null) {
			Printer.print("[X] Something went wrong during configuration!", webapp);
			System.exit(1);
		}
		crawl = new Crawler(conf);
		URLGetter patientZero = new URLGetter(conf.getFacebookProfileURL()
				+ args[0], conf.getCookies());
		String zeroPage = null;
		try {
			zeroPage = patientZero.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		crawl.setFBID(args[0]);
		Map<String, String> details = crawl.getDetails(zeroPage);
		ArrayList<String> friends = crawl.getFriends(zeroPage);
		todb = new SQLHelper();
		try {
			todb.setConfig(conf);
			todb.insertUser(details);
			todb.insertFriends(friends, args[0]);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		Printer.print("[!] Wrote user: " + details.get("name"), webapp);
		recurse(friends);
		String graph = null;
		try {
			todb.resetGraphDB();
			todb.createGraphDB(null);
			GraphCreator creator = new GraphCreator(conf);
			creator.setLayout(null);
			Printer.print("[!] Calculating graph... ", webapp);
			graph = creator.createGraphFromSQL();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Printer.print("[!] Writing gexf file... ", webapp);
		new WriteToFile()
				.writeToFile(graph, conf.getGexfPath() + "/" + args[0]);
	}

	public static void recurse(ArrayList<String> friends) {
		// first recurse
		ArrayList<String> nextFriends = null;
		for (String friend : friends) {
			URLGetter nextPatient = new URLGetter(conf.getFacebookProfileURL()
					+ friend, conf.getCookies());
			String nextPage = null;
			try {
				nextPage = nextPatient.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
			crawl.setFBID(friend);
			Map<String, String> nextDetails = crawl.getDetails(nextPage);
			nextFriends = crawl.getFriends(nextPage);

			try {
				todb.setConfig(conf);
				todb.insertUser(nextDetails);
				todb.insertFriends(nextFriends, friend);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			Printer.print("[!] Wrote user: " + nextDetails.get("name"), webapp);
		}
	}
}