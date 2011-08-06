package logic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import util.Config;
import util.Crawler;
import util.GraphCreator;
import util.Printer;
import util.SQLHelper;
import util.WriteToFile;

public class Main {
	private static Config conf = null;
	private static boolean webapp = false;
	private static SQLHelper todb = null;
	private static String token = null;
	private static Crawler crawl = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 2) {
			System.out
					.println("[X] Usage: succulent <FACEBOOKID> <PATH TO CONFIG FILE>");
			System.exit(1);
		}

		else if (args.length == 3) {
			webapp = true;
			token = args[2];
		}

		else {
			token = ((Long) (System.currentTimeMillis() / 1000L)).toString();
		}

		// Configuration of succulent
		try {
			conf = new Config();
			conf.doConfig(args[1]);
			crawl = new Crawler(conf);
			todb = new SQLHelper();
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
			Printer.print("[X] Something went wrong during configuration!",
					webapp);
			System.exit(1);
		}

		// GO!
		ExecutorService executor = Executors.newFixedThreadPool(15);
		Future<ArrayList<String>> firstFriends = executor.submit(new Sucker(
				conf, args[0], crawl, todb));

		Map<String, Future<ArrayList<String>>> parallel = new HashMap<String, Future<ArrayList<String>>>();

		try {
			for (String friend : firstFriends.get()) {
				parallel.put(friend,
						executor.submit(new Sucker(conf, friend, crawl, todb)));
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}
		for (Map.Entry<String, Future<ArrayList<String>>> entry : parallel
				.entrySet()) {
			try {
				String fbid = entry.getKey();
				ArrayList<String> friends = entry.getValue().get();
				todb.insertFriends(friends, fbid);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		// Write Graph
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
		new WriteToFile().writeToFile(graph, conf.getGexfPath() + "/" + token);

		executor.shutdown();
		todb.die();
	}
}