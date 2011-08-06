package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Config {

	private String db_username;
	private String db_password;
	private String db_database;
	private int db_port;
	private String db_host;
	private String gexpath;

	private ArrayList<String> cookies = new ArrayList<String>();

	private String db_user_config = "db_user=";
	private String db_pass_config = "db_pass=";
	private String db_data_config = "db_data=";
	private String db_port_config = "db_port=";
	private String db_host_config = "db_host=";
	private String db_cook_config = "cookie=";

	private String gexfpath_config = "gexfpath=";

	private String facebookProfileURL = "http://www.facebook.com/profile.php?id=";

	public void doConfig(String configFile) throws IOException {
		if (configFile == null) {
			configFile = "config/succulent.conf";
		}
		BufferedReader in = new BufferedReader(new FileReader(configFile));
		String strLine;
		while ((strLine = in.readLine()) != null) {
			if (strLine.toLowerCase().startsWith(db_user_config)) {
				db_username = strLine.substring(db_user_config.length());
			} else if (strLine.toLowerCase().startsWith(db_pass_config)) {
				db_password = strLine.substring(db_pass_config.length());
			} else if (strLine.toLowerCase().startsWith(db_data_config)) {
				db_database = strLine.substring(db_data_config.length());
			} else if (strLine.toLowerCase().startsWith(db_port_config)) {
				db_port = new Integer(
						strLine.substring(db_port_config.length()));
			} else if (strLine.toLowerCase().startsWith(db_host_config)) {
				db_host = strLine.substring(db_host_config.length());
			} else if (strLine.toLowerCase().startsWith(db_cook_config)) {
				cookies.add(strLine.substring(db_cook_config.length()));
			} else if (strLine.toLowerCase().startsWith(gexfpath_config)) {
				gexpath = strLine.substring(gexfpath_config.length());
			}
		}
	}

	public String getUser() {
		return db_username;
	}

	public String getPass() {
		return db_password;
	}

	public String getHost() {
		return db_host;
	}

	public int getPort() {
		return db_port;
	}

	public String getDatabase() {
		return db_database;
	}

	public String getCookies() {
		StringBuilder sb = new StringBuilder();
		for (String cookie : cookies) {
			sb.append(cookie + ";");
		}
		return sb.toString();
	}

	public String getFacebookProfileURL() {
		return facebookProfileURL;
	}
	
	public String getGexfPath() {
		return gexpath;
	}
}
