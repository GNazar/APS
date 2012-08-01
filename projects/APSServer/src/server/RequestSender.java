package server;


import java.sql.*;
import java.util.*;

import lpr.*;
import lpr.Number;
import manager.message.*;

//class responsible for making requests to database;
public class RequestSender {
	
	
	private Statement s;
	private DBConnection c;

	private Object compSynch;
	
	public RequestSender( ){
		
		try{
			c = new DBConnection();
			Connection con = c.getConnection();
			s = con.createStatement();
			
			DatabaseMetaData md = c.getConnection().getMetaData();
			String[] type = {"TABLE"};
			ResultSet rs = md.getTables( null, null, "%", type );	//check number of tables, must be 6;
			int i = 0; 
			while (rs.next()) {					//count number of tables;
			  i++;
			}
			
			if(i == 0){							//database scheme isn't created; 
				System.out.println( "First launching!" );
				this.creteDB();
			}else
				if ( i > 6 ){					//scheme isn't completed;	
					System.out.println( "Database scheme isn't completed !" );
					System.exit( 1 );
				}
		}catch( SQLException e){
			System.out.println( "Error while creating statment!" );
			System.out.println( e.getMessage() );
			System.exit( 1 );
		}
		
		compSynch = new Object();	//object for database access synchronization
	}
	
	//	function return company id by name;
	public int getCompanyId( String name ) throws SQLException {
		int result = 0;
		String str = "SELECT id FROM company WHERE name = '%s'";
		String sql = String.format( str, name );
		ResultSet rs;
		
		synchronized( this.compSynch ){
			rs = s.executeQuery( sql );
		}
		
		if ( rs.next() )
			result = rs.getInt( 1 );		
	
		return result;
	}
	//function add new company in table "company" and add administrator in "users";
	public int addCompany( String name, String adm_login, String adm_pass, String mark ){
		int result = -1;
		ResultSet rs;
		int id;
		String str = "INSERT INTO company VALUES ( DEFAULT, '%s', '%s', '%s', '%s' )";
		String str1 = "SELECT id FROM company WHERE name = '%s'";
		String str2 = "INSERT INTO users VALUES ( %d, '%s', '%s', true )";
		String sql = String.format(str, name, adm_login, adm_pass, mark );
		String sql1 = String.format( str1, name );
		String sql2;
		try{
			synchronized (this.compSynch){
				s.executeUpdate(sql);
				rs = s.executeQuery( sql1 );			//add company;
			}
			
			if (rs.next()){
				id = rs.getInt( 1 );
				sql2 = String.format( str2, id, adm_login, adm_pass );
				
				synchronized ( this.compSynch ){
					result = s.executeUpdate( sql2 );	//add user;
				}
			}
			
		}catch( SQLException e ){
			System.out.println( "Error while inserting company in database!" );
			System.out.println( e.getMessage() );
		}
	
		return result;
	}
	
	// delete company from table "company" and delete appropriate records
	//in other tables;
	
	public int deleteCompany( String name ){
		
		ResultSet rs;
		int result = -1;
		int index = 0;
		
		String str = "DELETE FROM company WHERE id = %d";
		String str0 = "SELECT id FROM company WHERE name = '%s'";
		String str1 = "DELETE FROM parking WHERE company_id = %d";
		String str2 = "DELETE FROM permissions WHERE company_id = %d";
		String str3 = "DELETE FROM users WHERE company_id = %d";
	
		String sql0 = String.format( str0, name );
		
		try{
			
			synchronized ( this.compSynch ){
				rs = s.executeQuery(sql0);
			}
			
			if ( rs.next() )
				index = rs.getInt( 1 );
			else return 0;
			
			String sql1 = String.format( str1, index );
			String sql2 = String.format( str2, index );
			String sql3 = String.format( str3, index );
			
			String sql = String.format( str, index );
			
			synchronized ( this.compSynch ){
				s.executeUpdate( sql1 );
				s.executeUpdate( sql2 );
				s.executeUpdate( sql3 );
				result = s.executeUpdate( sql );
			}

			
		}catch( SQLException e ){
			System.out.println( "Error deleting company from database" );
			System.out.println( e.getMessage() );
		}
		
		return result;
	}
	//function output list of companies
	public int companies(){
		int result = 0;
		ResultSet rs;
		String str = "SELECT name, adm_login, adm_pass, mark FROM company";
		
		String name, login, pass, mark; 
		
		try{
			
			synchronized ( this.compSynch ){
				rs = s.executeQuery( str );
			}
			
			while ( rs.next() ){
				name = rs.getString( 1 );
				login = rs.getString( 2 );
				pass = rs.getString( 3 );
				mark = rs.getString( 4 );
				System.out.printf( "%-20s %-20s %-20s %s \n", name, login, pass, mark );	
				
				result++;
			}
			
		}
		catch ( SQLException e ){
			System.out.println( "Error while getting list of companies" );
			System.out.println( e.getMessage() );
		}
		
		return result;
	}
	
