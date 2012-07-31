package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.*;

//class responsible for connection with database;

public class DBConnection{
	
	private String drivername = "jdbc:";
	
	private String subprotocol = "postgresql://";
	private String host;
	private String port;
	private String dbname;
	private String url;
	
	private String user;
	private String pass;
	
	private Connection c = null;
	
	private BufferedReader console; 
	
	
	public DBConnection() {
		
		try{
			console = new BufferedReader( new InputStreamReader (System.in ) );
			
			//data inputting;
			//TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST//		
/*			System.out.print("Database IP or localhost if you use local database:");
			host = console.readLine();
			
			System.out.print( "Port: " );
			port = console.readLine();
			
			System.out.print( "Database name: " );
			dbname = console.readLine();
			
			System.out.print( "User: " );
			user = console.readLine();

			
			System.out.print( "Password: " );
			pass = console.readLine();

*/			
			host = "localhost";
			port = "1502";
			dbname = "parking";
			user = "postgres";
			pass = "passpass";
			
			this.url = drivername + subprotocol + host + ":" + port + "/"+ dbname;
			
			Class.forName("org.postgresql.Driver");	//load driver, throws ClassNotFoundException;
		}
		
		catch( ClassNotFoundException e ){
			System.out.println( "Can't load driver!" );
			System.out.println( e.getMessage() );
			System.exit( 1 );
			
//TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST-TEST//
/*			
		} catch (IOException e) {
			System.out.println( "Error! Can't read data from console!" );
			System.out.println( e.getMessage() );
			System.exit( 1 );
*/		
		}
		
		System.out.println("PostgreSQL JDBC Driver Registered!");
		
		try{
			c = DriverManager.getConnection( this.url, this.user, this.pass );		//connect to database, throws SQLException;		
		}
		catch(SQLException e){
			System.out.println( "Can't connect to database " + url );
			System.out.println( e.getMessage() );
			System.exit( 1 );

		}
		
		System.out.println( "Connection to database is successful" );
	}
	
	public Connection getConnection(){
		return c;
	}
	
	public String getURL(){
		return url;
	}
	
	public void disconnect() {
		try{
			c.close();
		}catch(SQLException e){
			System.out.println("Disconnection is unsuccessful");
			System.out.println( e.getMessage() );
			System.exit(1);
		}
	}
}

