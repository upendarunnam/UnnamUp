package sample.api;

import java.sql.Connection;
import java.sql.DriverManager;

public class ApiUtils {

	public Connection getDbConn(String driverName,String url,String dbUser,String pwd) throws Exception {
		// TODO Auto-generated method stub
		
         if(pwd==null)pwd="";
		
		Class.forName (driverName); 
		Connection conn = DriverManager.getConnection (url, dbUser,pwd); 
		
		
		//String sql =  "CREATE TABLE USER_DATA1 (USER_NAME VARCHAR(255) not NULL, PHONE_NO  VARCHAR(50), PASSWORD VARCHAR(50)," + 
		 //		"	SEC_TOKEN VARCHAR(200),LAST_ACTIVE_TIME  TIMESTAMP ,PRIMARY KEY (USER_NAME))";
		 // Stmt.execute(sql); 
		
	return conn;	
	}

}