	//function return company id, and parking id 
	//by company name and parking address;
	public int[] getParkingId( ParkingId id){
		int[] result = new int[2];
		
		String str = "SELECT company_id, parking_id FROM parking WHERE " +
				"address = '%s' AND company_id = ( SELECT id FROM company WHERE name = '%s' )";
	
		String sql = String.format(str, id.getAddress(), id.getCompany() );
		ResultSet rs;
		try{
			synchronized ( this.compSynch ){
				rs = s.executeQuery(sql);
			}
			
			if ( rs.next() ){
				result[0] = rs.getInt( 1 );
				result[1] = rs.getInt( 2 );
			}else {
				result[0] = 0;
				result[1] = 0;
			}
			
		}catch( SQLException e ){
			System.out.println( "Error while register parking!" );
			System.out.println( e.getMessage() );
			
			result[0] = -1;
			result[0] = -1;
		}
		
		return result; 	
	}
	
	//add to table "history" information about event
	//( open or don't open parking door ) in parking
	public int insertEnter( Enter e, final int compId, final int parkId ){
		
		int res = 0;
		String compName, parkAddr;
		ResultSet rs;
		String str1 = "SELECT company.name, parking.address FROM company, parking WHERE company.id = %d AND parking.parking_id = %d";
		String str3 = "INSERT INTO history VALUES ( '%s', '%s', %d , '%s', %b )";		//parameters: company name, address, time, number, isfound;
		
		String sql1 = String.format( str1, compId, parkId );
		try{
			synchronized ( this.compSynch ){
				rs = s.executeQuery( sql1 );
			}
			if ( rs.next() ){				//get company name and parking address;
				compName = rs.getString( 1 );
				parkAddr = rs.getString( 2 );
			
				String sql3 = String.format(str3, compName, parkAddr, e.date, new String ( e.getNumber().getValue() ), e.isFound );
			
				synchronized ( this.compSynch ){	//insert record;
					res = s.executeUpdate( sql3 );
				}	
			}
		}catch( SQLException ex ){
			System.out.println( "Error while updating history!" );
			ex.printStackTrace();
			System.out.println( ex.getMessage() );
			res = -1;
		}
		
		return res; 
	}
	
	
	public Set<Number> getCarUpdating( final int c, final int p) throws SQLException {
		
		Set< Number > res = new HashSet< Number >();		//result set of cars number;
		String str = "SELECT car.number FROM car WHERE car_id IN " +
				"( SELECT car_id FROM permissions WHERE company_id = %d AND parking_id = %d )";
		Number n;
		ResultSet rs;
		String sql = String.format( str, c, p );
		String st;
		try{
			synchronized( this.compSynch ){
				rs = s.executeQuery(sql);
			}
			
			while ( rs.next() ){
				st = rs.getString( 1 );					//get number;
				n = new Number( st.toCharArray() );
				res.add( n );							//add number to result list;
			}
			
		}catch( SQLException e ){
			System.out.println( "Error while sampling cars!" );
			System.out.println( e.getMessage() );
			e.printStackTrace();
		}	
		
		return res;
	}
	
	//function return  0 - if user not found, 1 - if user type "user" 
	//and 2 - if user type "administrator";
	public int checkUser( User u ){	
		int result = 0;
		ResultSet rs;
		
		String str = "SELECT type FROM users WHERE login = '%s' " +
				"AND password = '%s' AND company_id = " +
				"(SELECT id FROM Company WHERE name = '%s') ";
		
		String sql = String.format( str, u.getLogin(), u.getPass(), u.getCompanyName() );
		
		try {
			
			synchronized ( this.compSynch ){
				rs = s.executeQuery( sql.toString() );		//get type of user;
			}
			
			if ( ! rs.next() )
				return result;
			
			result = rs.getBoolean( 1 ) ? 2 : 1; 	

		} catch (SQLException e) {
			System.out.println( "Error while register new user!" );
			System.out.println( e.getMessage() );
			result = -1;
		}
		
		return result;
	}
	
