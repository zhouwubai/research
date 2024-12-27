package fiu.kdrg.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.out.println("Cannot Load Driver!");
			return null;
		}
		
		try {
			return DriverManager.getConnection("jdbc:mysql://proceed.cs.fiu.edu:33061/ADSE", "cshen001",
					"1");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static Connection getDisasterConnection(){
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.out.println("Cannot Load Driver!");
			return null;
		}
		
		try {
			return DriverManager.getConnection("jdbc:mysql://rescue.cs.fiu.edu:33061/disaster", "hadoop",
					"zwb");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static void main(String[] args) {
		
		Connection conn = DBConnection.getDisasterConnection();
		System.out.println(conn);
		
	}

}
