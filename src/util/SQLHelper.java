package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

public class SQLHelper {
	private Config conf = null;
	private Connection connect = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	public void setConfig(Config conf) throws ClassNotFoundException,
			SQLException {
		this.conf = conf;
		Class.forName("com.mysql.jdbc.Driver");
		connect = DriverManager.getConnection("jdbc:mysql://"
				+ this.conf.getHost() + "/" + this.conf.getDatabase() + "?"
				+ "user=" + this.conf.getUser() + "&password="
				+ this.conf.getPass());
	}
	
	public void die() {
		try {
			connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void resetGraphDB() throws SQLException {
		String nodes = "truncate table nodes;";
		String edges = "truncate table edges;";
		Statement stmt = connect.createStatement();
		stmt.execute(nodes);
		stmt.execute(edges);
	}

	public void createGraphDB(ArrayList<String> ids) throws SQLException {
		String nodes = "insert into nodes(id,label,url,sex,single) select fbid,name,concat('http://www.facebook.com/profile.php?id=',fbid),sex,single from users;";
		String edges = "insert into edges select users.fbid as source, friends.friendfbid as target, '5' as weight, 'knows' as name from users join friends where friends.userid=users.id";
		preparedStatement = connect.prepareStatement(nodes);
		preparedStatement.execute();
		preparedStatement = connect.prepareStatement(edges);
		preparedStatement.execute();
	}

	public synchronized void insertUser(Map<String, String> details) throws SQLException {
		String sql = "select fbid from users where fbid=?";
		preparedStatement = connect.prepareStatement(sql);
		preparedStatement.setString(1, details.get("fbid"));
		resultSet = preparedStatement.executeQuery();
		String fbid = null;
		while (resultSet.next()) {
			fbid = resultSet.getString(1);
		}
		
		if (fbid != null && details.get("fbid").compareTo(fbid) == 0) {
			return;
		}
		
		sql = "insert into users(name, fbid, sex, pic, crawltime, single, lives, birth, mail, wants, origin) values(?,?,?,?,?,?,?,?,?,?,?)";
		preparedStatement = connect.prepareStatement(sql);
		preparedStatement.setString(1, details.get("name"));
		preparedStatement.setString(2, details.get("fbid"));
		preparedStatement.setString(3, details.get("sex"));
		preparedStatement.setString(4, details.get("pic"));
		preparedStatement.setString(5, details.get("crawltime"));
		preparedStatement.setString(6, details.get("single"));
		preparedStatement.setString(7, details.get("lives"));
		preparedStatement.setString(8, details.get("birth"));
		preparedStatement.setString(9, details.get("mail"));
		preparedStatement.setString(10, details.get("wants"));
		preparedStatement.setString(11, details.get("origin"));
		preparedStatement.execute();
	}

	public synchronized void insertFriends(ArrayList<String> friends, String fbid)
			throws SQLException {
		
//		String sql = "select fbid from friends where fbid=? limit 1";
//		preparedStatement = connect.prepareStatement(sql);
//		preparedStatement.setString(1, fbid);
//		resultSet = preparedStatement.executeQuery();
//		
		// TODO: we cannot unique this, as the edges weight seems to depend on it
//		String checkfbid = null;
//		while (resultSet.next()) {
//			checkfbid = resultSet.getString(1);
//		}
//		if (checkfbid != null && checkfbid.compareTo(fbid) == 0) {
//			//System.out.println("not inserting");
//			return;
//		}
		
		String sql = "select id from users where fbid=? limit 1";
		preparedStatement = connect.prepareStatement(sql);
		preparedStatement.setString(1, fbid);
		resultSet = preparedStatement.executeQuery();
		String userid = null;
		while (resultSet.next()) {
			userid = resultSet.getString(1);
			//System.out.println(userid);
		}

		for (String friend : friends) {
			sql = "insert into friends(userid,fbid,friendfbid) values(?,?,?)";
			preparedStatement = connect.prepareStatement(sql);
			preparedStatement.setString(1, userid);
			preparedStatement.setString(2, fbid);
			preparedStatement.setString(3, friend);
			preparedStatement.execute();
		}

	}
}