	//function return list of cars which try to enter
	//parameters p1 and p2 - date
	//if parameter don't take part in history sampling it's value - null; 
	public ArrayList<MEnter> sampleHistory( long d1, long d2, final int compId, String parkAddr, Number numb, final int isFound ){
		
		ArrayList<MEnter> res = new ArrayList<MEnter>();
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		MEnter tmp = null;
		
		//generate request;
		sql.append("select * from history where (company = (select name from company where id = ");
		sql.append( compId );
		sql.append(")");
		
		if ( parkAddr != null ){			//add sample by address;
			sql.append( ") and (parking_id = (select parking_id from parking where address = '" );
			sql.append( parkAddr );
			sql.append("')");
		}
		if ( d1 >= 0){						//add sample by date;
			sql.append( ") and (date > " );
			sql.append( d1 );
			sql.append( ") and (date < " );
			sql.append( d2 );
			
		}
		if ( numb != null){					//add sample by number;
			sql.append( ") and (car_id = '" );
			sql.append( new String ( numb.getValue() ) );
			sql.append("'");
		}
		
		if ( isFound >= 0 ){			//add sample by availability;
			sql.append( ") and (isfound = " );
			sql.append( isFound == 1 );
		}
		sql.append(")");
				
		String pid = null;
		long date = 0;
		Number n = null;
		boolean b = false;
		int t = -1;
		try {
			synchronized ( this.compSynch ){
				rs = s.executeQuery( sql.toString() );
			}
			
			while (rs.next()){					//parse requests result; 
				pid = rs.getString( 2 );		//parking address;
				date = rs.getLong( 3 );			//date;
				n = new Number ( rs.getString( 4 ).toCharArray() );		//date;
				b = rs.getBoolean( 5 );			//if found in database;
				t = b ? 1 : 0;
				tmp = new MEnter( -1, date, pid, n, t );
				res.add( tmp );					//add to result list;
			}
		} catch (SQLException e) {
			
			System.out.println( "Error while sampling history!" );
			System.out.println( e.getMessage() );
		}
		
		return res;
	}
	
	//function return company id by its name;
	public int initManager( String str ){
		int result = -1;
		ResultSet rs;
		String sql = "select id from company where name = '" + str + "'";
		try {
			synchronized ( this.compSynch ){
				rs = s.executeQuery( sql );
			}
			if ( rs.next() )
				result = rs.getInt( 1 );	//get company id;
			
		} catch (SQLException e) {
			System.out.println( "Error while manager initialization!" );
		}
		
		return result;
	}
	
	//function add car to table "car" and add record to table "permissins"
	public int addCar( int compId, MCar c ){
		int result = -1;
		String numb = new String ( c.getNumber().getValue() );

		String str1 = "INSERT INTO car VALUES( DEFAULT, '%s', %d )";
		String sql1 = String.format(str1, numb, c.getTime() );
		
		String str2 = "INSERT INTO Permissions ( parking_id, car_id, company_id ) " +
				"SELECT parking.parking_id, car.car_id,company.id FROM company, parking, car " +
				"WHERE company.id = %d AND ( parking.parking_id = " +							
				"(SELECT parking_id FROM parking WHERE address = '%s') " +						
				"AND parking.company_id = %d ) AND car.number = '%s'";							
		
		String sql2 = String.format(str2, compId, c.getAddress(), compId, numb );
		
		try{
			synchronized ( this.compSynch ){
				s.executeUpdate( sql1 );			//insert record to table "car";
				result = s.executeUpdate( sql2 );	//insert record to table "permissions";
			}
			
		}catch ( SQLException e ){
			System.out.println ( "Error while inserting car in database!" );
			System.out.println( e.getMessage() );
		}
		
		return result;
	}
	
	//function delete car from table "car" and delete record from table "permissions";
	public int deleteCar( int compId, MCar c ){
		int result = -1;
		String numb = new String ( c.getNumber().getValue() );
		
		String str1 = "DELETE FROM car WHERE number = '%s'";
		String sql1 = String.format( str1, numb );
		
		String str2 = "DELETE FROM  Permissions WHERE " +
				"Permissions.company_id = %d AND " +													
				"Permissions.parking_id = (SELECT parking_id FROM parking WHERE address = '%s' " +		
				"AND parking.company_id = %d ) AND Permissions.car_id = " +								
				"(SELECT Car.car_id FROM Car WHERE Car.number = '%s') ";								
		
		String sql2 = String.format(str2, compId, c.getAddress(), compId, numb );
		
		try{
			synchronized ( this.compSynch ){	
				result = s.executeUpdate( sql2 );
				if ( result > 0 )
					result = s.executeUpdate( sql1 );
			}
			
		}catch ( SQLException e ){
			System.out.println ( "Error while inserting car in database!" );
			System.out.println( e.getMessage() );
			result = -1;
		}
		
		return result;
	}
	
	//function add new user to table "users"
	//"compId" - company id;
	public int addUser( int compId, MUser u ){
		int result = -1;
		String str = "INSERT INTO users VALUES ( %d, '%s', '%s', false )";
		String sql = String.format(str, compId, u.getLogin(), u.getPass() );
		
		try{
	
			synchronized ( this.compSynch ){
				result = s.executeUpdate( sql );
			}
			
		}catch( SQLException e ){
			System.out.println ( "Error while adding user in database!" );
			System.out.println( e.getMessage() );
		}
		
		return result;
	}
	
	//function delete user from table "users"
	//"compId" - company id;
	public int deleteUser( int compId, MUser u ){
		int result = -1;
		String str = "DELETE FROM users WHERE company_id = %d and login = '%s'";
		String sql = String.format(str, compId, u.getLogin() );
		
		try{
			synchronized ( this.compSynch ){
				result = s.executeUpdate( sql );
			}
			
		}catch( SQLException e ){
			System.out.println ( "Error while deleting user from database!" );
			System.out.println( e.getMessage() );
		}
		
		return result;
	}
	
	//function add new parking to table "parking"
	//"compId" - company id;
	public int addParking( final int compId, MParking mp ){
		int result = -1;
		String str = "INSERT INTO parking VALUES ( DEFAULT, %d, '%s' )";
		String sql = String.format(str, compId, mp.getAddress() );
		try{
			synchronized ( this.compSynch ){
				result = s.executeUpdate(sql);
			}
		}catch( SQLException e ){
			System.out.println ( "Error while adding parking in database!" );
			System.out.println( e.getMessage() );
	
		}
		
		return result;
	}

	//function add new parking to table "parking"
	//"compId" - company id;
	public int deleteParking( final int compId, MParking mp ){
		int result = -1;
		String str = "DELETE FROM Parking WHERE company_id = %d AND address = '%s' ";
		String sql = String.format(str, compId, mp.getAddress() );
		try{
			synchronized ( this.compSynch ){
				result = s.executeUpdate(sql);
			}
		}catch( SQLException e ){
			System.out.println ( "Error while adding parking in database!" );
			System.out.println( e.getMessage() );
		}
		
		return result;
	}
	
	//function return list of cars by company id;
	public LinkedList<MCar> cars( final int compId ){
		MCar mc;
		LinkedList<MCar> result = new LinkedList<MCar>();
		ResultSet rs;
		Number numb;
		String addr;
		long date;
		
		String str = "SELECT car.number, parking.address, car.date FROM car, parking WHERE car.car_id IN " +
				"( SELECT permissions.car_id FROM permissions WHERE permissions.company_id = %d )";
		
		String sql = String.format( str, compId );
		
		
		try{
			synchronized ( this.compSynch ){
				rs = s.executeQuery(sql);
			}
			
			while ( rs.next() ){							//parse requests result;
				numb = new Number ( rs.getString( 1 ).toCharArray() );	//get cars number;
				addr = rs.getString( 2 );								//get address;
				date = rs.getLong( 3 );									//get date;
				mc = new MCar( numb, addr, date );
				result.add( mc );										//add to result list;
			}
			
		}catch( SQLException e ){
			System.out.println ( "Error while sampling cars from database!" );
			System.out.println( e.getMessage() );
		}
		
		return result;
	}
	
	//function return list of users by comapny id;
	public LinkedList< MUser > users( final int compId){
		LinkedList< MUser > result = new LinkedList<MUser>( );
		String str = "SELECT login, type FROM users WHERE company_id = %d";
		String sql = String.format( str, compId );
		String login;
		boolean type;
		ResultSet rs;
		MUser mu;
		try{
			rs = s.executeQuery( sql );
			
			while ( rs.next() ){										//parse requests result;
				login = rs.getString( 1 );								// get login;
				type = rs.getBoolean( 2 );								//get type;
				mu = new MUser( login, null, type );
				result.add( mu );
			}
		}catch ( SQLException e ){
			System.out.println ( "Error while sampling users from database!" );
			System.out.println( e.getMessage() );
		}
		
		return result;
	}
	
	//function return list of parking; 
	public LinkedList<MParking> parkings( final int compId ){
		LinkedList<MParking> result = new LinkedList<MParking>();
		
		String str = "SELECT address FROM Parking WHERE company_id = %d";
		String sql = String.format( str, compId );
		ResultSet rs;
		String addr;
		MParking mp;
		
		try{
			synchronized ( this.compSynch ){
				rs = s.executeQuery(sql);
			}
			
			while ( rs.next() ){					//parse requests result;
				addr = rs.getString( 1 );			//get address;
				mp = new MParking( addr );
				result.add( mp );
			}
			
		}catch ( SQLException e ){
			System.out.println ( "Error while sampling users from database!" );
			System.out.println( e.getMessage() );
		}
		
		return result;
	}
	
	//function disconnect database;
	public void delete(){
		c.disconnect();
	}
	
	//function create database;
	public void creteDB(){
		
		String Company = "CREATE TABLE Company( " +
				"   id SERIAL NOT NULL, name VARCHAR(20) UNIQUE, " +
			      "adm_login VARCHAR(20), " + 
			      "adm_pass VARCHAR(20)," +
			      " mark VARCHAR(100),PRIMARY KEY(id) )";
		
		String Parking = "CREATE TABLE Parking ( " +
 			      " parking_id SERIAL NOT NULL," +
 			      " company_id INT NOT NULL," +
			      " address VARCHAR(100) UNIQUE," +
			      " PRIMARY KEY(company_id, parking_id)," +		
			      " FOREIGN KEY (company_id) REFERENCES Company(id)  )";
		
		String Car = "CREATE TABLE Car ( " +
			      "   car_id SERIAL NOT NULL, " +
			      " number VARCHAR(8) UNIQUE, " +
			      " date BIGINT, " +
			      " PRIMARY KEY(car_id) )";

		String Permissions = "CREATE TABLE Permissions ( " +
			      "   id SERIAL NOT NULL," +
			      " parking_id INT, " +
			      " car_id INT, " +
			      " company_id INT, " +
			      " PRIMARY KEY(id), " +
			      " FOREIGN KEY (company_id, parking_id) REFERENCES Parking(company_id, parking_id), " +
			      " FOREIGN KEY (car_id) REFERENCES Car(car_id) )";	
		
		String User = "create table Users ( " +
			      "   company_id INT NOT NULL," +
			      " login VARCHAR(20) NOT NULL, " +
			      " password VARCHAR(20), " +
			      " type BOOLEAN," +
			      "PRIMARY KEY (company_id, login) ," +
			      "FOREIGN KEY (company_id) REFERENCES Company(id) )";	
		

		String History = "create table History ( " +
			      "   company VARCHAR(20), " +
			      " parking VARCHAR(25), " +
			      " date BIGINT, " +
			      " car_id VARCHAR(8), " +
			      " isfound BOOLEAN," +
			      " PRIMARY KEY (company, parking, date) )";	

		try{
			s.executeUpdate( Company );
			s.executeUpdate( Parking );
			s.executeUpdate( Car );
			s.executeUpdate( Permissions );
			s.executeUpdate( User );
			s.executeUpdate( History );
			
		}catch( SQLException e ){
			System.out.println( "Error while creating database!" );
			System.out.println( e.getMessage() );
			System.exit( 1 );
		}
	}

	
	public static void main( String... args ){
	
	
		DBConnection c = new DBConnection( );
		
		try{
			RequestSender rs = new RequestSender( );				
			
			rs.creteDB();
			
			System.out.println( "DB created successfuly!" );
			
			
		}finally{
			c.disconnect();
		}
			
		
	
	}

}
